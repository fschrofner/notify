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
	
	
	static public void updateFiles(Context _context){
		DownloadThread downThread = new DownloadThread(_context);
		Thread thread = new Thread(downThread);
		thread.start();	
	}
	
}
