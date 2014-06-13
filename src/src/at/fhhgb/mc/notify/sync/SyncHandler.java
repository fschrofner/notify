package at.fhhgb.mc.notify.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	
	/**
	 * Initiates an update 
	 */
	static public void updateFiles(Context _context){
		//TODO  differentiate the different hosts
		DriveHandler.updateFiles(_context);
	}
	
	static private ArrayList<String> removeRevisions(ArrayList<String> _files){
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0;i<_files.size();i++){
			result.add(_files.get(i).substring(_files.get(i).lastIndexOf("_")));
		}
		return result;
	}
}
