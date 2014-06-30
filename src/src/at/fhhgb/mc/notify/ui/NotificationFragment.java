package at.fhhgb.mc.notify.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.joda.time.DateTime;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import at.fhhgb.mc.notify.MainActivity;
import at.fhhgb.mc.notify.R;
import at.fhhgb.mc.notify.notification.Notification;
import at.fhhgb.mc.notify.notification.NotificationService;
import at.fhhgb.mc.notify.sync.SyncHandler;
import at.fhhgb.mc.notify.xml.XmlParser;

public class NotificationFragment extends Fragment implements
		OnItemClickListener, MultiChoiceModeListener {

	private static final String TAG = "NotificationFragment";
	public static final String ARG_NOTI_STATUS = "at.fhhgb.mc.notify.ui.NotificationFragment.ARG_NOTI_STATUS";
	private static final int NOTIFICATION_CURRENT = 0;
	private static final int NOTIFICATION_FUTURE = 1;
	private static final int NOTIFICATION_PAST = 2;

	public static final String NOTIFICATION_FRAGMENT_TAG = "at.fhhgb.mc.notify.notification_fragment";

	private boolean mNotiStatus;
	private ArrayList<Notification> mNotifications;
	private static ArrayList<Notification> mTriggeredNotifications;
	private static ArrayList<Notification> mFutureNotifications;
	private View mView;
	private int mNr;
	private ArrayListAdapter listAdapter;
	private Menu mMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater _inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mNotiStatus = getArguments().getBoolean(ARG_NOTI_STATUS);

		mView = _inflater.inflate(R.layout.fragment_notification, null);

		update(mView);
		
		return mView;
	}

	@Override
	public void onStart() {

		super.onStart();
	}

	public void updateFragment() {
		update(mView);
	}
	
	public void showAddOption() {
		mMenu.findItem(R.id.action_add).setVisible(true);
	}
	
	public void hideAddOption() {
		mMenu.findItem(R.id.action_add).setVisible(false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		mMenu = menu;
		
		inflater.inflate(R.menu.main, mMenu);

		super.onCreateOptionsMenu(mMenu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_add) {
			Intent i = new Intent(getActivity(), NotificationEditActivity.class);
			startActivityForResult(i, MainActivity.NOTIFICATION_REQUEST);
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i(TAG, "result");
		if (requestCode == MainActivity.NOTIFICATION_REQUEST && resultCode == MainActivity.RESULT_OK) {
			ArrayList<String> fileList = data
					.getStringArrayListExtra(SyncHandler.EXTRA_FILE_LIST);
			ArrayList<String> fileListDelete = data.getStringArrayListExtra(SyncHandler.EXTRA_FILE_LIST_DELETE);
			ArrayList<String> titleList = data.getStringArrayListExtra(SyncHandler.EXTRA_TITLE_LIST);
			if (fileList != null) {
				SyncHandler.uploadFiles(getActivity(), getActivity(), fileList);
			} else {
				Log.w(TAG,
						"error! saved notification did not return a filelist!");
			}
			if (fileListDelete != null) {
				SyncHandler.deleteFiles(getActivity(), getActivity(), fileListDelete);

			}
			if (titleList != null) {
				Log.w(TAG, "title: " + titleList.get(0));
				update(mView);
				deleteNotifications(titleList);
			} else {
				update(mView);
				Log.w(TAG, "title: null");
			}
			
			//update(mView);
		}
	}

	/**
	 * This method is used to instantly remove a list of notifications from the
	 * fragment. (While background threads still take place, for example)
	 * 
	 * @param _titles
	 *            the titles of the notifications you want to delete
	 */
	private void deleteNotifications(ArrayList<String> _titles) {

		ArrayList<String> titleList = new ArrayList<String>();
		ArrayList<String> messageList = new ArrayList<String>();
		ArrayList<Notification> notiList = null;

		if (mNotiStatus) {
			notiList = mTriggeredNotifications;
		} else {
			notiList = mFutureNotifications;
		}

		// create the two lists needed for the arraylistadapter
		for (int i = 0; i < notiList.size(); i++) {
			titleList.add(notiList.get(i).getTitle());
			messageList.add(notiList.get(i).getMessage());
		}

		// delete all items with matching title from the lists
		for (int i = 0; i < _titles.size(); i++) {

			int index = titleList.indexOf(_titles.get(i));

			if (index >= 0 && index < titleList.size()) {
				Log.i(TAG, "found item in list! @index: " + index);
				titleList.remove(index);
				messageList.remove(index);
				notiList.remove(index);
			}
		}

		showLists(titleList, messageList);
	}

	private void update(View _v) {
		compareNotifications();

		ArrayList<String> titleList = new ArrayList<String>();
		ArrayList<String> messageList = new ArrayList<String>();
		ArrayList<Notification> notiList = null;

		if (mNotiStatus) {
			notiList = mTriggeredNotifications;
		} else {
			notiList = mFutureNotifications;
		}

		for (int i = 0; i < notiList.size(); i++) {
			titleList.add(notiList.get(i).getTitle());
			messageList.add(notiList.get(i).getMessage());
		}

		showLists(titleList, messageList);
	}

	private void showLists(ArrayList<String> _titleList,
			ArrayList<String> _messageList) {

		Log.i(TAG, "show lists called!");

		ListView v = (ListView) mView.findViewById(R.id.cardListView);
		listAdapter = new ArrayListAdapter(getActivity(),
				R.layout.fragment_list_item, R.id.item_title, _titleList,
				_messageList);

		v.setAdapter(listAdapter);
		v.setOnItemClickListener(this);
		v.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		v.setMultiChoiceModeListener(this);
		listAdapter.notifyDataSetChanged();
	}

	/**
	 * Compares all the notifications inside the notification list.
	 */
	private void compareNotifications() {
		mTriggeredNotifications = new ArrayList<Notification>();
		mFutureNotifications = new ArrayList<Notification>();

		// sets the current time every time an intent is sent
		int currentYear = Integer.parseInt(String.valueOf(Calendar
				.getInstance().get(Calendar.YEAR)));
		// please note: the months start with 0 (= January)
		int currentMonth = Integer.parseInt(String.valueOf(Calendar
				.getInstance().get(Calendar.MONTH)));
		++currentMonth;
		int currentDay = Integer.parseInt(String.valueOf(Calendar.getInstance()
				.get(Calendar.DAY_OF_MONTH)));
		int currentHours = Integer.parseInt(String.valueOf(Calendar
				.getInstance().get(Calendar.HOUR_OF_DAY)));
		int currentMinutes = Integer.parseInt(String.valueOf(Calendar
				.getInstance().get(Calendar.MINUTE)));

		Log.i(TAG, "date set to: " + currentYear + "/" + currentMonth + "/"
				+ currentDay + " " + currentHours + ":" + currentMinutes);
		Log.i(TAG, "current time set and comparison started");

		reload();

		DateTime currentDate = new DateTime(currentYear, currentMonth,
				currentDay, currentHours, currentMinutes);

		for (int i = 0; i < mNotifications.size(); i++) {
			// checks if the notification should be triggered at the current
			// time and if it hasn't been triggered before
			Log.i(TAG, "CHECKING UNIQUE ID: "
					+ mNotifications.get(i).getUniqueIDString());
			if (compareDates(currentDate, mNotifications.get(i)) == NOTIFICATION_CURRENT) {
				Log.i(TAG, "matching notification: "
						+ mNotifications.get(i).getTitle());
				mTriggeredNotifications.add(mNotifications.get(i));
			} else if (compareDates(currentDate, mNotifications.get(i)) == NOTIFICATION_FUTURE) {
				Log.i(TAG, "notification does not match");
				mFutureNotifications.add(mNotifications.get(i));
			}
		}
	}

	/**
	 * Checks if the given date is inside the given date range.
	 * 
	 * @param _date
	 *            the date you want to check
	 * @param _dateRange
	 *            the date range to which it should be compared
	 * @return true = date inside range, false = date outside the range
	 */
	private int compareDates(DateTime _date, Notification _notification) {
		ArrayList<DateTime> dateRange = _notification.getDates();

		if ((_notification.getStartYear() == -1 && _notification.getEndYear() == -1)
				|| (dateRange.get(0).isBefore(_date) && _notification
						.getEndYear() == -1)) {
			Log.i(TAG,
					"no start year, or start year was before and no end year specified");
			return NOTIFICATION_CURRENT;
		} else if ((_date.isAfter(dateRange.get(0)) || _date.isEqual(dateRange
				.get(0)))
				&& (_date.isBefore(dateRange.get(1)) || _date.isEqual(dateRange
						.get(1)))) {
			Log.i(TAG,
					"start date and end date specified, current time is inbetween or equal");
			return NOTIFICATION_CURRENT;
		} else if (_date.isBefore(dateRange.get(0))) {
			return NOTIFICATION_FUTURE;
		} else if (_date.isAfter(dateRange.get(1))) {
			return NOTIFICATION_PAST;
		} else {
			Log.e(TAG, "Error while comparing times");
			return NOTIFICATION_CURRENT;
		}
	}

	/**
	 * Reloads the notification list of the service.
	 */
	public void reload() {
		mNotifications = new ArrayList<Notification>();
		// String[] xmlList = getActivity().getFilesDir().list();
		// java.io.File fileFolder = new
		// java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER + "/" +
		// SyncHandler.NOTIFICATION_FOLDER);
		// boolean returnV = fileFolder.mkdirs();

		java.io.File rootFolder = new java.io.File(
				SyncHandler.ROOT_NOTIFICATION_FOLDER);
		rootFolder.mkdirs();

		// create directory
		java.io.File fileFolder = new java.io.File(rootFolder,
				SyncHandler.NOTIFICATION_FOLDER);
		fileFolder.mkdirs();

		String[] xmlList = fileFolder.list();
		XmlParser parser = new XmlParser(getActivity().getApplicationContext());
		Notification noti;

		if (xmlList == null) {
			Log.i(TAG, "null");
		}

		try {
			for (String file : xmlList) {
				noti = parser.readXml(file);
				mNotifications.add(noti);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Notification n;

		if (mNotiStatus) {
			n = mTriggeredNotifications.get(position);
		} else {
			n = mFutureNotifications.get(position);
		}

		Intent i = new Intent(getActivity(), NotificationEditActivity.class);
		Bundle b = new Bundle();
		b.putLong(Notification.KEY_UNIQUE_ID, n.getUniqueID());
		b.putInt(Notification.KEY_VERSION, n.getVersion());
		b.putString(Notification.KEY_TITLE, n.getTitle());
		b.putString(Notification.KEY_MESSAGE, n.getMessage());
		b.putInt(Notification.KEY_START_YEAR, n.getStartYear());
		b.putInt(Notification.KEY_START_MONTH, n.getStartMonth());
		b.putInt(Notification.KEY_START_DAY, n.getStartDay());
		b.putInt(Notification.KEY_START_HOURS, n.getStartHours());
		b.putInt(Notification.KEY_START_MINUTES, n.getStartMinutes());
		b.putInt(Notification.KEY_END_YEAR, n.getEndYear());
		b.putInt(Notification.KEY_END_MONTH, n.getEndMonth());
		b.putInt(Notification.KEY_END_DAY, n.getEndDay());
		b.putInt(Notification.KEY_END_HOURS, n.getEndHours());
		b.putInt(Notification.KEY_END_MINUTES, n.getEndMinutes());
		i.putExtra(Notification.KEY_ROOT, b);
		startActivityForResult(i, MainActivity.NOTIFICATION_REQUEST);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mNr = 0;

		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.context, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// mAdapter.clearSelection();
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		SharedPreferences triggeredNotifications = getActivity()
				.getSharedPreferences(
						NotificationService.TRIGGERED_NOTIFICATIONS, 0);

		switch (item.getItemId()) {
		case R.id.menu_delete:
			// retrieve selected items and delete them out
			boolean[] selected = listAdapter.getSelectedIds();
			ArrayList<String> titles = new ArrayList<String>();
			ArrayList<String> fileList = new ArrayList<String>();

			for (int i = 0; i < selected.length; i++) {
				if (selected[i]) {
					Notification noti;
					if (mNotiStatus) {
						noti = mTriggeredNotifications.get(i);
					} else {
						noti = mFutureNotifications.get(i);
					}

					// int notificationID =
					// triggeredNotifications.getInt(noti.getUniqueIDString(),
					// -1);

					// Intent action = new Intent(getActivity(),
					// NotificationService.class);
					// action.setAction(Notification.ACTION_DELETE);
					// action.putExtra(Notification.EXTRA_UNIQUE_ID,
					// noti.getUniqueID());
					// action.putExtra(Notification.EXTRA_VERSION,
					// noti.getVersion());
					// action.putExtra(Notification.EXTRA_NOTIFICATION_ID,
					// notificationID);
					// getActivity().startService(action);
					titles.add(noti.getTitle());
					fileList.add(noti.getFileName());
					// mTriggeredNotifications.remove(i);
				}
			}

			deleteNotifications(titles);
			SyncHandler.deleteFiles(getActivity(), getActivity(), fileList);

			// listAdapter.notifyDataSetChanged();
			// listAdapter = null;
			// update(mView);
			mode.finish();
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		listAdapter.noSelection();
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {

		if (checked) {
			mNr++;
			listAdapter.setNewSelection(position);
		} else {
			mNr--;
			listAdapter.removeSelection(position);
		}
		mode.setTitle(mNr + " " + getResources().getString(R.string.selected));
	}

}
