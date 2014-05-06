package at.fhhgb.mc.notify.notification;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.content.Context;
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
	
	//static notification id makes sure that each id is unique
	//so that multiple notification don't get concatenated
	static int mNotificationID = 0;
	
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
}
