package at.fhhgb.mc.notify.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import at.fhhgb.mc.notify.R;
import at.fhhgb.mc.notify.notification.Notification;
import at.fhhgb.mc.notify.notification.NotificationService;
import at.fhhgb.mc.notify.xml.XmlCreator;

public class NotificationEditActivity extends Activity implements
		OnClickListener, OnDateSetListener, OnTimeSetListener,
		OnCheckedChangeListener {

	private static final String TAG = "NotificationEditActivity";
	private final int REQUESTCODE_GET_FILE = 42;
	private final int NO_TITLE = 0;
	private final int TO_SMALL_END_DATE = 1;
	private final int NO_START_DATE = 2;
	private final int NO_START_TIME = 3;
	private final int NO_END_DATE = 4;
	private final int NO_END_TIME = 5;
	private final int NO_START = 6;
	private Calendar mCalendar = Calendar.getInstance();

	private TextView mStartDate;
	private TextView mEndDate;
	private TextView mStartTime;
	private TextView mEndTime;
	private boolean isStart = true;

	private long mUniqueID = -1;
	private int mVersion = -1;
	private String mTitle;
	private String mMessage;
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

	private int mStartYearTemp = -1;
	private int mStartMonthTemp = -1;
	private int mStartDayTemp = -1;
	private int mEndYearTemp = -1;
	private int mEndMonthTemp = -1;
	private int mEndDayTemp = -1;
	private int mStartHoursTemp = -1;
	private int mStartMinutesTemp = -1;
	private int mEndHoursTemp = -1;
	private int mEndMinutesTemp = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification_edit);

		Bundle b = getIntent().getBundleExtra(Notification.KEY_ROOT);

		if (b != null) {
			mUniqueID = b.getLong(Notification.KEY_UNIQUE_ID);
			mVersion = b.getInt(Notification.KEY_VERSION);
			mTitle = b.getString(Notification.KEY_TITLE);
			mMessage = b.getString(Notification.KEY_MESSAGE);
			mStartYear = b.getInt(Notification.KEY_START_YEAR);
			mStartMonth = b.getInt(Notification.KEY_START_MONTH);
			mStartDay = b.getInt(Notification.KEY_START_DAY);
			mStartHours = b.getInt(Notification.KEY_START_HOURS);
			mStartMinutes = b.getInt(Notification.KEY_START_MINUTES);
			mEndYear = b.getInt(Notification.KEY_END_YEAR);
			mEndMonth = b.getInt(Notification.KEY_END_MONTH);
			mEndDay = b.getInt(Notification.KEY_END_DAY);
			mEndHours = b.getInt(Notification.KEY_END_HOURS);
			mEndMinutes = b.getInt(Notification.KEY_END_MINUTES);
		}

		mStartDate = (TextView) findViewById(R.id.editStartDate);
		mStartDate.setOnClickListener(this);
		mEndDate = (TextView) findViewById(R.id.editEndDate);
		mEndDate.setOnClickListener(this);
		mStartTime = (TextView) findViewById(R.id.editStartTime);
		mStartTime.setOnClickListener(this);
		mEndTime = (TextView) findViewById(R.id.editEndTime);
		mEndTime.setOnClickListener(this);

		if (mUniqueID >= 0) {
			EditText title = (EditText) findViewById(R.id.editTitle);
			title.setText(mTitle);
			EditText message = (EditText) findViewById(R.id.editMessage);
			message.setText(mMessage);
		}

		CheckBox checkStart = (CheckBox) findViewById(R.id.checkStart);
		checkStart.setOnCheckedChangeListener(this);
		CheckBox checkStop = (CheckBox) findViewById(R.id.checkStop);
		checkStop.setOnCheckedChangeListener(this);
		Button buttonFile = (Button) findViewById(R.id.buttonFile);
		buttonFile.setOnClickListener(this);

		if (mStartYear >= 0) {
			checkStart.setChecked(true);
			mStartDate.setEnabled(true);
			mStartTime.setEnabled(true);

			mCalendar.set(Calendar.YEAR, mStartYear);
			mCalendar.set(Calendar.MONTH, mStartMonth - 1);
			mCalendar.set(Calendar.DAY_OF_MONTH, mStartDay);
			mCalendar.set(Calendar.HOUR_OF_DAY, mStartHours);
			mCalendar.set(Calendar.MINUTE, mStartMinutes);

			String format = "MM/dd/yy";
			SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
			mStartDate.setText(sdf.format(mCalendar.getTime()));
			format = "kk:mm";
			sdf = new SimpleDateFormat(format, Locale.US);
			mStartTime.setText(sdf.format(mCalendar.getTime()));
		}

		if (mEndYear >= 0) {
			checkStop.setChecked(true);
			mEndDate.setEnabled(true);
			mEndTime.setEnabled(true);

			mCalendar.set(Calendar.YEAR, mEndYear);
			mCalendar.set(Calendar.MONTH, mEndMonth - 1);
			mCalendar.set(Calendar.DAY_OF_MONTH, mEndDay);
			mCalendar.set(Calendar.HOUR_OF_DAY, mEndHours);
			mCalendar.set(Calendar.MINUTE, mEndMinutes);

			String format = "MM/dd/yy";
			SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
			mEndDate.setText(sdf.format(mCalendar.getTime()));
			format = "kk:mm";
			sdf = new SimpleDateFormat(format, Locale.US);
			mEndTime.setText(sdf.format(mCalendar.getTime()));
		}
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
		if (id == R.id.action_save) {
			EditText title = (EditText) findViewById(R.id.editTitle);
			CheckBox checkStart = (CheckBox) findViewById(R.id.checkStart);
			CheckBox checkStop = (CheckBox) findViewById(R.id.checkStop);

			if (title.getText().toString().equals("")) {
				showAlertDialog(NO_TITLE);
			} else if (!checkDate()) {
				showAlertDialog(TO_SMALL_END_DATE);
			} else if (checkStart.isChecked() && mStartYear < 0) {
				showAlertDialog(NO_START_DATE);
			} else if (checkStart.isChecked() && mStartHours < 0) {
				showAlertDialog(NO_START_TIME);
			} else if (checkStop.isChecked() && mEndYear < 0) {
				showAlertDialog(NO_END_DATE);
			} else if (checkStop.isChecked() && mEndHours < 0) {
				showAlertDialog(NO_END_TIME);
			} else if (mStartYear < 0 && mEndYear >= 0) {
				showAlertDialog(NO_START);

			} else {
				save();
				finish();
			}
		} else if (id == R.id.action_cancel) {
			finish();
		}

		return super.onOptionsItemSelected(item);
	}

	private void save() {
		Notification n = new Notification();
		XmlCreator creator = new XmlCreator();
		EditText title = (EditText) findViewById(R.id.editTitle);
		EditText message = (EditText) findViewById(R.id.editMessage);
		SharedPreferences triggeredNotifications = getSharedPreferences(NotificationService.TRIGGERED_NOTIFICATIONS, 0);

		try {
			n.setTitle(title.getText().toString());
			n.setMessage(message.getText().toString());
			n.setStartDate(mStartYear, mStartMonth, mStartDay);
			n.setEndDate(mEndYear, mEndMonth, mEndDay);
			n.setStartTime(mStartHours, mStartMinutes);
			n.setEndTime(mEndHours, mEndMinutes);
			if (mUniqueID < 0) {
				n.setUniqueID(Notification.generateUniqueID());
			} else {
				n.setUniqueID(mUniqueID);
			}
			if (mVersion < 0) {
				n.setVersion(0);
				Log.i(TAG, "new");
			} else {
				int notificationID = triggeredNotifications.getInt(String.valueOf(mUniqueID), -1);
				Log.i(TAG, "Version: " + mVersion);
				Intent action = new Intent(this, NotificationService.class);
				action.setAction(Notification.ACTION_DELETE);
				action.putExtra(Notification.EXTRA_UNIQUE_ID, mUniqueID);
				action.putExtra(Notification.EXTRA_VERSION, mVersion);
				action.putExtra(Notification.EXTRA_NOTIFICATION_ID, notificationID);
				this.startService(action);
				n.setVersion(mVersion + 1);
			}
		} catch (Exception e) {
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
			break;
		case R.id.buttonFile:
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, REQUESTCODE_GET_FILE);
		}

	}

	@Override
	public void onDateSet(DatePicker _view, int _year, int _monthOfYear,
			int _dayOfMonth) {
		Log.i(TAG, "onDateSet");

		mCalendar.set(Calendar.YEAR, _year);
		mCalendar.set(Calendar.MONTH, _monthOfYear);
		mCalendar.set(Calendar.DAY_OF_MONTH, _dayOfMonth);
		String format = "MM/dd/yy";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);

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
		String format = "kk:mm";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);

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

	@Override
	public void onCheckedChanged(CompoundButton _view, boolean _isChecked) {

		// boolean checked = ((CheckBox) _view).isChecked();

		if (_view.getId() == R.id.checkStart) {
			if (!_isChecked) {
				mStartDate.setEnabled(false);
				mStartTime.setEnabled(false);
				mStartDate.setText(R.string.ignored);
				mStartTime.setText(R.string.ignored);

				if (mStartYear != -1) {
					mStartYearTemp = mStartYear;
					mStartMonthTemp = mStartMonth;
					mStartDayTemp = mStartDay;
				}
				if (mStartHours != -1) {
					mStartHoursTemp = mStartHours;
					mStartMinutesTemp = mStartMinutes;
				}

				mStartYear = -1;
				mStartMonth = -1;
				mStartDay = -1;

				mStartHours = -1;
				mStartMinutes = -1;
			} else {
				mStartDate.setEnabled(true);
				mStartTime.setEnabled(true);

				if (mStartYearTemp != -1) {
					mStartYear = mStartYearTemp;
					mStartMonth = mStartMonthTemp;
					mStartDay = mStartDayTemp;

					mCalendar.set(Calendar.YEAR, mStartYear);
					mCalendar.set(Calendar.MONTH, mStartMonth - 1);
					mCalendar.set(Calendar.DAY_OF_MONTH, mStartDay);

					String format = "MM/dd/yy";
					SimpleDateFormat sdf = new SimpleDateFormat(format,
							Locale.US);
					mStartDate.setText(sdf.format(mCalendar.getTime()));
				} else {
					mStartDate.setText(R.string.edit_start_date);
				}

				if (mStartHoursTemp != -1) {
					mStartHours = mStartHoursTemp;
					mStartMinutes = mStartMinutesTemp;

					mCalendar.set(Calendar.HOUR_OF_DAY, mStartHours);
					mCalendar.set(Calendar.MINUTE, mStartMinutes);

					String format = "kk:mm";
					SimpleDateFormat sdf = new SimpleDateFormat(format,
							Locale.US);
					mStartTime.setText(sdf.format(mCalendar.getTime()));
				} else {
					mStartTime.setText(R.string.edit_start_time);
				}

			}
		} else if (_view.getId() == R.id.checkStop) {
			if (!_isChecked) {
				mEndDate.setEnabled(false);
				mEndTime.setEnabled(false);
				mEndDate.setText(R.string.ignored);
				mEndTime.setText(R.string.ignored);

				if (mEndYear != -1) {
					mEndYearTemp = mEndYear;
					mEndMonthTemp = mEndMonth;
					mEndDayTemp = mEndDay;
				}
				if (mEndHours != -1) {
					mEndHoursTemp = mEndHours;
					mEndMinutesTemp = mEndMinutes;
				}

				mEndYear = -1;
				mEndMonth = -1;
				mEndDay = -1;

				mEndHours = -1;
				mEndMinutes = -1;
			} else {
				mEndDate.setEnabled(true);
				mEndTime.setEnabled(true);

				if (mEndYearTemp != -1) {
					mEndYear = mEndYearTemp;
					mEndMonth = mEndMonthTemp;
					mEndDay = mEndDayTemp;

					mCalendar.set(Calendar.YEAR, mEndYear);
					mCalendar.set(Calendar.MONTH, mEndMonth - 1);
					mCalendar.set(Calendar.DAY_OF_MONTH, mEndDay);

					String format = "MM/dd/yy";
					SimpleDateFormat sdf = new SimpleDateFormat(format,
							Locale.US);
					mEndDate.setText(sdf.format(mCalendar.getTime()));
				} else {
					mEndDate.setText(R.string.edit_end_date);
				}

				if (mEndHoursTemp != -1) {
					mEndHours = mEndHoursTemp;
					mEndMinutes = mEndMinutesTemp;

					mCalendar.set(Calendar.HOUR_OF_DAY, mEndHours);
					mCalendar.set(Calendar.MINUTE, mEndMinutes);

					String format = "kk:mm";
					SimpleDateFormat sdf = new SimpleDateFormat(format,
							Locale.US);
					mEndTime.setText(sdf.format(mCalendar.getTime()));
				} else {
					mEndTime.setText(R.string.edit_end_time);
				}
			}
		}
	}

	private boolean checkDate() {
		if (mEndYear < 0) {
			return true;
		} else if (mEndYear > mStartYear) {
			return true;
		} else if (mEndYear == mStartYear) {
			if (mEndMonth > mStartMonth) {
				return true;
			} else if (mEndMonth == mStartMonth) {
				if (mEndDay > mStartDay) {
					return true;
				} else if (mEndDay == mStartDay) {
					if (mEndHours > mStartHours) {
						return true;
					} else if (mEndHours == mStartHours) {
						if (mEndMinutes > mStartMinutes) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private void showAlertDialog(int _status) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this,
				AlertDialog.THEME_DEVICE_DEFAULT_DARK);

		switch (_status) {
		case NO_TITLE:
			dialog.setTitle(getResources()
					.getString(R.string.alert_title_title));
			dialog.setMessage(getResources().getString(
					R.string.alert_title_text));
			break;
		case TO_SMALL_END_DATE:
			dialog.setTitle(getResources().getString(R.string.alert_date_title));
			dialog.setMessage(getResources()
					.getString(R.string.alert_date_text));
			break;
		case NO_START_DATE:
			dialog.setTitle(getResources().getString(
					R.string.alert_start_date_title));
			dialog.setMessage(getResources().getString(
					R.string.alert_start_date_text));
			break;
		case NO_START_TIME:
			dialog.setTitle(getResources().getString(
					R.string.alert_start_time_title));
			dialog.setMessage(getResources().getString(
					R.string.alert_start_time_text));
			break;
		case NO_END_DATE:
			dialog.setTitle(getResources().getString(
					R.string.alert_end_date_title));
			dialog.setMessage(getResources().getString(
					R.string.alert_end_date_text));
			break;
		case NO_END_TIME:
			dialog.setTitle(getResources().getString(
					R.string.alert_end_time_title));
			dialog.setMessage(getResources().getString(
					R.string.alert_end_time_text));
			break;
		case NO_START:
			dialog.setTitle(getResources().getString(
					R.string.alert_no_start_title));
			dialog.setMessage(getResources().getString(
					R.string.alert_no_start_text));
			break;
		default:
			Log.e(TAG, "Error showing AlertDialog");
		}

		// dialog.setIcon(R.drawable.alerts_and_states_warning);
		dialog.setNegativeButton(getResources()
				.getString(R.string.alert_button),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		dialog.show();
	}

	@Override
	// if the file(which should be written) was picked
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUESTCODE_GET_FILE && resultCode == RESULT_OK) {
			TextView tv = (TextView) findViewById(R.id.textFile);
			tv.setText(data.getData().getPath());
		}
	}
}
