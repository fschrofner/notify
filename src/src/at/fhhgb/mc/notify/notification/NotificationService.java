package at.fhhgb.mc.notify.notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.joda.time.DateTime;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import at.fhhgb.mc.notify.sync.SyncHandler;
import at.fhhgb.mc.notify.xml.XmlParser;

/**
 * Service used to compare and display notifications.
 * @author Dominik Koeltringer & Florian Schrofner
 *
 */
public class NotificationService extends IntentService {

	/**
	 * Create a new service with the given name.
	 * @param _name the name of the service
	 */
	public NotificationService(String _name) {
		super(_name);
		Log.i(TAG, "service started");
	}
	
	public NotificationService() {
		super("name");
		Log.i(TAG, "service started");
	}

	private static final String TAG = "NotificationService";
	
	//name of the shared preferences were all previous triggered notifications will be saved
	public static final String TRIGGERED_NOTIFICATIONS = "triggered_notifications";
	
	
	//member variables that hold the notifications
	private ArrayList<Notification> mNotifications;
	public static ArrayList<Notification> mTriggeredNotifications;
	public static ArrayList<Notification> mFutureNotifications;

	/**
	 * Reloads the notification list of the service.
	 */
	private void reload(){
		mNotifications = new ArrayList<Notification>();
		
		java.io.File rootFolder = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER); 
		rootFolder.mkdirs();
		
		//create directory
		java.io.File fileFolder = new java.io.File(rootFolder,SyncHandler.NOTIFICATION_FOLDER);
		fileFolder.mkdirs();
		
		String[] xmlList = fileFolder.list();
		XmlParser parser = new XmlParser(getApplicationContext());
		Notification noti;
		try {
			for (String file : xmlList) {
				Log.i(TAG, "file: " + file);
				noti = parser.readXml(file);
				mNotifications.add(noti);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Registers alarms for every notification, so that the notification service
	 * will be called at the relevant times.
	 */
	private void registerNotificationAlarms(){
		reload();
		for(int i = 0; i < mNotifications.size(); i++){
			mNotifications.get(i).registerAlarm(getApplicationContext());
			Log.i(TAG, "register: " + mNotifications.get(i).getTitle());
		}
	}
	
	/**
	 * Displays all matching notifications.
	 */
	private void showNotifications() {
		Log.i(TAG, "comparison called");
		SharedPreferences triggeredNotifications = getSharedPreferences(TRIGGERED_NOTIFICATIONS, 0);
		
		compareNotifications();
		
		for (int i = 0; i < mTriggeredNotifications.size(); i++) {
			
			Log.i(TAG, "show: " + mTriggeredNotifications.get(i).getTitle());
			
			if (!triggeredNotifications.contains(mTriggeredNotifications.get(i).getUniqueIDString())) {
				Log.i(TAG, "notification to show: " + mTriggeredNotifications.get(i).getTitle());
				mTriggeredNotifications.get(i).showNotification(getApplicationContext());
				triggeredNotifications.edit().putInt(mTriggeredNotifications.get(i).getUniqueIDString(), Notification.mNotificationID).commit();
				Notification.mNotificationID++;
			} else {
				Log.i(TAG, "notification has already been shown");
			}
		}
	}
	
	/**
	 * Compares all the notifications inside the notification list.
	 */
	private void compareNotifications() {
		mTriggeredNotifications = new ArrayList<Notification>();
		mFutureNotifications = new ArrayList<Notification>();
		
		//sets the current time every time an intent is sent
		int currentYear = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
		//please note: the months start with 0 (= January)
		int currentMonth = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MONTH)));
		++currentMonth;
		int currentDay = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
		int currentHours = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
		int currentMinutes = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MINUTE)));
		
		Log.i(TAG, "date set to: " + currentYear + "/" + currentMonth + "/" + currentDay + " " + currentHours + ":" + currentMinutes);	
		Log.i(TAG, "current time set and comparison started");
		
		reload();
		
		DateTime currentDate = new DateTime(currentYear, currentMonth, currentDay, currentHours, currentMinutes);
		
		for(int i = 0; i < mNotifications.size(); i++){ 
			//checks if the notification should be triggered at the current time and if it hasn't been triggered before
			Log.i(TAG, "CHECKING UNIQUE ID: " + mNotifications.get(i).getUniqueIDString());
			if(compareDates(currentDate, mNotifications.get(i))){
				Log.i(TAG, "matching notification: " + mNotifications.get(i).getTitle());
				mTriggeredNotifications.add(mNotifications.get(i));
			} else {
				Log.i(TAG, "notification does not match");
				mFutureNotifications.add(mNotifications.get(i));
			}
		}
	}

	/**
	 * Checks if the given date is inside the given date range.
	 * @param _date the date you want to check
	 * @param _dateRange the date range to which it should be compared
	 * @return true = date inside range, false = date outside the range
	 */
	private boolean compareDates(DateTime _date, Notification _notification){
		ArrayList<DateTime>dateRange = _notification.getDates();
		
		if((_notification.getStartYear() == -1 && _notification.getEndYear() == -1) || (dateRange.get(0).isBefore(_date) && _notification.getEndYear() == -1)){
			Log.i(TAG, "no start year, or start year was before and no end year specified");
			return true;
		} else if((_date.isAfter(dateRange.get(0)) || _date.isEqual(dateRange.get(0))) && 
				(_date.isBefore(dateRange.get(1)) || _date.isEqual(dateRange.get(1)))){
			Log.i(TAG, "start date and end date specified, current time is inbetween or equal");
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Deletes a notification
	 * @param _uniqueID The ID of the notification
	 * @param _version The Version of the notification
	 * @param _notificationID The notification ID
	 */
	private void deleteNotification(long _uniqueID, int _version, int _notificationID) {
		String fileName = _uniqueID + "_" + _version + "." + SyncHandler.NOTIFICATION_FILE_EXTENSION;
		ArrayList<String> fileNames = new ArrayList<String>();
		fileNames.add(fileName);
		SyncHandler.deleteFiles(this, null, fileNames);
		
		Notification n = new Notification();
		n.setUniqueID(_uniqueID);
		n.cancel(this);
		
		Log.i(TAG, "notification " + _uniqueID + "_" + _version + " dismissed");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	protected void onHandleIntent(Intent _intent) {
		Log.i(TAG, "received intent");
		
		if(_intent.getAction() != null && _intent.getAction().equals(Notification.ACTION_ALARM)){
			Log.i(TAG, "intent: action_alarm");
			showNotifications();
		}
		
		else if(_intent.getAction() != null && _intent.getAction().equals(Notification.ACTION_START_SERVICE)){
			registerNotificationAlarms();
			showNotifications();
			Log.i(TAG, "notifications registered");
		}
		
		else if(_intent.getAction() != null && _intent.getAction().equals(Notification.ACTION_DELETE)) {
			Log.i(TAG, "received notification delete intent!");
			long uniqueID = _intent.getLongExtra(Notification.EXTRA_UNIQUE_ID, 0);
			int version = _intent.getIntExtra(Notification.EXTRA_VERSION, 0);
			int notificationID = _intent.getIntExtra(Notification.EXTRA_NOTIFICATION_ID, 0);
			
			deleteNotification(uniqueID, version, notificationID);
		}
		
		else if(_intent.getAction() != null && _intent.getAction().equals(Notification.ACTION_COMPARE)) {
			compareNotifications();
			Log.i(TAG, "action compare");
		}

	}
	
}
