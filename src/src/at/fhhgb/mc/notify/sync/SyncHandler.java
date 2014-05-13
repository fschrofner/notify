package at.fhhgb.mc.notify.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import at.fhhgb.mc.notify.sync.drive.DriveHandler;

public class SyncHandler {
	final static public String NOTIFICATION_FOLDER = "notifications";
	final static public String FILE_FOLDER = "notification_files";
	final static public String GOOGLE_DRIVE = "google_drive";
	
	static private ArrayList<String> getFilesToUpdate(Context _context){
		ArrayList<String> hostFiles = DriveHandler.getFileList();
		ArrayList<String> missingFiles = new ArrayList<String>();
		File folder = new File(_context.getFilesDir()+"/"+NOTIFICATION_FOLDER); 
		String[] fileList = folder.list();
		List<String> localFiles = Arrays.asList(fileList);
		for(int i=0;i<hostFiles.size();i++){
			if(!localFiles.contains(hostFiles.get(i))){
				missingFiles.add(hostFiles.get(i));
				//TODO if there's a new revision of a file (if the filename contains a "_"
				//and has a newer revision number) an update needs to take place
			}
		}
		return missingFiles;
	}
}
