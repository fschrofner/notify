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
		
		if(!preferences.contains(SyncHandler.GOOGLE_DRIVE_FOLDER) && !preferences.getString(SyncHandler.GOOGLE_DRIVE_FOLDER, "").equals("")){
			//TODO first check if the folder exists here (created by other application)
			createFolder();
		}
		
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
	
	private String checkFolder(){
		//TODO check if there is already a notify folder in google drive
		return null;
	}
	
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
				preferences.edit().putString(SyncHandler.GOOGLE_DRIVE_FOLDER, folderId);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private ArrayList<File> getFileList(String _folderId) throws IOException{		
		Files.List request = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().list();
		//TODO get only file list from notify folder
		//at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().get(_folderId).
		ArrayList<File> fileList = new ArrayList<File>();
		FileList files;
		
		do {
			try {
				files = request.execute();
				fileList.addAll(files.getItems());
				request.setPageToken(files.getNextPageToken());
				for(int i = 0; i<fileList.size();i++){
					Log.i(TAG, "got: " + fileList.get(i).getOriginalFilename());
				}			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (request.getPageToken() != null && 
				request.getPageToken().length() > 0);	
		return fileList;
	}
	
	private ArrayList<File> getMissingFiles(ArrayList<File> _hostFiles,Context _context){
		ArrayList<File> missingFiles = new ArrayList<File>();
		java.io.File folder = new java.io.File(_context.getFilesDir()+"/"+SyncHandler.NOTIFICATION_FOLDER); 
		//TODO crash when folder not existent 
		//String[] fileList = folder.list();
		String[] fileList = {"abc"};
		List<String> localFiles = Arrays.asList(fileList);
		
		for(int i=0;i<_hostFiles.size();i++){
			if(!localFiles.contains(_hostFiles.get(i))){
				missingFiles.add(_hostFiles.get(i));
				//TODO if there's a new revision of a file (if the filename contains a "_"
				//and has a newer revision number) an update needs to take place
			}
		}
		return missingFiles;
	}
	
	private void downloadFiles(ArrayList<File> _files, Context _context){
		InputStream inputStream = null;
		OutputStream outputStream = null;
		 try {
			 
			java.io.File folder = new java.io.File(_context.getFilesDir(),SyncHandler.NOTIFICATION_FOLDER);
			folder.mkdir();
			
			for(int i=0;i<_files.size();i++){
				HttpResponse resp = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.getRequestFactory().
						 buildGetRequest(new GenericUrl(_files.get(i).getDownloadUrl())).execute();
				inputStream = resp.getContent();
				outputStream = new FileOutputStream(new java.io.File(mContext.getFilesDir() + "/" + 
						SyncHandler.NOTIFICATION_FOLDER, _files.get(i).getOriginalFilename()));
				int read = 0;
				byte[] bytes = new byte[1024];
		 
				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
				Log.i(TAG, "file complete!");
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO organise files in sub-folders
	}
}


