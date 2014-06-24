package at.fhhgb.mc.notify.sync.drive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.R;
import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import at.fhhgb.mc.notify.push.PushConstants;
import at.fhhgb.mc.notify.push.PushSender;
import at.fhhgb.mc.notify.sync.SyncHandler;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Parents;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

public class UploadThread implements Runnable {

	final static String TAG = "UploadThread";
	private Context mContext;
	private Activity mActivity;
	private ArrayList<String> mFileList;
	private boolean mFinishedUpload;
	private boolean mConnected;
	
	public UploadThread(Context _context, Activity _activity, ArrayList<String> _fileList){
		mContext = _context;
		mActivity = _activity;
		mFileList = _fileList;
		mFinishedUpload = false;
		mConnected = true;
	}
	
	@Override
	public void run() {
		Log.i(TAG, "started upload thread");
		
		//sets up the drive service. filelist is given for callback
		String[] fileList = mFileList.toArray(new String[mFileList.size()]);
		DriveHandler.setup(mContext, fileList);
		
		if(DriveHandler.service != null){
			//searches and validates a folder id that is saved in the shared preferences
			//creates a new folder if none is existent
			DriveFolder.checkFolder(mContext);
			
			//uploads all files from the list
			for(int i=0; i<mFileList.size();i++){
				mFinishedUpload = false;
				do{
					uploadFile(mFileList.get(i));
					Log.i(TAG, "tried uploading file: " + i);
					
					//tries uploading the file until the upload completes
					if(!mFinishedUpload && mConnected){
						try {
							Thread.sleep(10000);
							Log.i(TAG, "upload failed! waiting for 10 seconds and retry");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//internet connectivity is not present, doesn't retry
					if(!mConnected){
						break;
					}
				} while (!mFinishedUpload);
			}
			
			//update all files after upload
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
			
			//TODO set push alias in sharedpreferences before reading
			String alias = preferences.getString(PushConstants.PUSH_ALIAS, null);
			PushSender.sendPushToAlias(alias);
			//SyncHandler.updateFiles(mContext);
		} else {
			Log.i(TAG, "no drive service running!");
		}
	}
	
	private void uploadFile(String _fileName){
		try{
			at.fhhgb.mc.notify.sync.drive.DriveHandler.service = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
					new GsonFactory(), at.fhhgb.mc.notify.sync.drive.DriveHandler.credential).build();
			java.io.File file = new java.io.File(SyncHandler.getFullPath(_fileName));
			File body = new File();
			body.setTitle(file.getName());
			body.setMimeType("text/plain");
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
			String parentId = preferences.getString(SyncHandler.GOOGLE_DRIVE_FOLDER, null);
			if(parentId != null){
				body.setParents(Arrays.asList(new ParentReference().setId(parentId)));
			}	

			FileContent mediaContent = new FileContent(getMimeType(file), file);
			File resultFile;
	
			resultFile = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().insert(body, mediaContent).execute();
			
			//sets the boolean flag to true
			mFinishedUpload = true;
			Log.i(TAG, "upload complete, file id: " + resultFile.getId());
		} catch (UserRecoverableAuthIOException e) {
			if(mActivity != null){
				mActivity.startActivityForResult(e.getIntent(), at.fhhgb.mc.notify.sync.drive.AuthenticationActivity.REQUEST_AUTHENTICATION);
			} else {
				Log.w(TAG, "upload thread not started from activity and authentication error occurred!");
			}
	          
	        } catch (IOException e) {
	        	//TODO schedule a re-upload, if network error. save files to upload, if internet connectivity is deactivated.	
	        	Log.w(TAG, "network error!");
	        	ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);    	 
	        	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	        	boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	        	if(!isConnected){
	        		//TODO register broadcast receiver for network connectivity changed and save files to upload in shared preferences
	        		mConnected = false;
	        		Log.i(TAG, "no internet connectivity! scheduled upload for connectivity change");
	        	}
		}
	}
	
	private String getMimeType(java.io.File _file){
		MimeTypeMap mime = MimeTypeMap.getSingleton();
	    String ext = SyncHandler.getFileExtension(_file.getName());
	    
	    //setting the extension to the real mime type extension
	    if(ext.equals(SyncHandler.NOTIFICATION_FILE_EXTENSION)){
	    	ext = "xml";
	    }
	    
	    String type = mime.getMimeTypeFromExtension(ext);
		Log.i(TAG, "mime type of file is: " + type);
		return type;
	}
}
