package at.fhhgb.mc.notify.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.joda.time.DateTime;

import android.app.Fragment;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import at.fhhgb.mc.notify.R;
import at.fhhgb.mc.notify.notification.Notification;
import at.fhhgb.mc.notify.notification.NotificationBroadcastReceiver;
import at.fhhgb.mc.notify.notification.NotificationService;
import at.fhhgb.mc.notify.xml.XmlParser;

public class NotificationFragment extends Fragment {

	private static final String TAG = "NotificationFragment";
	public static final String ARG_NOTI_STATUS = "at.fhhgb.mc.notify.ui.NotificationFragment.ARG_NOTI_STATUS";
	private ArrayList<Notification> mNotiList;
	private boolean mNotiStatus;
	private ArrayList<Notification> mNotifications;
	public static ArrayList<Notification> mTriggeredNotifications;
	public static ArrayList<Notification> mFutureNotifications;

	@Override
	public View onCreateView(LayoutInflater _inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mNotiStatus = getArguments().getBoolean(ARG_NOTI_STATUS);

		View view = _inflater.inflate(R.layout.fragment_notification, null);

		update(view);

		return view;
	}

	private void update(View _v) {
		compareNotifications();

		ArrayList<String> titleList = new ArrayList<String>();
		ArrayList<Notification> notiList = null;

		if (mNotiStatus) {
			notiList = mTriggeredNotifications;
		} else {
			notiList = mFutureNotifications;
		}
		
		for (int i = 0; i < notiList.size(); i++) {
			titleList.add(notiList.get(i).getTitle());
		}

		ListView v = (ListView) _v.findViewById(R.id.cardListView);
		ListAdapter listAdapter = new ArrayListAdapter(getActivity(),
				R.layout.fragment_list_item, R.id.item_text_view, titleList);

		v.setAdapter(listAdapter);
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
	 * Reloads the notification list of the service.
	 */
	private void reload(){
		mNotifications = new ArrayList<Notification>();
		String[] xmlList = getActivity().getFilesDir().list();
		XmlParser parser = new XmlParser(getActivity().getApplicationContext());
		Notification noti;
		try {
			for (String file : xmlList) {
				noti = parser.readXml(file);
				mNotifications.add(noti);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
