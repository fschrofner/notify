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
				} else {
					Log.i(TAG, "online folder not confirmed!");
				}
			} catch (IOException e) {
				//if folder id is not matching an online folder
				Log.w(TAG, "found folder id, but the folder is not available on google drive!");
			}
		} 
		//TODO search for folder and save in preferences
		String onlineId = searchFolder();
		if(onlineId == null){
			createFolder();
		} else {
			preferences.edit().putString(SyncHandler.GOOGLE_DRIVE_FOLDER, onlineId).commit();
			Log.i(TAG, "id saved in preferences");
			return onlineId;
		}
		return preferences.getString(SyncHandler.GOOGLE_DRIVE_FOLDER, null);
	}
	
	private String searchFolder(){
		//TODO get filelist of online files and compare file names.
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
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	private String createFolder(){
		String folderId = null;
			try {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
				File body = new File();
				body.setTitle(SyncHandler.HOST_FOLDER);
				body.setDescription(SyncHandler.HOST_DESCRIPTION);
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
					e.printStackTrace();
				}
		}
		return fileList;
	}
	
	private ArrayList<File> getMissingFiles(ArrayList<File> _hostFiles,Context _context){
		ArrayList<File> missingFiles = new ArrayList<File>();
		java.io.File rootFolder = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER); 
		
		//opens the notification folder
		java.io.File notificationFolder = new java.io.File(rootFolder+"/"+SyncHandler.NOTIFICATION_FOLDER); 
		notificationFolder.mkdirs();
		
		//gets the file list
		String[] notificationFileList = notificationFolder.list();
		
		//opens the file folder
		java.io.File fileFolder = new java.io.File(rootFolder+"/"+SyncHandler.FILE_FOLDER);
		fileFolder.mkdirs();
		
		//gets filelist
		String[] fileFileList = fileFolder.list();
		
		//joins the two filelists into one list
		ArrayList<String> localFiles = new ArrayList<String>();
		localFiles.addAll(Arrays.asList(notificationFileList));
		localFiles.addAll(Arrays.asList(fileFileList));
		
		List<String> hostFileList = new ArrayList<String>();
		
		//adds files to missing file list, which are not present on the local file system
		for(int i=0;i<_hostFiles.size();i++){
			if(_hostFiles.get(i).getOriginalFilename() != null){
				hostFileList.add(_hostFiles.get(i).getOriginalFilename());
			
				if(!localFiles.contains(hostFileList.get(hostFileList.size()-1))){
					missingFiles.add(_hostFiles.get(i));
					Log.i(TAG, "file " + hostFileList.get(hostFileList.size()-1) + " added to missing files");
					//TODO if there's a new revision of a file (if the filename contains a "_"
					//and has a newer revision number) an update needs to take place
				} else {
					Log.i(TAG, "file " + hostFileList.get(hostFileList.size()-1) + " already exists in filesystem (or is null)");
				}
			}
		}
		
		//searches for files that are not present on the host, but on the local system and deletes them
		for(int i=0;i<localFiles.size();i++){
			if(!hostFileList.contains(localFiles.get(i))){
				deleteFile(localFiles.get(i));
			}
		}
		return missingFiles;
	}
	
	private void deleteFile(String _fileName){
		java.io.File oldFile;
		if(getFileExtension(_fileName).equals(SyncHandler.NOTIFICATION_FILE_EXTENSION)){
			oldFile = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER+"/"+SyncHandler.NOTIFICATION_FOLDER + "/" + _fileName);
			//TODO get associated files for notification and check if notification is really outdated
		} else {
			oldFile = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER+"/"+SyncHandler.FILE_FOLDER + "/" + _fileName);
		}
		oldFile.delete();
		Log.i(TAG, "file " + _fileName + " is outdated and was deleted");
	}
	
	private String getFileExtension(String _fileName){
		int lastIndex = _fileName.lastIndexOf(".");
		if(lastIndex < 0){
			lastIndex = _fileName.length();
		}
		String fileExtension = _fileName.substring(lastIndex,_fileName.length());
		fileExtension = fileExtension.toLowerCase();
		return fileExtension;
	}
	
	private void downloadFiles(ArrayList<File> _files, Context _context){
		InputStream inputStream = null;
		OutputStream outputStream = null;
		 try {
			java.io.File rootFolder = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER); 
			
			//create directories
			java.io.File folder = new java.io.File(rootFolder,SyncHandler.NOTIFICATION_FOLDER);
			folder.mkdirs();
			folder = new java.io.File(rootFolder,SyncHandler.FILE_FOLDER);
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
						java.io.File outputFile;
						
						String fileExtension = _files.get(i).getOriginalFilename();
						fileExtension = getFileExtension(fileExtension);
						
						//is a notification
						if(fileExtension.equals(SyncHandler.NOTIFICATION_FILE_EXTENSION)){
							outputFile = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER + "/" + 
									SyncHandler.NOTIFICATION_FOLDER, _files.get(i).getOriginalFilename());
							Log.i(TAG, "saved in notification folder, because extension is: " + fileExtension);
						//is any other file
						} else {
							outputFile = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER + "/" + 
									SyncHandler.FILE_FOLDER, _files.get(i).getOriginalFilename());
							Log.i(TAG, "saved in file folder, because extension is: " + fileExtension);
						}
						
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
			e.printStackTrace();
		}
	}
}


