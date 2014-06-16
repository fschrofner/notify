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
		
		DriveHandler.setup(mContext);
		
		String folderId = DriveFolder.checkFolder(mContext);
		
		
		//at.fhhgb.mc.notify.sync.drive.DriveHandler.service = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
		//		new GsonFactory(), at.fhhgb.mc.notify.sync.drive.DriveHandler.credential).build();
		try {
			ArrayList<File> hostFiles = getFileList(folderId);
			downloadFiles(getMissingFiles(hostFiles, mContext),mContext);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	private ArrayList<File> getFileList(String _folderId) throws IOException{	
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
		oldFile = new java.io.File(SyncHandler.getFullPath(_fileName));
		if(SyncHandler.getFileExtension(_fileName).equals(SyncHandler.NOTIFICATION_FILE_EXTENSION)){
			//TODO get associated files for notification and check if notification is really outdated
		}	
		oldFile.delete();
		Log.i(TAG, "file " + _fileName + " is outdated and was deleted");
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
						
						String fullPath = SyncHandler.getFullPath(_files.get(i).getOriginalFilename());
						
						outputFile = new java.io.File(fullPath);
						
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


