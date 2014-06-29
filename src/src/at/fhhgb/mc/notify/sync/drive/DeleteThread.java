package at.fhhgb.mc.notify.sync.drive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import com.google.api.services.drive.model.File;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import at.fhhgb.mc.notify.notification.Notification;
import at.fhhgb.mc.notify.sync.SyncHandler;
import at.fhhgb.mc.notify.xml.XmlParser;

public class DeleteThread implements Runnable {
	
	static final String TAG = "DeleteThread";
	
	Context mContext;
	Activity mActivity;
	ArrayList<String> mFileList;
	ArrayList<File> mHostFiles;
	private boolean mFinishedDeletion;
	private boolean mConnected;

	public DeleteThread(Context _context, Activity _activity, ArrayList<String> _fileList){
		mContext = _context;
		mActivity = _activity;
		mFileList = _fileList;
		mFinishedDeletion = false;
		mConnected = true;
	}
	
	@Override
	public void run() {
		//TODO schedule deletion, when there's no internet connection
		DriveHandler.setup(mContext);
		updateFileList();
		
		for(int i=0; i<mFileList.size(); i++){
			deleteFile(mFileList.get(i));
		}
	}
	
	private void deleteFile(String _fileName){
		java.io.File oldFile;
		
		if(SyncHandler.getFileExtension(_fileName).equals(SyncHandler.NOTIFICATION_FILE_EXTENSION)){
			
			try {
				XmlParser parser = new XmlParser(mContext);
				Notification notification = parser.readXml(_fileName);
				
				if(notification != null){
					ArrayList<String> files = notification.getFiles();
					
					if(files != null){
						for(int i=0;i<files.size();i++){
							deleteFile(files.get(i));
						}
					}
				}
								
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		oldFile = new java.io.File(SyncHandler.getFullPath(_fileName));
		
		mFinishedDeletion = false;
		do{
			deleteHostFile(_fileName);
			if(!mFinishedDeletion && mConnected){
				try {
					Thread.sleep(10000);
					Log.i(TAG, "deletion failed! waiting for 10 seconds and retry");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//internet connectivity is not present, don't retry but schedule the deletion
			if(!mConnected){
				ArrayList<String> deleteFile = new ArrayList<String>();
				deleteFile.add(_fileName);
				scheduleDeletion(deleteFile);
				break;
			}
		} while(!mFinishedDeletion);
		
		if(oldFile != null){
			oldFile.delete();
			Log.i(TAG, "file " + _fileName + " was deleted from the local filesystem");
		}			
	}
	
	private void scheduleDeletion(ArrayList<String> _fileList){
		SharedPreferences outstanding = mContext.getSharedPreferences(SyncHandler.OUTSTANDING_TASKS, Context.MODE_PRIVATE); 
		HashSet<String> redoFileList;
		
		//if already other deletions are scheduled, just append the new file
		if(outstanding.contains(SyncHandler.OUTSTANDING_DELETION)){
			redoFileList = (HashSet<String>) outstanding.getStringSet(SyncHandler.OUTSTANDING_DELETION, null);
		} else {
			redoFileList = new HashSet<String>();
		}
		
		for(int i=0; i<_fileList.size();i++){
			//adds the file to the missing uploads, if not present yet
			if(!redoFileList.contains(_fileList.get(i))){
				redoFileList.add(_fileList.get(i));
				Log.i(TAG, "added the file " + _fileList.get(i) + " to the scheduled deletions");
			} else {
				Log.i(TAG, "deletion of file " + _fileList.get(i) + " already scheduled");
			}
		}
		
		outstanding.edit().putStringSet(SyncHandler.OUTSTANDING_DELETION, redoFileList).commit();
	}
	
	
	private void deleteHostFile(String _fileName){
		for(int i=0; i<mHostFiles.size(); i++){
			if(mHostFiles.get(i).getOriginalFilename().equals(_fileName)){
				try {
					String id = mHostFiles.get(i).getId();
					DriveHandler.service.files().delete(id).execute();
					Log.i(TAG, "file " + _fileName + " deleted on the host filesystem");
				} catch (IOException e) {
					//TODO schedule a re-upload, if network error. save files to upload, if internet connectivity is deactivated.	
		        	Log.w(TAG, "network error!");
		        	boolean isConnected = SyncHandler.networkConnected(mContext);
		        	if(!isConnected){
		        		//TODO register broadcast receiver for network connectivity changed and save files to upload in shared preferences
		        		mConnected = false;
		        		Log.i(TAG, "no internet connectivity! scheduled upload for connectivity change");
		        	}
				}
			}
		}
		mConnected = SyncHandler.networkConnected(mContext);
		mFinishedDeletion = true;	
	}
	
	private void updateFileList(){	
		try {
			String folderId = DriveFolder.checkFolder(mContext, mActivity);
			mHostFiles = DriveHandler.getFileList(folderId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
