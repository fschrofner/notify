package at.fhhgb.mc.notify.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.os.Build;
import at.fhhgb.mc.notify.R;
import at.fhhgb.mc.notify.R.id;
import at.fhhgb.mc.notify.R.layout;
import at.fhhgb.mc.notify.R.menu;
import at.fhhgb.mc.notify.notification.Notification;
import at.fhhgb.mc.notify.xml.XmlCreator;

public class NotificationEditActivity extends Activity implements
		OnClickListener, OnDateSetListener, OnTimeSetListener {

	private static final String TAG = "NotificationEditActivity";
	private Calendar mCalendar = Calendar.getInstance();

	private TextView mStartDate;
	private TextView mEndDate;
	private TextView mStartTime;
	private TextView mEndTime;
	private boolean isStart = true;

	private int mStartYear = -1;
	private int mStartMonth = -1;
	private int mStartDay = -1;
	private int mEndYear = -1;
	private int mEndMonth = -1;
	private int mEndDay = -1;
	private int mStartHours = -1;
	private int mStartMinutes = -1;
	private int mEndHours = -1;
	private int mEndMinutes = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification_edit);

		// if (savedInstanceState == null) {
		// getFragmentManager().beginTransaction()
		// .add(R.id.container, new PlaceholderFragment()).commit();
		// }

		mStartDate = (TextView) findViewById(R.id.editStartDate);
		mStartDate.setOnClickListener(this);
		mEndDate = (TextView) findViewById(R.id.editEndDate);
		mEndDate.setOnClickListener(this);
		mStartTime = (TextView) findViewById(R.id.editStartTime);
		mStartTime.setOnClickListener(this);
		mEndTime = (TextView) findViewById(R.id.editEndTime);
		mEndTime.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_notification_edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.action_save) {
			save();
			finish();
		} else if (id == R.id.action_cancel) {
			finish();
		}

		return super.onOptionsItemSelected(item);
	}

	// /**
	// * A placeholder fragment containing a simple view.
	// */
	// public static class PlaceholderFragment extends Fragment {
	//
	// public PlaceholderFragment() {
	// }
	//
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View rootView = inflater.inflate(
	// R.layout.fragment_notification_edit, container, false);
	// return rootView;
	// }
	// }

	private void save() {
		Notification n = new Notification();
		XmlCreator creator = new XmlCreator();
		EditText title = (EditText) findViewById(R.id.editTitle);
		EditText message = (EditText) findViewById(R.id.editMessage);
		try {
			n.setTitle(title.getText().toString());
			n.setMessage(message.getText().toString());
			n.setStartDate(mStartYear, mStartMonth, mStartDay);
			n.setEndDate(mEndYear, mEndMonth, mEndDay);
			n.setStartTime(mStartHours, mStartMinutes);
			n.setEndTime(mEndHours, mEndMinutes);
			n.setUniqueID(99);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		creator.create(n, this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.editStartDate:
			DatePickerDialog dpdStart;
			if (mStartYear < 0 || mStartMonth < 0 || mStartDay < 0) {
				getCurrentTimeDate();
				dpdStart = new DatePickerDialog(this, this,
						mCalendar.get(Calendar.YEAR),
						mCalendar.get(Calendar.MONTH),
						mCalendar.get(Calendar.DAY_OF_MONTH));
			} else {
				dpdStart = new DatePickerDialog(this, this, mStartYear,
						mStartMonth - 1, mStartDay);
			}
			dpdStart.setTitle(R.string.title_start_date);
			isStart = true;
			dpdStart.show();
			break;
		case R.id.editStartTime:
			TimePickerDialog tpdStart;
			if (mStartHours < 0 || mStartMinutes < 0) {
				tpdStart = new TimePickerDialog(this, this,
						mCalendar.get(Calendar.HOUR_OF_DAY),
						mCalendar.get(Calendar.MINUTE),
						DateFormat.is24HourFormat(this));
			} else {
				tpdStart = new TimePickerDialog(this, this, mStartHours,
						mStartMinutes, DateFormat.is24HourFormat(this));
			}
			tpdStart.setTitle(R.string.title_start_time);
			isStart = true;
			tpdStart.show();
			break;
		case R.id.editEndDate:
			DatePickerDialog dpdEnd;
			if (mEndYear < 0 || mEndMonth < 0 || mEndDay < 0) {
				getCurrentTimeDate();
				dpdEnd = new DatePickerDialog(this, this,
						mCalendar.get(Calendar.YEAR),
						mCalendar.get(Calendar.MONTH),
						mCalendar.get(Calendar.DAY_OF_MONTH));
			} else {
				dpdEnd = new DatePickerDialog(this, this, mEndYear,
						mEndMonth - 1, mEndDay);
			}
			dpdEnd.setTitle(R.string.title_end_date);
			isStart = false;
			dpdEnd.show();
			break;
		case R.id.editEndTime:
			TimePickerDialog tpdEnd;
			if (mEndHours < 0 || mEndMinutes < 0) {
				tpdEnd = new TimePickerDialog(this, this,
						mCalendar.get(Calendar.HOUR_OF_DAY),
						mCalendar.get(Calendar.MINUTE),
						DateFormat.is24HourFormat(this));
			} else {
				tpdEnd = new TimePickerDialog(this, this, mEndHours,
						mEndMinutes, DateFormat.is24HourFormat(this));
			}
			tpdEnd.setTitle(R.string.title_end_time);
			isStart = false;
			tpdEnd.show();
		}

	}

	@Override
	public void onDateSet(DatePicker _view, int _year, int _monthOfYear,
			int _dayOfMonth) {

		mCalendar.set(Calendar.YEAR, _year);
		mCalendar.set(Calendar.MONTH, _monthOfYear);
		mCalendar.set(Calendar.DAY_OF_MONTH, _dayOfMonth);
		String myFormat = "MM/dd/yy";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

		if (isStart) {
			mStartDate.setText(sdf.format(mCalendar.getTime()));
			mStartYear = _year;
			mStartMonth = _monthOfYear + 1;
			mStartDay = _dayOfMonth;
		} else {
			mEndDate.setText(sdf.format(mCalendar.getTime()));
			mEndYear = _year;
			mEndMonth = _monthOfYear + 1;
			mEndDay = _dayOfMonth;
		}
	}

	@Override
	public void onTimeSet(TimePicker _view, int _hourOfDay, int _minute) {
		mCalendar.set(Calendar.HOUR_OF_DAY, _hourOfDay);
		mCalendar.set(Calendar.MINUTE, _minute);
		String myFormat = "kk:mm";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

		if (isStart) {
			mStartTime.setText(sdf.format(mCalendar.getTime()));
			mStartHours = _hourOfDay;
			mStartMinutes = _minute;
		} else {
			mEndTime.setText(sdf.format(mCalendar.getTime()));
			mEndHours = _hourOfDay;
			mEndMinutes = _minute;
		}

	}

	/**
	 * Gets the current date and Time
	 */
	private void getCurrentTimeDate() {
		mCalendar = Calendar.getInstance();
	}

}
