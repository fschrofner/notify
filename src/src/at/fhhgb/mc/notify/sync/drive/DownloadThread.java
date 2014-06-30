package at.fhhgb.mc.notify.sync.drive;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import at.fhhgb.mc.notify.MainActivity;
import at.fhhgb.mc.notify.notification.Notification;
import at.fhhgb.mc.notify.push.PushConstants;
import at.fhhgb.mc.notify.sync.SyncHandler;
import at.fhhgb.mc.notify.xml.XmlParser;

public class DownloadThread implements Runnable {

	final static String TAG = "DownloadThread";
	private Context mContext;
	private boolean mDownloadFinished;
	private boolean mConnected;
	private Activity mActivity;
	
	public DownloadThread(Context _context, Activity _activity){
		mContext = _context;
		mDownloadFinished = false;
		mConnected = true;
		mActivity = _activity;
	}
	
	@Override
	public void run() {
		Log.i(TAG, "started download thread");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);		
		
		DriveHandler.setup(mContext);
		
		if(DriveHandler.service != null){
			String folderId = DriveFolder.checkFolder(mContext,mActivity);
			
			
			//at.fhhgb.mc.notify.sync.drive.DriveHandler.service = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
			//		new GsonFactory(), at.fhhgb.mc.notify.sync.drive.DriveHandler.credential).build();
			if(folderId != null){
				try {
					ArrayList<File> hostFiles = DriveHandler.getFileList(folderId);
					downloadFiles(getMissingFiles(hostFiles, mContext),mContext);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Log.i(TAG, "currently no internet connection! scheduled download");
				SharedPreferences outstanding = mContext.getSharedPreferences(SyncHandler.OUTSTANDING_TASKS, Context.MODE_PRIVATE); 
				outstanding.edit().putBoolean(SyncHandler.OUTSTANDING_DOWNLOAD, true).commit();
			}

		} else {
			Log.i(TAG, "no drive service running!");
		}
		
		//refresh the notification fragment afterwards
		if(mActivity != null){
			Log.i(TAG, "finished deleting, trying to refresh fragments now.");
			//TODO check if activity is castable to MainActivity
			((MainActivity)mActivity).refreshFragments();
		}
		
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
		
		//TODO only delete notification files (and their added files)
		if(SyncHandler.getFileExtension(_fileName).equals(SyncHandler.NOTIFICATION_FILE_EXTENSION)){
			//TODO get associated files for notification and check if notification is really outdated
			//upload it otherwise
			
			try {
				XmlParser parser = new XmlParser(mContext);
				Notification notification = parser.readXml(_fileName);

				if(notification != null){
					
					//TODO check if notification is still valid
					//TODO if yes check for revisions
					
					ArrayList<DateTime> dates = notification.getDates();
					if(dates.get(1) != null && dates.get(1).isBeforeNow() ){
						ArrayList<String> files = notification.getFiles();
						
						if(files != null){
							for(int i=0;i<files.size();i++){
								SyncHandler.deleteFiles(mContext, null, files);
//								java.io.File file = new java.io.File(SyncHandler.getFullPath(files.get(i)));
//								file.delete();
							}
						}
					}
				}
				
				oldFile = new java.io.File(SyncHandler.getFullPath(_fileName));
				oldFile.delete();
				Log.i(TAG, "file " + _fileName + " is outdated and was deleted");
								
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		

	}
	
	private void downloadFiles(ArrayList<File> _files, Context _context){
			java.io.File rootFolder = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER); 
			
			//create directories
			java.io.File folder = new java.io.File(rootFolder,SyncHandler.NOTIFICATION_FOLDER);
			folder.mkdirs();
			folder = new java.io.File(rootFolder,SyncHandler.FILE_FOLDER);
			folder.mkdirs();
			
			for(int i=0;i<_files.size();i++){
				//check for correct file
				if(_files.get(i) != null){
					mDownloadFinished = false;
					do{
						Log.i(TAG, "tried downloading file: " + i);
						downloadFile(_files.get(i),_context);	
						if(!mDownloadFinished){
							try {
								Log.i(TAG, "download failed! waiting for 10 seconds and retry");
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if(!mConnected){
							//schedules the download until connectivity state changes
							Log.i(TAG, "currently no internet connection! scheduled download");
							SharedPreferences outstanding = _context.getSharedPreferences(SyncHandler.OUTSTANDING_TASKS, Context.MODE_PRIVATE); 
							outstanding.edit().putBoolean(SyncHandler.OUTSTANDING_DOWNLOAD, true).commit();
							break;
						}
					} while(!mDownloadFinished);
				}
		
			}		
	}
	
	private void downloadFile(File _file, Context _context){
		try {
			InputStream inputStream = null;
			OutputStream outputStream = null;
			
			String downloadLink = _file.getDownloadUrl(); 
			
			//check if the file is downloadable
			if(downloadLink != null){
				GenericUrl downloadUrl = new GenericUrl(downloadLink);
				Log.i(TAG, "downloadlink created: " + downloadLink.toString());
				HttpResponse resp;
					resp = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.getRequestFactory().
							 buildGetRequest(downloadUrl).execute();
	
				inputStream = resp.getContent();
				java.io.File outputFile;
				
				String fullPath = SyncHandler.getFullPath(_file.getOriginalFilename());
				
				outputFile = new java.io.File(fullPath);
				
				outputFile.createNewFile();
				outputStream = new FileOutputStream(outputFile);
				int read = 0;
				byte[] bytes = new byte[1024];
				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
				
				Log.i(TAG, "file " + _file.getOriginalFilename() + " complete!");
				mDownloadFinished = true;
			} 
		} catch (UserRecoverableAuthIOException e) {
			if(mActivity != null){
				mActivity.startActivityForResult(e.getIntent(), AuthenticationActivity.REQUEST_AUTHENTICATION);
			} else {
				Log.w(TAG, "authentication exception but no activity given!");
			}
			  
		} catch (IOException e) {
			Log.w(TAG, "network error!");
			mConnected = false;
		}
	}
}



