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
	private static final String TRIGGERED_NOTIFICATIONS = "triggered_notifications";
	
	private int mCurrentYear;
	private int mCurrentMonth;
	private int mCurrentDay;
	private int mCurrentHours;
	private int mCurrentMinutes;
	
	private ArrayList<Notification> mNotifications;

	/**
	 * Reloads the notification list of the service.
	 */
	public void reload(){
		//TODO these are just some test notifications, the real notifications need to be loaded from xml files here
		mNotifications = new ArrayList<Notification>();
		String[] xmlList = getFilesDir().list();
		XmlParser parser = new XmlParser(getApplicationContext());
		Notification noti;
		try {
			for (String file : xmlList) {
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
//		noti.setUniqueID(12);
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
//		noti.setUniqueID(13);
////		mNotifications.add(noti);
//		
//		creator = new XmlCreator();
//		creator.create(noti, getApplicationContext());
	}
	
	public void registerNotificationAlarms(){
		reload();
		for(int i = 0; i < mNotifications.size(); i++){
			mNotifications.get(i).registerAlarm(getApplicationContext());
		}
	}
	
	/**
	 * Compares all the notifications inside the notification list and displays all matching notifications.
	 */
	public void compare(){
		Log.i(TAG, "comparison called");
		DateTime currentDate = new DateTime(mCurrentYear, mCurrentMonth, mCurrentDay, mCurrentHours, mCurrentMinutes);
		SharedPreferences triggeredNotifications = getSharedPreferences(TRIGGERED_NOTIFICATIONS, 0);
		
		for(int i = 0; i < mNotifications.size(); i++){ 
			//checks if the notification should be triggered at the current time and if it hasn't been triggered before
			Log.i(TAG, "CHECKING UNIQUE ID: " + mNotifications.get(i).getUniqueIDString());
			if(compareDates(currentDate, mNotifications.get(i)) 
					&& !triggeredNotifications.contains(mNotifications.get(i).getUniqueIDString())){
				Log.i(TAG, "matching notification: " + mNotifications.get(i).getTitle());
				mNotifications.get(i).showNotification(getApplicationContext());
				triggeredNotifications.edit().putBoolean(mNotifications.get(i).getUniqueIDString(), true).commit();
			} else {
				Log.i(TAG, "notification does not match or has already been showed");
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
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onHandleIntent(Intent _intent) {
		
		
		if(_intent.getAction() != null && _intent.getAction().equals(Notification.ACTION_ALARM)){
			//sets the current time every time an intent is sent
			mCurrentYear = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
			//please note: the months start with 0 (= January)
			mCurrentMonth = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MONTH)));
			++mCurrentMonth;
			mCurrentDay = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
			mCurrentHours = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
			mCurrentMinutes = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MINUTE)));
			
			Log.i(TAG, "date set to: " + mCurrentYear + "/" + mCurrentMonth + "/" + mCurrentDay + " " + mCurrentHours + ":" + mCurrentMinutes);
			
			Log.i(TAG, "current time set and comparison started");
			reload();
			compare();
		}
		
		else if(_intent.getAction() != null && _intent.getAction().equals("bla")){
			registerNotificationAlarms();
			Log.i(TAG, "notifications registered");
		}

	}
	

}
