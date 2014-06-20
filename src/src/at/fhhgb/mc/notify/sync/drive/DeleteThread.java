package at.fhhgb.mc.notify.sync.drive;

import java.io.IOException;
import java.util.ArrayList;

import com.google.api.services.drive.model.File;

import android.app.Activity;
import android.content.Context;
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

	public DeleteThread(Context _context, Activity _activity, ArrayList<String> _fileList){
		mContext = _context;
		mActivity = _activity;
		mFileList = _fileList;
	}
	
	@Override
	public void run() {
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
		deleteHostFile(_fileName);
		if(oldFile != null){
			oldFile.delete();
			Log.i(TAG, "file " + _fileName + " was deleted from the local filesystem");
		}			
	}
	
	private void deleteHostFile(String _fileName){
		for(int i=0; i<mHostFiles.size(); i++){
			if(mHostFiles.get(i).getOriginalFilename().equals(_fileName)){
				try {
					String id = mHostFiles.get(i).getId();
					DriveHandler.service.files().delete(id).execute();
					Log.i(TAG, "file " + _fileName + " deleted on the host filesystem");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void updateFileList(){	
		try {
			String folderId = DriveFolder.checkFolder(mContext);
			mHostFiles = DriveHandler.getFileList(folderId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
