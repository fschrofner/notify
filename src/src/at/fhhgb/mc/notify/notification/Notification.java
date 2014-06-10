package at.fhhgb.mc.notify.notification;

import java.util.ArrayList;

import org.joda.time.DateTime;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import at.fhhgb.mc.notify.MainActivity;
import at.fhhgb.mc.notify.R;

/**
 * Container class that is used to transfer notifications between methods.
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */

public class Notification {
	private final static String TAG = "Notification";
	
	public final static String ACTION_ALARM = "at.fhhgb.mc.notify.notification.NOTIFICATION_ALARM";
	public final static String ACTION_DISMISS = "at.fhhgb.mc.notify.notification.NOTIFICATION_DISMISS";
	public final static String ACTION_COMPARE = "at.fhhgb.mc.notify.notification.NOTIFICATION_COMPARE";
	public final static String EXTRA_UNIQUE_ID = "at.fhhgb.mc.notify.notification.NOTIFICATION_UNIQUE_ID";
	public final static String EXTRA_NOTIFICATION_ID = "at.fhhgb.mc.notify.notification.NOTIFICATION_ID";
	public final static String EXTRA_VERSION = "at.fhhgb.mc.notify.notification.NOTIFICATION_VERSION";
	
	//strings used for xml creation and parsing
	public final static String KEY_ROOT = "notification";
	public final static String KEY_TITLE = "title";
	public final static String KEY_DATE = "date";
	public final static String KEY_START_YEAR = "start_year";
	public final static String KEY_START_MONTH = "start_month";
	public final static String KEY_START_DAY = "start_day";
	public final static String KEY_START_HOURS= "start_hours";
	public final static String KEY_START_MINUTES = "start_minutes";
	public final static String KEY_END_YEAR = "end_year";
	public final static String KEY_END_MONTH = "end_month";
	public final static String KEY_END_DAY = "end_day";
	public final static String KEY_END_HOURS= "end_hours";
	public final static String KEY_END_MINUTES = "end_minutes";
	public final static String KEY_TIME = "time";
	public final static String KEY_MESSAGE = "message";
	public final static String KEY_FILE = "file";
	public final static String KEY_UNIQUE_ID = "unique_id";
	public final static String ATTRIBUTE_CONTENT = "content";
	public final static String ATTRIBUTE_PATH = "path";
	public final static String ATTRIBUTE_START_YEAR = "start_year";
	public final static String ATTRIBUTE_START_MONTH = "start_month";
	public final static String ATTRIBUTE_START_DAY = "start_day";
	public final static String ATTRIBUTE_START_HOURS = "start_hours";
	public final static String ATTRIBUTE_START_MINUTES = "start_minutes";
	public final static String ATTRIBUTE_END_YEAR = "end_year";
	public final static String ATTRIBUTE_END_MONTH = "end_month";
	public final static String ATTRIBUTE_END_DAY = "end_day";
	public final static String ATTRIBUTE_END_HOURS = "end_hours";
	public final static String ATTRIBUTE_END_MINUTES = "end_minutes";
	
	private static long mTotalUniqueID;
	
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
	private int mVersion;
	
	//static notification id makes sure that each id is unique
	//so that multiple notification don't get concatenated
//	static int mNotificationID = 0;
	
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
		Intent i = new Intent(_context, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(_context, 0, i, 0);
		
		Intent action = new Intent(_context, NotificationService.class);
		action.setAction(Notification.ACTION_DISMISS);
		action.putExtra(EXTRA_UNIQUE_ID, getUniqueID());
		action.putExtra(EXTRA_VERSION, getVersion());
		action.putExtra(EXTRA_NOTIFICATION_ID, (int)getUniqueID());
		PendingIntent pAction = PendingIntent.getService(_context, (int)getUniqueID(), action, PendingIntent.FLAG_UPDATE_CURRENT);
		
        NotificationManager notificationManager = (NotificationManager)_context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(_context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(this.getTitle())
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(this.getMessage()))
                        .setContentText(this.getMessage())
                        .setTicker(this.getTitle())
                        .setVibrate(new long[]{200, 200, 200, 200})
                        .setLights(Color.WHITE, 1000, 10000)
                        .setContentIntent(pi)
                        .addAction(R.drawable.ic_launcher, "dismiss", pAction)
                        .setAutoCancel(true);
        notificationManager.notify((int)getUniqueID(), mBuilder.build());
        
//        mNotificationID++;
        Log.i(TAG, "notification built");
        
        Log.i(TAG, "uid: " + getUniqueID());
        Log.i(TAG, "uid: " + (int)getUniqueID());
        
	}
	
	static public void cancel(Context _context, int _notificationID) {
		NotificationManager notificationManager = (NotificationManager)_context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(_notificationID);
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
	
	public void setStartDate(int _year,int _month, int _day){
		mStartYear = _year;
		mStartMonth = _month;
		mStartDay = _day;
	}
	
	public void setEndDate(int _year,int _month, int _day){
		mEndYear = _year;
		mEndMonth = _month;
		mEndDay = _day;
	}
	
	public void setStartTime(int _hours,int _minutes){
		mStartHours = _hours;
		mStartMinutes = _minutes;
	}
	
	public void setEndTime(int _hours,int _minutes){
		mEndHours = _hours;
		mEndMinutes = _minutes;
	}
	
	public String getUniqueIDString(){
		return Long.toString(mUniqueID);
	}
	
	public String getTitle() {
		return mTitle;
	}
	public void setTitle(String title) throws Exception {
		if(title != null){
			this.mTitle = title;
		}
		else{
			throw(new Exception("title can not be null"));
		}
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
	public void setNewUniqueID(Context _context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_context);
		this.mUniqueID = pref.getLong(KEY_UNIQUE_ID, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putLong(KEY_UNIQUE_ID, mUniqueID + 1);
		editor.commit();
	}
	public int getVersion() {
		return mVersion;
	}
	public void setVersion(int mVersion) {
		this.mVersion = mVersion;
	}
}
