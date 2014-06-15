package at.fhhgb.mc.notify.sync.drive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.R;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
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
	Context mContext;
	Activity mActivity;
	ArrayList<String> mFileList;
	
	public UploadThread(Context _context, Activity _activity, ArrayList<String> _fileList){
		mContext = _context;
		mActivity = _activity;
		mFileList = _fileList;
	}
	
	@Override
	public void run() {
		// credential.setSelectedAccountName(accountName);
		//TODO upload selected files only
		Log.i(TAG, "started upload thread");	
		for(int i=0; i<mFileList.size();i++){
			uploadFile(mFileList.get(i));
		}
	}
	
	private void uploadFile(String _fileName){
		try{
			at.fhhgb.mc.notify.sync.drive.DriveHandler.service = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
					new GsonFactory(), at.fhhgb.mc.notify.sync.drive.DriveHandler.credential).build();
			java.io.File file = new java.io.File(SyncHandler.getFullPath(_fileName));
			File body = new File();
			body.setTitle(file.getName());
			//body.setDescription("A test document");
			body.setMimeType("text/plain");
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
			String parentId = preferences.getString(SyncHandler.GOOGLE_DRIVE_FOLDER, null);
			if(parentId != null){
				body.setParents(Arrays.asList(new ParentReference().setId(parentId)));
			}	
			//TODO change mediatype
			FileContent mediaContent = new FileContent("text/plain", file);
			File resultFile;
	
			resultFile = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().insert(body, mediaContent).execute();
			Log.i(TAG, "upload complete, file id: " + resultFile.getId());
		} catch (UserRecoverableAuthIOException e) {
	          mActivity.startActivityForResult(e.getIntent(), at.fhhgb.mc.notify.sync.drive.AuthenticationActivity.REQUEST_AUTHENTICATION);
	        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createApplicationFolder(){
		File folder =  new File();
		folder.setTitle(SyncHandler.HOST_FOLDER);
		//TODO replace description with translatable string
		folder.setDescription(SyncHandler.HOST_DESCRIPTION);
		folder.setMimeType("application/vnd.google-apps.folder");
	}
}
