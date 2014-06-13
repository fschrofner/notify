package at.fhhgb.mc.notify.sync.drive;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import at.fhhgb.mc.notify.sync.SyncHandler;

public class DownloadThread implements Runnable {

	final static String TAG = "DownloadThread";
	Context mContext;
	
	public DownloadThread(Context _context){
		mContext = _context;
	}
	
	@Override
	public void run() {
		Log.i(TAG, "started download thread");
		//TODO check for authentication, check if folder exists
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		
//		if(!preferences.contains(SyncHandler.GOOGLE_DRIVE_FOLDER) && !preferences.getString(SyncHandler.GOOGLE_DRIVE_FOLDER, "").equals("")){
//			//TODO first check if the folder exists (created by other application)
//			createFolder();
//		}
		
		String folderId = preferences.getString(SyncHandler.GOOGLE_DRIVE_FOLDER, "");
		
		at.fhhgb.mc.notify.sync.drive.DriveHandler.service = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), at.fhhgb.mc.notify.sync.drive.DriveHandler.credential).build();
		try {
			ArrayList<File> hostFiles = getFileList(folderId);
			downloadFiles(getMissingFiles(hostFiles, mContext),mContext);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Checks if there is a correct folder id set in the shared preferences,
	 * if not a folder is created and the id is saved.
	 * @return the id of the online folder
	 */
	private String checkFolder(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		//TODO change the folder from public folder to appdata folder
		if(preferences.contains(SyncHandler.GOOGLE_DRIVE_FOLDER)){
			try {
				String folderId = preferences.getString(SyncHandler.GOOGLE_DRIVE_FOLDER, null);
				Log.i(TAG, "found folder id " + folderId + "in preferences");
				File folder = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().get(folderId).execute();
				if(folder != null){
					Log.i(TAG, "confirmed online folder from preferences: " + folderId);
					return folderId;
				}
			} catch (IOException e) {
				//if folder id is not matching an online folder
				Log.w(TAG, "found folder id, but the folder is not available on google drive!");
			}
		} 
		//TODO search for folder and save in preferences
		createFolder();
		return preferences.getString(SyncHandler.GOOGLE_DRIVE_FOLDER, null);
	}
	
	private String searchFolder(){
		//TODO get filelist of online files and compare file names.
		return null;
	}
	
	/**
	 * Checks if there's already an application folder inside the Google Drive.
	 * If so, the id will be saved into the shared preferences. If not, the
	 * folder will be created and the id will be saved.
	 */
	private void createFolder(){
			try {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
				File body = new File();
				body.setTitle(SyncHandler.HOST_FOLDER);
				body.setDescription(SyncHandler.HOST_DESCRIPTION);
				body.setMimeType("application/vnd.google-apps.folder");
				File file;
				file = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().insert(body).execute();
				String folderId = file.getId();
				preferences.edit().putString(SyncHandler.GOOGLE_DRIVE_FOLDER, folderId).commit();
				Log.i(TAG, "created online folder with id: " + folderId);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private ArrayList<File> getFileList(String _folderId) throws IOException{	
		String folderId = checkFolder();
		ArrayList<File> fileList = new ArrayList<File>();
		if(folderId != null){
			//gets the children of the notify subfolder
			ChildList children = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.children().list(folderId).execute();
			ArrayList<ChildReference> childList = new ArrayList<ChildReference>();
				try {
					childList.addAll(children.getItems());
					//gets the fileobject of every child
					for(int i = 0; i<childList.size();i++){
						fileList.add(at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().get(childList.get(i).getId()).execute());
						Log.i(TAG, "got: " + fileList.get(i).getOriginalFilename());
					}			
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return fileList;
	}
	
	private ArrayList<File> getMissingFiles(ArrayList<File> _hostFiles,Context _context){
		ArrayList<File> missingFiles = new ArrayList<File>();
		java.io.File rootFolder = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER); 
		java.io.File folder = new java.io.File(rootFolder+"/"+SyncHandler.NOTIFICATION_FOLDER); 
		//creates directories when the folder is not existent
		folder.mkdirs();
		String[] fileList = folder.list();
		List<String> localFiles = Arrays.asList(fileList);
		
		for(int i=0;i<_hostFiles.size();i++){
			if(_hostFiles.get(i).getOriginalFilename() != null && !localFiles.contains(_hostFiles.get(i).getOriginalFilename())){
				missingFiles.add(_hostFiles.get(i));
				Log.i(TAG, "file " + _hostFiles.get(i).getOriginalFilename() + " added to missing files");
				//TODO if there's a new revision of a file (if the filename contains a "_"
				//and has a newer revision number) an update needs to take place
			} else {
				Log.i(TAG, "file " + _hostFiles.get(i).getOriginalFilename() + " already exists in filesystem (or is null)");
			}
		}
		return missingFiles;
	}
	
	private void downloadFiles(ArrayList<File> _files, Context _context){
		InputStream inputStream = null;
		OutputStream outputStream = null;
		 try {
			java.io.File rootFolder = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER); 
			java.io.File folder = new java.io.File(rootFolder,SyncHandler.NOTIFICATION_FOLDER);
			folder.mkdirs();
			
			for(int i=0;i<_files.size();i++){
				//check for correct file
				if(_files.get(i) != null){
					String downloadLink = _files.get(i).getDownloadUrl(); 
					
					//check if the file is downloadable
					if(downloadLink != null){
						GenericUrl downloadUrl = new GenericUrl(downloadLink);
						Log.i(TAG, "downloadlink created: " + downloadLink.toString());
						HttpResponse resp = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.getRequestFactory().
								 buildGetRequest(downloadUrl).execute();
						inputStream = resp.getContent();
						java.io.File outputFile = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER + "/" + 
								SyncHandler.NOTIFICATION_FOLDER, _files.get(i).getOriginalFilename());
						outputFile.createNewFile();
						outputStream = new FileOutputStream(outputFile);
						int read = 0;
						byte[] bytes = new byte[1024];
				 
						while ((read = inputStream.read(bytes)) != -1) {
							outputStream.write(bytes, 0, read);
						}
						Log.i(TAG, "file " + _files.get(i).getOriginalFilename() + " complete!");
					}
				}			
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO organise files in sub-folders
	}
}


