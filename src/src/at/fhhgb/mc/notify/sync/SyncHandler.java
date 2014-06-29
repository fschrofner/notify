package at.fhhgb.mc.notify.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import at.fhhgb.mc.notify.push.PushConstants;
import at.fhhgb.mc.notify.push.PushSender;
import at.fhhgb.mc.notify.sync.drive.DriveHandler;

public class SyncHandler {
	final static String TAG = "SyncHandler";
	final static public String NOTIFICATION_FOLDER = "notifications";
	final static public String FILE_FOLDER = "notification_files";
	final static public String HOST_FOLDER = "Notify";
	final static public String HOST_DESCRIPTION = "A folder used for notify syncing";
	final static public String GOOGLE_DRIVE = "google_drive";
	final static public String GOOGLE_DRIVE_FOLDER = "google_drive_folder_id";
	final static public String ROOT_NOTIFICATION_FOLDER = Environment.getExternalStorageDirectory().toString() + "/Notify";
	final static public String NOTIFICATION_FILE_EXTENSION = "noti";
	final static public String UPLOAD_FILE_LIST = "filelist";
	final static public String APPLICATION_NAME = "Notify";
	
	//the name of the shared preferences which save outstanding tasks
	final static public String OUTSTANDING_TASKS = "outstanding_tasks";
	
	//the names for outstanding tasks inside the shared preferences
	final static public String OUTSTANDING_PUSH = "push";
	final static public String OUTSTANDING_DOWNLOAD = "download";
	final static public String OUTSTANDING_UPLOAD = "upload";
	final static public String OUTSTANDING_DELETION = "delete";
	final static public String OUTSTANDING_PUSH_REGISTRATION = "push_registration";
	
	/**
	 * Initiates an update 
	 */
	static public void updateFiles(Context _context, Activity _activity){
		//TODO  differentiate the different hosts
		DriveHandler.updateFiles(_context, _activity);
	}
	
	static public void uploadFiles(Context _context,Activity _activity, ArrayList<String> _fileList){
		//TODO differentiate different hosts
		DriveHandler.uploadFiles(_context, _activity, _fileList);
	}
	
	static public void sendPush(Context _context){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);	
		//TODO set push alias in sharedpreferences before reading
		String alias = preferences.getString(PushConstants.PUSH_ALIAS, null);
		PushSender.sendPushToAlias(alias,_context);
	}
	/**
	 * Checks if the device is currently connected to the internet.
	 * @param _context context used for the check
	 * @return true = connected, false = no connection
	 */
	static public boolean networkConnected(Context _context){
		ConnectivityManager cm = (ConnectivityManager)_context.getSystemService(Context.CONNECTIVITY_SERVICE);    	 
    	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    	boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    	Log.i(TAG, "current network status: internet connectivity = " + isConnected);
    	return isConnected;
	}
	/**
	 * Deletes the given giles from the local file system AND from the host.
	 * @param _context context needed for some methods
	 * @param _activity activity needed to show authentication activity, when there is an authentication error
	 * @param _fileNames the names of the files you want to delete
	 */
	static public void deleteFiles(Context _context, Activity _activity, ArrayList<String> _fileNames){
		//TODO differentiate different hosts
		DriveHandler.deleteFiles(_context, _activity, _fileNames);
	}
	
	static private ArrayList<String> removeRevisions(ArrayList<String> _files){
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0;i<_files.size();i++){
			result.add(_files.get(i).substring(_files.get(i).lastIndexOf("_")));
		}
		return result;
	}
	
	public static String getFullPath(String _fileName){
		String fullPath;
		String fileExtension = getFileExtension(_fileName);
		if(fileExtension.equals(SyncHandler.NOTIFICATION_FILE_EXTENSION)){
			fullPath = SyncHandler.ROOT_NOTIFICATION_FOLDER+"/"+SyncHandler.NOTIFICATION_FOLDER + "/" + _fileName;
			Log.i(TAG, "file in notification folder, because extension is: " + fileExtension);
		} else {
			fullPath = SyncHandler.ROOT_NOTIFICATION_FOLDER+"/"+SyncHandler.FILE_FOLDER + "/" + _fileName;
			Log.i(TAG, "file in file folder, because extension is: " + fileExtension);
		}
		return fullPath;
	}
	
	public static String getFileNameWithoutExtension(String _fileName){
		int lastIndex = _fileName.lastIndexOf(".") + 1;
		if(lastIndex < 0){
			lastIndex = _fileName.length();
		}
		String fileName = _fileName.substring(0,lastIndex);
		return fileName;
	}
	
	public static DateTime getCurrentSystemTime(){
		Calendar calendar = Calendar.getInstance(); 
		int minutes = calendar.get(Calendar.MINUTE);
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		++month;
		int year = calendar.get(Calendar.YEAR);
		DateTime date = new DateTime(year,month,day,hours,minutes);
		return date;
	}
	
	public static String getFileExtension(String _fileName){
		int lastIndex = _fileName.lastIndexOf(".") + 1;
		if(lastIndex < 0){
			lastIndex = _fileName.length();
		}
		String fileExtension = _fileName.substring(lastIndex,_fileName.length());
		fileExtension = fileExtension.toLowerCase();
		return fileExtension;
	}
}
