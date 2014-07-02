package at.fhhgb.mc.notify.sync;

import java.util.ArrayList;
import java.util.Calendar;

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

/**
 * General class used to provide static methods to interchange data
 * between all linked devices using the specified host.
 * @author Dominik Koeltringer & Florian Schrofner
 *
 */
public class SyncHandler {
	final static String TAG = "SyncHandler";
	
	//constants used for the local file system
	final static public String NOTIFICATION_FOLDER = "notifications";
	final static public String FILE_FOLDER = "notification_files";
	final static public String ROOT_NOTIFICATION_FOLDER = Environment.getExternalStorageDirectory().toString() + "/Notify";
	final static public String NOTIFICATION_FILE_EXTENSION = "noti";
	
	final static public String UPLOAD_FILE_LIST = "filelist";

	
	//constants used for google drive
	final static public String APPLICATION_NAME = "Notify";
	final static public String HOST_FOLDER = "Notify";
	final static public String HOST_DESCRIPTION = "A folder used for notify syncing";
	final static public String GOOGLE_DRIVE = "google_drive";
	final static public String GOOGLE_DRIVE_FOLDER = "google_drive_folder_id";
	
	//extras that can be specified inside intent extras
	final static public String EXTRA_FILE_LIST = "at.fhhgb.mc.notify.sync.SyncHandler.FILE_LIST";
	final static public String EXTRA_FILE_LIST_DELETE = "at.fhhgb.mc.notify.sync.SyncHandler.FILE_LIST_DELETE";
	final static public String EXTRA_TITLE_LIST = "at.fhhgb.mc.notify.sync.SyncHandler.TITLE_LIST";
	
	//the name of the shared preferences which save outstanding tasks
	final static public String OUTSTANDING_TASKS = "outstanding_tasks";
	
	//the names for outstanding tasks inside the shared preferences
	final static public String OUTSTANDING_PUSH = "push";
	final static public String OUTSTANDING_DOWNLOAD = "download";
	final static public String OUTSTANDING_UPLOAD = "upload";
	final static public String OUTSTANDING_DELETION = "delete";
	final static public String OUTSTANDING_PUSH_REGISTRATION = "push_registration";
	

	/**
	 * Initiates an update from the host.
	 * @param _context context needed for certain operations
	 * @param _activity no need to be specified (can be null), but allows
	 * the update of the specified activity otherwise
	 */
	static public void updateFiles(Context _context, Activity _activity){
		//TODO  differentiate the different hosts
		DriveHandler.updateFiles(_context, _activity);
	}
	
	/**
	 * Uploads the given files to the hosts.
	 * @param _context context needed for certain operations
	 * @param _activity no need to be specified (can be null), but allows
	 * the update of the specified activity otherwise
	 * @param _fileList the files to be uploaded as filenames inside an arraylist
	 */
	static public void uploadFiles(Context _context,Activity _activity, ArrayList<String> _fileList){
		//TODO differentiate different hosts
		DriveHandler.uploadFiles(_context, _activity, _fileList);
	}
	
	/**
	 * Sends a push to the alias specified inside the default shared preferences.
	 * @param _context context needed for certain operations
	 */
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
	 * Deletes the given files from the local file system AND from the host.
	 * @param _context context needed for some methods
	 * @param _activity activity needed to show authentication activity, when there is an authentication error
	 * @param _fileNames the names of the files you want to delete
	 */
	static public void deleteFiles(Context _context, Activity _activity, ArrayList<String> _fileNames){
		//TODO differentiate different hosts
		DriveHandler.deleteFiles(_context, _activity, _fileNames);
	}
	
	/**
	 * Returns the full path inside the filesystem for the specified filename.
	 * (checks file extension and delivers according path inside folder structure)
	 * @param _fileName the filename of the file of which to get the path
	 * @return the full path of the file
	 */
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
	
	/**
	 * Cuts of the extension of the specified filename.
	 * @param _fileName the filename of which to cut the extension
	 * @return the filename without the extension
	 */
	public static String getFileNameWithoutExtension(String _fileName){
		int lastIndex = _fileName.lastIndexOf(".") + 1;
		if(lastIndex < 0){
			lastIndex = _fileName.length();
		}
		String fileName = _fileName.substring(0,lastIndex);
		return fileName;
	}
	
	
	/**
	 * Returns the current system time as datetime object.
	 * @return the current system time
	 */
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
	
	/**
	 * Returns the file extension of the given filename.
	 * @param _fileName the file name of which to get the extension
	 * @return the file extension (not including the dot)
	 */
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
