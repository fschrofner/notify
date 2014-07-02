package at.fhhgb.mc.notify.sync.drive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.MimeTypeMap;
import at.fhhgb.mc.notify.sync.SyncHandler;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;


/**
 * Thread used to upload files from the local file system to the host.
 * @author Dominik Koeltringer & Florian Schrofner
 *
 */
public class UploadThread implements Runnable {

	final static String TAG = "UploadThread";
	private Context mContext;
	private volatile Activity mActivity;
	private ArrayList<String> mFileList;
	private boolean mFinishedUpload;
	private boolean mConnected;
	
	/**
	 * Creates a thread that will upload the specified files when run.
	 * @param _context context needed for some methods
	 * @param _activity activity to show authentication dialog 
	 * @param _fileList files to upload
	 */
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
			String folderId = DriveFolder.checkFolder(mContext, mActivity);
			
			//if folderId is null there must have been some error
			if(folderId != null){
				//uploads all files from the list
				for(int i=0; i<mFileList.size();i++){
					mFinishedUpload = false;
					do{
						uploadFile(mFileList.get(i),folderId);
						Log.i(TAG, "tried uploading file: " + mFileList.get(i));
						
						//tries uploading the file until the upload completes
						if(!mFinishedUpload && mConnected){
							try {
								Thread.sleep(10000);
								Log.i(TAG, "upload failed! waiting for 10 seconds and retry");
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						//internet connectivity is not present, don't retry but schedule the upload
						if(!mConnected){
							ArrayList<String> uploadFile = new ArrayList<String>();
							uploadFile.add(mFileList.get(i));
							scheduleUpload(uploadFile);
							break;
						}
					} while (!mFinishedUpload);
				}
				//update all files after upload
				SyncHandler.sendPush(mContext);
			} else {
				Log.w(TAG, "could not check for folder! scheduling all uploads");
				scheduleUpload(mFileList);
			}
			
		} else {
			Log.i(TAG, "no drive service running!");
		}
	}
	
	/**
	 * Schedules the upload until a connectivity change occurs.
	 * The files that will be uploaded then are written into shared preferences.
	 * @param _fileList the files to upload at connectivity change
	 */
	private void scheduleUpload(ArrayList<String> _fileList){
		SharedPreferences outstanding = mContext.getSharedPreferences(SyncHandler.OUTSTANDING_TASKS, Context.MODE_PRIVATE); 
		HashSet<String> redoFileList;
		
		//if already other uploads are scheduled, just append the new file
		if(outstanding.contains(SyncHandler.OUTSTANDING_UPLOAD)){
			redoFileList = (HashSet<String>) outstanding.getStringSet(SyncHandler.OUTSTANDING_UPLOAD, null);
		} else {
			redoFileList = new HashSet<String>();
		}
		
		for(int i=0; i<_fileList.size();i++){
			//adds the file to the missing uploads, if not present yet
			if(!redoFileList.contains(_fileList.get(i))){
				redoFileList.add(_fileList.get(i));
				Log.i(TAG, "added the file " + _fileList.get(i) + " to the scheduled uploads");
			} else {
				Log.i(TAG, "upload of file " + _fileList.get(i) + " already scheduled");
			}
		}
		
		outstanding.edit().putStringSet(SyncHandler.OUTSTANDING_UPLOAD, redoFileList).commit();
	}
	
	
	/**
	 * Uploads the given file into the specified parent.
	 * @param _fileName the file to upload
	 * @param _parentId the intended parent of the file
	 */
	private void uploadFile(String _fileName, String _parentId){
		try{
			
			//builds the service before it can be used
			DriveHandler.buildService();
			ArrayList<File> hostFiles = DriveHandler.getFileList(_parentId);
			boolean contains = false;
			for(int i = 0;i < hostFiles.size() ;i++){
				if(hostFiles.get(i).getOriginalFilename().equals(_fileName)){
					contains = true;
				}
			}
			
			//no need to upload files that are present on the host
			if(contains){
				Log.i(TAG, "file already present on the host, no need to upload");
				mFinishedUpload = true;
				return;
			}
			
			java.io.File file = new java.io.File(SyncHandler.getFullPath(_fileName));
			File body = new File();
			body.setTitle(file.getName());
			body.setMimeType("text/plain");
			body.setParents(Arrays.asList(new ParentReference().setId(_parentId)));

			FileContent mediaContent = new FileContent(getMimeType(file), file);
			File resultFile;
	
			resultFile = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().insert(body, mediaContent).execute();
			
			//sets the boolean flag to true
			mFinishedUpload = true;
			Log.i(TAG, "upload complete, file id: " + resultFile.getId());
		} catch (UserRecoverableAuthIOException e) {
			if(mActivity != null){
				Intent intent = new Intent(mContext, AuthenticationActivity.class);
				mActivity.startActivity(intent);
			} else {
				Log.w(TAG, "upload thread not started from activity and authentication error occurred!");
			}
	          
	        } catch (FileNotFoundException e){
	        	//simply forget the file now
	        	mFinishedUpload = true;
	        	Log.w(TAG, "file " + _fileName + " not found. stopping upload..");
			}catch (IOException e) {
	        	Log.w(TAG, "network error!");
	        	boolean isConnected = SyncHandler.networkConnected(mContext);
	        	if(!isConnected){
	        		mConnected = false;
	        		Log.i(TAG, "no internet connectivity! scheduled upload for connectivity change");
	        	}
		}
	}
	
	/**
	 * Returns the mimetype of the given file.
	 * @param _file the file of which to determinate the mimetype
	 * @return the mimetype of the file as string
	 */
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
