package at.fhhgb.mc.notify.sync.drive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class DriveHandler {
	static Drive service;
	static GoogleAccountCredential credential;
	static ArrayList<File> driveFileList;
//	static Files.List request;
	final static String TAG = "DriveHandler";

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
//			do {
//				FileList files = request.execute();
//			    result.addAll(files.getItems());
//			    request.setPageToken(files.getNextPageToken());
//			} while (request.getPageToken() != null && 
//					request.getPageToken().length() > 0);
			FileListThread fileThread = new FileListThread();
			Thread thread = new Thread(fileThread);
			thread.start();
		return driveFileList;
	}
	
	static public void downloadFiles(ArrayList<String> _files, Context _context){
		Log.i(TAG, "download files called");
		ArrayList<File> driveFiles = getDriveFileList();
		ArrayList<File> downloadFiles = new ArrayList<File>();
		for(int i=0;i<driveFiles.size();i++){
			if(_files.contains(driveFiles.get(i).getOriginalFilename())){
				downloadFiles.add(driveFiles.get(i));
				Log.i(TAG, "added " + driveFiles.get(i).getOriginalFilename() + " to download queue");
			}
		}
		DownloadThread downThread = new DownloadThread(downloadFiles, _context);
		Thread thread = new Thread(downThread);
		thread.start();
		
	}
	
}
