package at.fhhgb.mc.notify.notification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NotificationService extends Service {

	static final String TAG = "NotificationService";
	
	private int mCurrentYear;
	private int mCurrentMonth;
	private int mCurrentDay;
	private int mCurrentHours;
	private int mCurrentMinutes;
	
	private ArrayList<Notification> mNotifications;
	
	@Override
	public void onCreate() {
		reload();
		super.onCreate();
		Log.i(TAG, "service started");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//sets the current time every time an intent is sent
		mCurrentYear = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
		
		//please note: the months start with 0 (= January)
		mCurrentMonth = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MONTH)));
		mCurrentDay = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
		mCurrentHours = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
		mCurrentMinutes = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MINUTE)));
		
		Log.i(TAG, "date set to: " + mCurrentYear + "/" + mCurrentMonth + 1 + "/" + mCurrentDay + " " + mCurrentHours + ":" + mCurrentMinutes);
		
		compare();
		Log.i(TAG, "current time set and comparison started");
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Reloads the notification list of the service.
	 */
	public void reload(){
		mNotifications = new ArrayList<Notification>();
		Notification noti = new Notification("Test Nr. 1", "This is the first notification");
		noti.setStartYear(2014);
		noti.setStartMonth(4);
		noti.setStartDay(6);
		noti.setStartHours(22);
		noti.setStartMinutes(50);
		mNotifications.add(noti);
	}
	
	/**
	 * Compares all the notifications inside the notification list and displays all matching notifications.
	 */
	public void compare(){
		Log.i(TAG, "comparison called");
		DateTime currentDate = new DateTime(mCurrentYear, mCurrentMonth, mCurrentDay, mCurrentHours, mCurrentMinutes);
		for(int i=0;i<mNotifications.size();i++){ 
			if(compareDates(currentDate,mNotifications.get(i))){
				mNotifications.get(i).showNotification(getApplicationContext());
				Log.i(TAG, "matching notification: " + mNotifications.get(i).getTitle());
			} else {
				Log.i(TAG, "notification does not match");
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
		
		if(_notification.getStartYear() == -1 || (dateRange.get(0).isBefore(_date) && _notification.getEndYear() == -1)){
			return true;
		} else if(_date.isAfter(dateRange.get(0)) && _date.isBefore(dateRange.get(1))){
			return true;
		} else if(_date.isEqual(dateRange.get(0)) && _date.isBefore(dateRange.get(1))){
			return true;
		} else if(_date.isAfter(dateRange.get(0)) && _date.isEqual(dateRange.get(1))){
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
	

}
