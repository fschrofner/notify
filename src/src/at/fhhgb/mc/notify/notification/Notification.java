package at.fhhgb.mc.notify.notification;

import java.util.ArrayList;

import org.joda.time.DateTime;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import at.fhhgb.mc.notify.R;

/**
 * Container class that is used to transfer notifications between methods.
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */

public class Notification {
	final static String TAG = "Notification";
	final static String ACTION_ALARM = "at.fhhgb.mc.notify.notification.NOTIFICATION_ALARM";
	
	private String mTitle;
	private int mStartYear;
	private int mStartMonth;
	private int mStartDay;
	private int mEndYear;
	private int mEndMonth;
	private int mEndDay;
	private int mStartHours;
	private int mStartMinutes;
	private int mEndHours;
	private int mEndMinutes;
	private String mMessage;
	private ArrayList<String> mFiles;
	private long mUniqueID;
	
	//static notification id makes sure that each id is unique
	//so that multiple notification don't get concatenated
	static int mNotificationID = 0;
	
	public Notification(){
		initialiseDate();
	}
	
	public Notification(String _title, String _message){
		initialiseDate();
		mTitle = _title;
		mMessage = _message;
	}
	
	private void initialiseDate(){
		mStartYear = -1;
		mStartMonth = -1;
		mStartDay = -1;
		mStartHours = -1;
		mStartMinutes = -1;
		mEndYear = -1;
		mEndMonth = -1;
		mEndDay = -1;
		mEndHours = -1;
		mEndMinutes = -1;
	}
	
	/**
	 * Shows the notification.
	 */
	public void showNotification(Context _context){
		
        NotificationManager notificationManager = (NotificationManager)_context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(_context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(this.getTitle())
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(this.getMessage()))
                        .setContentText(this.getMessage());
        notificationManager.notify(mNotificationID, mBuilder.build());
        
        mNotificationID++;
        Log.i(TAG, "notification built");
        
	}
	
	public ArrayList<DateTime> getDates(){
		ArrayList<DateTime> dates = new ArrayList<DateTime>(2);
		if(mStartYear != -1){
			dates.add(new DateTime(mStartYear, mStartMonth, mStartDay, mStartHours, mStartMinutes));
		} else {
			dates.add(null);
		}
		if(mEndYear != -1){
			dates.add(new DateTime(mEndYear, mEndMonth, mEndDay, mEndHours, mEndMinutes));
		} else {
			dates.add(null);
		}
		return dates;
	}
	
	public void registerAlarm(Context _context){
		AlarmManager alarmManager = (AlarmManager)_context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(_context, NotificationBroadcastReceiver.class);
		intent.setAction(ACTION_ALARM);
		ArrayList<DateTime> dates = getDates();
		
		for(int i = 0; i < dates.size(); i++){
			if(dates.get(i) != null){
				//the data gets set here so that the intents become different and will not overwrite each other
				intent.setData(Uri.parse(dates.get(i).toString()));
				PendingIntent pendingIntent= PendingIntent.getBroadcast(_context, 0, intent, 0);
				alarmManager.set(AlarmManager.RTC_WAKEUP, dates.get(i).getMillis(), pendingIntent);
				Log.i(TAG, "set notification alarm at: " + dates.get(i).toString());
			}
		}

	}
	
	/**
	 * Generates a unique id to use with a notification.
	 * The first value is a random number between 0 and 99, then the current time in ms is concatenated.
	 * @return a unique id for your notification
	 */
	public static long generateUniqueID(){
		byte first = (byte)(Math.random() * 99);
		long id = Long.parseLong(Byte.toString(first) + System.currentTimeMillis());
		Log.i(TAG, "generated unique id: " + id);
		return id;
	}
	
	public String getUniqueIDString(){
		return Long.toString(mUniqueID);
	}
	
	public String getTitle() {
		return mTitle;
	}
	public void setTitle(String title) {
		this.mTitle = title;
	}
	public int getStartYear() {
		return mStartYear;
	}
	public void setStartYear(int startYear) {
		this.mStartYear = startYear;
	}
	public int getStartMonth() {
		return mStartMonth;
	}
	public void setStartMonth(int startMonth) {
		this.mStartMonth = startMonth;
	}
	public int getStartDay() {
		return mStartDay;
	}
	public void setStartDay(int startDay) {
		this.mStartDay = startDay;
	}
	public int getEndYear() {
		return mEndYear;
	}
	public void setEndYear(int endYear) {
		this.mEndYear = endYear;
	}
	public int getEndMonth() {
		return mEndMonth;
	}
	public void setEndMonth(int endMonth) {
		this.mEndMonth = endMonth;
	}
	public int getEndDay() {
		return mEndDay;
	}
	public void setEndDay(int endDay) {
		this.mEndDay = endDay;
	}
	public int getStartHours() {
		return mStartHours;
	}
	public void setStartHours(int startHours) {
		this.mStartHours = startHours;
	}
	public int getStartMinutes() {
		return mStartMinutes;
	}
	public void setStartMinutes(int startMinutes) {
		this.mStartMinutes = startMinutes;
	}
	public int getEndHours() {
		return mEndHours;
	}
	public void setEndHours(int endHours) {
		this.mEndHours = endHours;
	}
	public int getEndMinutes() {
		return mEndMinutes;
	}
	public void setEndMinutes(int endMinutes) {
		this.mEndMinutes = endMinutes;
	}
	public String getMessage() {
		return mMessage;
	}
	public void setMessage(String message) {
		this.mMessage = message;
	}
	public ArrayList<String> getFiles() {
		return mFiles;
	}
	public void setFiles(ArrayList<String> files) {
		this.mFiles = files;
	}
	public long getUniqueID() {
		return mUniqueID;
	}
	public void setUniqueID(long mUniqueID) {
		this.mUniqueID = mUniqueID;
	}
}
