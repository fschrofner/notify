package at.fhhgb.mc.notify.sync.drive;

import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class FileListThread implements Runnable{

	final static String TAG = "FileListThread";
	Files.List mRequest;
	
	FileListThread(){			
		try {
			at.fhhgb.mc.notify.sync.drive.DriveHandler.driveFileList = new ArrayList<File>();
			mRequest = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().list();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		Log.i(TAG, "thread started!");
		FileList files;
		do {
			try {
				files = mRequest.execute();
				at.fhhgb.mc.notify.sync.drive.DriveHandler.driveFileList.addAll(files.getItems());
				mRequest.setPageToken(files.getNextPageToken());
				Log.i(TAG, "got: " + files.getItems().get(0).getOriginalFilename());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (mRequest.getPageToken() != null && 
				mRequest.getPageToken().length() > 0);	
	}
	
}