package at.fhhgb.mc.notify.sync.drive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import at.fhhgb.mc.notify.sync.SyncHandler;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class DriveHandler {
	static Drive service;
	static GoogleAccountCredential credential;
//	static ArrayList<File> driveFileList;
//	static Files.List request;
	final static String TAG = "DriveHandler";

	static public void authenticate(Context _context) {
		if(credential == null){
			createCredentials(_context);
		}		
		Intent intent = new Intent(_context,at.fhhgb.mc.notify.sync.drive.AuthenticationActivity.class);
		_context.startActivity(intent);
	}
	
	static public void createCredentials(Context _context){
		credential = GoogleAccountCredential.usingOAuth2(_context,
				Collections.singleton(DriveScopes.DRIVE));
	}
	
	static public void setup(Context _context){
		Log.i(TAG, "setup called!");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
		String accountName = preferences.getString(SyncHandler.GOOGLE_DRIVE, null);
			
		if(accountName == null){
			DriveHandler.authenticate(_context);
			Log.i(TAG, "no account saved in preferences");
		} else {
			Log.i(TAG, "account found in preferences. setting up the saved account..");
			createCredentials(_context);
			credential.setSelectedAccountName(accountName);
			service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), 
			    new GsonFactory(), at.fhhgb.mc.notify.sync.drive.DriveHandler.credential).build();
		}
	}
	
	static public void updateFiles(Context _context){
		DownloadThread downThread = new DownloadThread(_context);
		Thread thread = new Thread(downThread);
		thread.start();	
	}
	
	static public void uploadFiles(Context _context, Activity _activity, ArrayList<String> _fileList){
		UploadThread upThread = new UploadThread(_context, _activity, _fileList);
		Thread thread = new Thread(upThread);
		thread.start();
	}
	
}
