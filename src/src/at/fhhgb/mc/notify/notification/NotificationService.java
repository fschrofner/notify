package at.fhhgb.mc.notify.notification;

import java.io.File;
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
import at.fhhgb.mc.notify.xml.XmlCreator;
import at.fhhgb.mc.notify.xml.XmlParser;

public class NotificationService extends IntentService {

	public NotificationService(String name) {
		super(name);
		Log.i(TAG, "service started");
		// TODO Auto-generated constructor stub
	}
	
	public NotificationService() {
		super("name");
		Log.i(TAG, "service started");
		// TODO Auto-generated constructor stub
	}

	private static final String TAG = "NotificationService";
	public static final String TRIGGERED_NOTIFICATIONS = "triggered_notifications";
	
	private ArrayList<Notification> mNotifications;
	public static ArrayList<Notification> mTriggeredNotifications;
	public static ArrayList<Notification> mFutureNotifications;

	/**
	 * Reloads the notification list of the service.
	 */
	private void reload(){
		//TODO these are just some test notifications, the real notifications need to be loaded from xml files here
		mNotifications = new ArrayList<Notification>();
//		String[] xmlList = getFilesDir().list();
//		java.io.File fileFolder = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER + "/" + SyncHandler.NOTIFICATION_FOLDER);
//		boolean returnV = fileFolder.mkdirs();
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		Notification noti = new Notification("Test Nr. 1", "This is the first notification");
//		noti.setStartYear(2014);
//		noti.setStartMonth(5);
//		noti.setStartDay(8);
//		noti.setStartHours(8);
//		noti.setStartMinutes(0);
//		noti.setUniqueID(11);
////		mNotifications.add(noti);
//		XmlCreator creator = new XmlCreator();
//		creator.create(noti, getApplicationContext());
//		
//		noti = new Notification("Test Nr. 2", "This is the second notification");
//		noti.setStartYear(2014);
//		noti.setStartMonth(5);
//		noti.setStartDay(8);
//		noti.setStartHours(9);
//		noti.setStartMinutes(20);
//		noti.setUniqueID(12);
////		mNotifications.add(noti);
//		creator = new XmlCreator();
//		creator.create(noti, getApplicationContext());
//		
//		noti = new Notification("Test Nr. 3", "This is the third notification");
//		noti.setStartYear(-1);
//		noti.setStartMonth(-1);
//		noti.setStartDay(-1);
//		noti.setStartHours(-1);
//		noti.setStartMinutes(-1);
//		noti.setUniqueID(13);
////		mNotifications.add(noti);
//		creator = new XmlCreator();
//		creator.create(noti, getApplicationContext());
//		
//		noti = new Notification("Test Nr. 4", "This is the fourth notification");
//		noti.setStartYear(2015);
//		noti.setStartMonth(5);
//		noti.setStartDay(8);
//		noti.setStartHours(9);
//		noti.setStartMinutes(20);
//		noti.setUniqueID(14);
////		mNotifications.add(noti);
//		creator = new XmlCreator();
//		creator.create(noti, getApplicationContext());
//		
//		noti = new Notification("Test Nr. 5", "This is the fifth notification");
//		noti.setStartYear(2014);
//		noti.setStartMonth(5);
//		noti.setStartDay(8);
//		noti.setStartHours(9);
//		noti.setStartMinutes(20);
//		noti.setEndYear(2014);
//		noti.setEndMonth(5);
//		noti.setEndDay(9);
//		noti.setEndHours(9);
//		noti.setEndMinutes(20);
//		noti.setUniqueID(15);
////		mNotifications.add(noti);
//		creator = new XmlCreator();
//		creator.create(noti, getApplicationContext());
	}
	
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
//		DateTime currentDate = new DateTime(mCurrentYear, mCurrentMonth, mCurrentDay, mCurrentHours, mCurrentMinutes);
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
		
//		for(int i = 0; i < mNotifications.size(); i++){ 
//			//checks if the notification should be triggered at the current time and if it hasn't been triggered before
//			Log.i(TAG, "CHECKING UNIQUE ID: " + mNotifications.get(i).getUniqueIDString());
//			if(compareDates(currentDate, mNotifications.get(i)) 
//					&& !triggeredNotifications.contains(mNotifications.get(i).getUniqueIDString())){
//				Log.i(TAG, "matching notification: " + mNotifications.get(i).getTitle());
//				mNotifications.get(i).showNotification(getApplicationContext());
//				triggeredNotifications.edit().putBoolean(mNotifications.get(i).getUniqueIDString(), true).commit();
//			} else {
//				Log.i(TAG, "notification does not match or has already been showed");
//			}
//		}
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
		
		SharedPreferences triggeredNotifications = getSharedPreferences(TRIGGERED_NOTIFICATIONS, 0);
		triggeredNotifications.edit().remove(String.valueOf(_uniqueID)).commit();
		
		Notification.cancel(this, _notificationID);
		
		Log.i(TAG, "notification " + _uniqueID + "_" + _version + " dismissed");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onHandleIntent(Intent _intent) {
		Log.i(TAG, "received intent");
		
		if(_intent.getAction() != null && _intent.getAction().equals(Notification.ACTION_ALARM)){
			showNotifications();
		}
		
		else if(_intent.getAction() != null && _intent.getAction().equals(Notification.ACTION_START_SERVICE)){
			registerNotificationAlarms();
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
