package at.fhhgb.mc.notify.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import at.fhhgb.mc.notify.sync.drive.DriveHandler;

public class SyncHandler {
	final static String TAG = "SyncHandler";
	final static public String NOTIFICATION_FOLDER = "notifications";
	final static public String FILE_FOLDER = "notification_files";
	final static public String HOST_FOLDER = "Notify";
	final static public String HOST_DESCRIPTION = "A folder used for notify syncing";
	final static public String GOOGLE_DRIVE = "google_drive";
	final static public String GOOGLE_DRIVE_FOLDER = "google_drive_folder_id";
	final static public String ROOT_NOTIFICATION_FOLDER = "/storage/emulated/0/Notify/";
	final static public String NOTIFICATION_FILE_EXTENSION = "noti";
	final static public String UPLOAD_FILE_LIST = "filelist";
	
	/**
	 * Initiates an update 
	 */
	static public void updateFiles(Context _context){
		//TODO  differentiate the different hosts
		DriveHandler.updateFiles(_context);
	}
	
	static public void uploadFiles(Context _context,Activity _activity, ArrayList<String> _fileList){
		//TODO differentiate different hosts
		DriveHandler.uploadFiles(_context, _activity, _fileList);
	}
	
	static private ArrayList<String> removeRevisions(ArrayList<String> _files){
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0;i<_files.size();i++){
			result.add(_files.get(i).substring(_files.get(i).lastIndexOf("_")));
		}
		return result;
	}
	
	public static String getFullPath(String _fileName){
		String fullPath;
		String fileExtension = getFileExtension(_fileName);
		if(fileExtension.equals(SyncHandler.NOTIFICATION_FILE_EXTENSION)){
			fullPath = SyncHandler.ROOT_NOTIFICATION_FOLDER+"/"+SyncHandler.NOTIFICATION_FOLDER + "/" + _fileName;
			Log.i(TAG, "file in notification folder, because extension is: " + fileExtension);
		} else {
			fullPath = SyncHandler.ROOT_NOTIFICATION_FOLDER+"/"+SyncHandler.FILE_FOLDER + "/" + _fileName;
			Log.i(TAG, "file in file folder, because extension is: " + fileExtension);
		}
		return fullPath;
	}
	
	public static String getFileExtension(String _fileName){
		int lastIndex = _fileName.lastIndexOf(".") + 1;
		if(lastIndex < 0){
			lastIndex = _fileName.length();
		}
		String fileExtension = _fileName.substring(lastIndex,_fileName.length());
		fileExtension = fileExtension.toLowerCase();
		return fileExtension;
	}
}
