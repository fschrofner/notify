package at.fhhgb.mc.notify.sync.drive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.content.Intent;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class DriveHandler {
	static Drive service;
	static GoogleAccountCredential credential;
	final String TAG = "DriveHandler";

	static public void authenticate(Context _context) {
		credential = GoogleAccountCredential.usingOAuth2(_context,
				Collections.singleton(DriveScopes.DRIVE));
		Intent intent = new Intent(_context,at.fhhgb.mc.notify.sync.drive.AuthenticationActivity.class);
		_context.startActivity(intent);
	}
	
	static public ArrayList<String> getFileList(){
		ArrayList<File> files = getDriveFileList();
		ArrayList<String> result = new ArrayList<String>();
		
		for(int i=0;i<files.size();i++){
			result.add(files.get(i).getOriginalFilename());
		}
		return result;
	}
	
	static private ArrayList<File> getDriveFileList(){
		ArrayList<File> result = new ArrayList<File>();
		try {
			Files.List request = service.files().list();
			do {
				FileList files = request.execute();
			    result.addAll(files.getItems());
			    request.setPageToken(files.getNextPageToken());
			} while (request.getPageToken() != null && 
					request.getPageToken().length() > 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
}
