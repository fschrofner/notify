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
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class DriveHandler {
	static Drive service;
	static GoogleAccountCredential credential;
	final static String TAG = "DriveHandler";

	static public void authenticate(Context _context) {
		Log.i(TAG, "authentication called");
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
	
	static public void setup(Context _context, String[] _fileList){
		Log.i(TAG, "setup called with filelist!");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
		String accountName = preferences.getString(SyncHandler.GOOGLE_DRIVE, null);
		
		if(_fileList != null && accountName == null){
			Log.i(TAG, "authentication with filelist called");
			if(credential == null){
				createCredentials(_context);
			}		 
			Intent intent = new Intent(_context,at.fhhgb.mc.notify.sync.drive.AuthenticationActivity.class);
			intent.putExtra(SyncHandler.UPLOAD_FILE_LIST, _fileList);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			_context.startActivity(intent);
		} else {
			setup(_context);
		}
	}
	
	static public void setup(Context _context){
		Log.i(TAG, "setup called!");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
		String accountName = preferences.getString(SyncHandler.GOOGLE_DRIVE, null);
			
		if(accountName == null){
			Log.i(TAG, "no account saved in preferences");
			DriveHandler.authenticate(_context);		
		} else {
			Log.i(TAG, "account found in preferences. setting up the saved account..");
			createCredentials(_context);
			credential.setSelectedAccountName(accountName);
			Drive.Builder builder = new Drive.Builder(AndroidHttp.newCompatibleTransport(), 
			    new GsonFactory(), at.fhhgb.mc.notify.sync.drive.DriveHandler.credential);
			builder.setApplicationName(SyncHandler.APPLICATION_NAME);
			service = builder.build();
		}
	}
	
	static public void updateFiles(Context _context, Activity _activity){
		DownloadThread downThread = new DownloadThread(_context, _activity);
		Thread thread = new Thread(downThread);
		thread.start();	
	}
	
	static public void buildService(){
		Drive.Builder builder = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), at.fhhgb.mc.notify.sync.drive.DriveHandler.credential);
		builder.setApplicationName(SyncHandler.APPLICATION_NAME);
		at.fhhgb.mc.notify.sync.drive.DriveHandler.service = builder.build();
	}
	
	static public ArrayList<File> getFileList(String _folderId) throws IOException{	
		ArrayList<File> fileList = new ArrayList<File>();
		if(_folderId != null){
			//gets the children of the notify subfolder
			ChildList children = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.children().list(_folderId).execute();
			ArrayList<ChildReference> childList = new ArrayList<ChildReference>();
				try {
					childList.addAll(children.getItems());
					//gets the fileobject of every child
					for(int i = 0; i<childList.size();i++){
						fileList.add(at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().get(childList.get(i).getId()).execute());
						Log.i(TAG, "got: " + fileList.get(i).getOriginalFilename());
					}			
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return fileList;
	}
	
	static public void uploadFiles(Context _context, Activity _activity, ArrayList<String> _fileList){
		UploadThread upThread = new UploadThread(_context, _activity, _fileList);
		Thread thread = new Thread(upThread);
		thread.start();
	}
	
	static public void deleteFiles(Context _context, Activity _activity, ArrayList<String> _fileNames){
		DeleteThread delThread = new DeleteThread(_context, _activity, _fileNames);
		Thread thread = new Thread(delThread);
		thread.start();
	}
	
}
