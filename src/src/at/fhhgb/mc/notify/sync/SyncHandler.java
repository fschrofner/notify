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
	
	/**
	 * Compares the local files with the files on the selected host
	 * and returns a list of strings, which contains the file names of the
	 * files that are missing locally.
	 * @param _context a context which is needed for operation
	 * @return a list of strings, containing the filenames of the missing files.
	 */
	static private ArrayList<String> getMissingFiles(ArrayList<String> _hostFiles,Context _context){
		ArrayList<String> missingFiles = new ArrayList<String>();
		File folder = new File(_context.getFilesDir()+"/"+NOTIFICATION_FOLDER); 
		//TODO crash when folder not existent 
		//String[] fileList = folder.list();
		String[] fileList = {"abc"};
		List<String> localFiles = Arrays.asList(fileList);
		for(int i=0;i<_hostFiles.size();i++){
			if(!localFiles.contains(_hostFiles.get(i))){
				missingFiles.add(_hostFiles.get(i));
				//TODO if there's a new revision of a file (if the filename contains a "_"
				//and has a newer revision number) an update needs to take place
			}
		}
		return missingFiles;
	}
	
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
