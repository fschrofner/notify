package at.fhhgb.mc.notify.notification;

import java.util.ArrayList;
import java.util.Calendar;

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
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//sets the current time every time an intent is sent
		mCurrentYear = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
		mCurrentMonth = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MONTH)));
		mCurrentDay = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
		mCurrentHours = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
		mCurrentMinutes = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MINUTE)));
		
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Reloads the notification list of the service.
	 */
	public void reload(){
		
	}
	
	/**
	 * Compares all the notifications inside the notification list and displays all matching notifications.
	 */
	public void compare(){
		for(int i=0;i<mNotifications.size();i++){
			if(compareYear(mNotifications.get(i))){
				if(compareMonth(mNotifications.get(i))){
					
				} else {
					Log.i(TAG, "month not matching for: " + mNotifications.get(i).getTitle());
				}
			} else {
				Log.i(TAG, "year not matching for: " + mNotifications.get(i).getTitle());
			}
		}
	}
	
	/**
	 * Checks if the current year would fit to the year range set in the notification.
	 * @param _notification the notification you want to check
	 * @return true = it matches, false = it does not
	 */
	private boolean compareYear(Notification _notification){
		if(_notification.getStartYear() == -1 && _notification.getEndYear() == -1){
			return true;
		} else if(_notification.getStartYear() >= mCurrentYear && 
				(_notification.getEndYear() == -1 || _notification.getEndYear() <= mCurrentYear)){
			return true;
		} else {
			return false;
		}
	}
	
	
	/**
	 * Checks if the current month matches the month specified inside the notification.
	 * @param _notification the notification you want to check
	 * @return true = it matches, false = it does not
	 */
	private boolean compareMonth(Notification _notification){	
		//checks if the notification should start in the current year and month
		//and if the end year either is either higher than the current year or the end month is not reached yet
		if(_notification.getStartYear() == mCurrentYear && _notification.getStartMonth() >= mCurrentMonth &&
				((_notification.getEndYear() > mCurrentYear) || 
						(_notification.getEndYear() == mCurrentYear && _notification.getEndMonth() <= mCurrentMonth))){
			return true;
		//checks if the current year is inbetween the start and endyear
		} else if(_notification.getStartYear() < mCurrentYear && _notification.getEndYear() > mCurrentYear){
			return true;
		//checks if the startyear is earlier than the current year and if the end year is equal to the current year
		//and the end month mathes the current month
		} else if(_notification.getStartYear() < mCurrentYear && _notification.getEndYear() == mCurrentYear 
				&& _notification.getEndMonth() >= mCurrentMonth){
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
