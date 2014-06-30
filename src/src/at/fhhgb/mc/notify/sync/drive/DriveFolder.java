package at.fhhgb.mc.notify.sync.drive;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import at.fhhgb.mc.notify.R;
import at.fhhgb.mc.notify.sync.SyncHandler;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;

public class DriveFolder {
	static final String TAG = "DriveFolder";
		
	/**
	 * Checks if there is a correct folder id set in the shared preferences,
	 * if not a folder is created and the id is saved.
	 * @return the id of the online folder
	 */
	public static String checkFolder(Context _context, Activity _activity){
		
		//TODO check for service, setup if null
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
		if(SyncHandler.networkConnected(_context)){
			//TODO change the folder from public folder to appdata folder
			if(preferences.contains(SyncHandler.GOOGLE_DRIVE_FOLDER)){
				try {
					String folderId = preferences.getString(SyncHandler.GOOGLE_DRIVE_FOLDER, null);
					Log.i(TAG, "found folder id " + folderId + "in preferences");
					File folder = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().get(folderId).execute();
					if(folder != null){
						Log.i(TAG, "confirmed online folder from preferences: " + folderId);
						return folderId;
					} else {
						Log.i(TAG, "online folder not confirmed!");
					}
				} catch (IOException e) {
					//if folder id is not matching an online folder
					Log.w(TAG, "found folder id, but the folder is not available on google drive!");
				}
			} 
			String onlineId = searchFolder(_activity);
			if(onlineId == null){
				createFolder(_context);
			} else {
				preferences.edit().putString(SyncHandler.GOOGLE_DRIVE_FOLDER, onlineId).commit();
				Log.i(TAG, "id saved in preferences");
				return onlineId;
			}
			return preferences.getString(SyncHandler.GOOGLE_DRIVE_FOLDER, null);
		} else {
			Log.i(TAG, "can't verify folder. device is not connected to the internet!");
			return null;
		}
	}
	
	private static String searchFolder(Activity _activity){
		//TODO when migrating to app data folder this method should become a lot shorter
		try {
			ChildList children = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.children().list("root").execute();
			ArrayList<ChildReference> childList = new ArrayList<ChildReference>();
			childList.addAll(children.getItems());
			for(int i=0;i<childList.size();i++){
				File temp = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().get(childList.get(i).getId()).execute();
				if(temp.getTitle().equals(SyncHandler.HOST_FOLDER) && temp.getMimeType().equals("application/vnd.google-apps.folder")){
					Log.i(TAG, "existing folder found on host with id: " + temp.getId());
					return temp.getId();
				}
			}
			
		} catch (UserRecoverableAuthIOException e) {
			  _activity.startActivityForResult(e.getIntent(), AuthenticationActivity.REQUEST_AUTHENTICATION);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i(TAG, "no folder with matching name found on host!");
		return null;
	}
	
	/**
	 * Checks if there's already an application folder inside the Google Drive.
	 * If so, the id will be saved into the shared preferences. If not, the
	 * folder will be created and the id will be saved.
	 */
	private static String createFolder(Context _context){
		String folderId = null;
			try {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
				File body = new File();
				body.setTitle(SyncHandler.HOST_FOLDER);
				body.setDescription(_context.getResources().getString(R.string.host_folder_description));
				body.setMimeType("application/vnd.google-apps.folder");
				File file;
				file = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().insert(body).execute();
				folderId = file.getId();
				preferences.edit().putString(SyncHandler.GOOGLE_DRIVE_FOLDER, folderId).commit();
				Log.i(TAG, "created online folder with id: " + folderId);
			} catch (IOException e) {
				e.printStackTrace();
			}
		return folderId;
	}
}
