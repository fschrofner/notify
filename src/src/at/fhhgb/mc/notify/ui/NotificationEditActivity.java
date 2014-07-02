package at.fhhgb.mc.notify.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.android.datetimepicker.time.TimePickerDialog.OnTimeSetListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import at.fhhgb.mc.notify.R;
import at.fhhgb.mc.notify.notification.Notification;
import at.fhhgb.mc.notify.sync.SyncHandler;
import at.fhhgb.mc.notify.xml.XmlCreator;

/**
 * Activity used 
 * @author Dominik Koeltringer & Florian Schrofner
 *
 */
public class NotificationEditActivity extends Activity implements
		OnClickListener, OnDateSetListener, OnTimeSetListener,
		OnCheckedChangeListener {

	private static final String TAG = "NotificationEditActivity";
	public static final String DATEPICKER_START_TAG = "datepicker_start";
    public static final String TIMEPICKER_START_TAG = "timepicker_start";
    public static final String DATEPICKER_END_TAG = "datepicker_end";
    public static final String TIMEPICKER_END_TAG = "timepicker_end";
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
	
	private int addedFiles = 0;
	private ArrayList<String> mFileList;
	private ArrayList<String> mFileListDelete;
	private ArrayList<String> mTitleList;
	private ArrayList<String> mFilePaths;
	private ArrayList<String> mFileNames;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification_edit);
		
		mFilePaths = new ArrayList<String>();
		mFileNames = new ArrayList<String>();

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
			mFileNames = b.getStringArrayList(Notification.KEY_FILE);
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
			mStartDate.setText(Html.fromHtml("<u>" + sdf.format(mCalendar.getTime()) + "</u>"));
			format = "kk:mm";
			sdf = new SimpleDateFormat(format, Locale.US);
			mStartTime.setText(Html.fromHtml("<u>" + sdf.format(mCalendar.getTime()) + "</u>"));
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
			mEndDate.setText(Html.fromHtml("<u>" + sdf.format(mCalendar.getTime()) + "</u>"));
			format = "kk:mm";
			sdf = new SimpleDateFormat(format, Locale.US);
			mEndTime.setText(Html.fromHtml("<u>" + sdf.format(mCalendar.getTime()) + "</u>"));
		}
		
		if (mFileNames.size() > 0) {
			showFiles(mFileNames);
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
		} else if (id == R.id.action_add_file) {
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, REQUESTCODE_GET_FILE);
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Saves the currently selected specifications into a notification object
	 * and then writes it to a xml-file.
	 */
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
			n.setFiles(mFileNames);
			if (mUniqueID < 0) {
				n.setUniqueID(Notification.generateUniqueID());
			} else {
				n.setUniqueID(mUniqueID);
			}
			if (mVersion < 0) {
				n.setVersion(0);
			} else {
				n.setVersion(mVersion);
				
				mFileListDelete = new ArrayList<String>();
				mFileListDelete.add(n.getFileName());
				mTitleList = new ArrayList<String>();
				mTitleList.add(mTitle);
				
				n.setVersion(mVersion + 1);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < mFilePaths.size(); i++) {
			File src = new File(mFilePaths.get(i));
	        File dst = new File(SyncHandler.getFullPath(mFileNames.get(i)));
	        try {
				copy(src, dst);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		creator.create(n, this);
		
		mFileList = new ArrayList<String>();
		mFileList.add(n.getFileName());
		
		
		ArrayList<String> fileList = n.getFiles();
		if(fileList != null){
			mFileList.addAll(fileList);
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.editStartDate:
			DatePickerDialog dpdStart;
			if (mStartYear < 0 || mStartMonth < 0 || mStartDay < 0) {
				getCurrentTimeDate();

				dpdStart = DatePickerDialog.newInstance(this,
						mCalendar.get(Calendar.YEAR),
						mCalendar.get(Calendar.MONTH),
						mCalendar.get(Calendar.DAY_OF_MONTH));
			} else {
				dpdStart = DatePickerDialog.newInstance(this, mStartYear,
						mStartMonth - 1, mStartDay);
			}
			isStart = true;
			dpdStart.show(getFragmentManager(), DATEPICKER_START_TAG);
			break;
		case R.id.editStartTime:
			TimePickerDialog tpdStart;
			if (mStartHours < 0 || mStartMinutes < 0) {
				tpdStart = TimePickerDialog.newInstance(this,
						mCalendar.get(Calendar.HOUR_OF_DAY),
						mCalendar.get(Calendar.MINUTE),
						DateFormat.is24HourFormat(this));
			} else {
				tpdStart = TimePickerDialog.newInstance(this, mStartHours,
						mStartMinutes, DateFormat.is24HourFormat(this));
			}
			isStart = true;
			tpdStart.show(getFragmentManager(), TIMEPICKER_START_TAG);
			break;
		case R.id.editEndDate:
			DatePickerDialog dpdEnd;
			if (mEndYear < 0 || mEndMonth < 0 || mEndDay < 0) {
				getCurrentTimeDate();
				dpdEnd = DatePickerDialog.newInstance(this,
						mCalendar.get(Calendar.YEAR),
						mCalendar.get(Calendar.MONTH),
						mCalendar.get(Calendar.DAY_OF_MONTH));
			} else {
				dpdEnd = DatePickerDialog.newInstance(this, mEndYear,
						mEndMonth - 1, mEndDay);
			}
			isStart = false;
			dpdEnd.show(getFragmentManager(), DATEPICKER_END_TAG);
			break;
		case R.id.editEndTime:
			TimePickerDialog tpdEnd;
			if (mEndHours < 0 || mEndMinutes < 0) {
				tpdEnd = TimePickerDialog.newInstance(this,
						mCalendar.get(Calendar.HOUR_OF_DAY),
						mCalendar.get(Calendar.MINUTE),
						DateFormat.is24HourFormat(this));
			} else {
				tpdEnd = TimePickerDialog.newInstance(this, mEndHours,
						mEndMinutes, DateFormat.is24HourFormat(this));
			}
			isStart = false;
			tpdEnd.show(getFragmentManager(), TIMEPICKER_END_TAG);
			break;			
		}

	}

	@Override
	public void onDateSet(DatePickerDialog _view, int _year, int _monthOfYear,
			int _dayOfMonth) {
		Log.i(TAG, "onDateSet");

		mCalendar.set(Calendar.YEAR, _year);
		mCalendar.set(Calendar.MONTH, _monthOfYear);
		mCalendar.set(Calendar.DAY_OF_MONTH, _dayOfMonth);
		String format = "MM/dd/yy";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);

		if (isStart) {
			mStartDate.setText(Html.fromHtml("<u>" + sdf.format(mCalendar.getTime()) + "</u>"));
			mStartYear = _year;
			mStartMonth = _monthOfYear + 1;
			mStartDay = _dayOfMonth;
		} else {
			mEndDate.setText(Html.fromHtml("<u>" + sdf.format(mCalendar.getTime()) + "</u>"));
			mEndYear = _year;
			mEndMonth = _monthOfYear + 1;
			mEndDay = _dayOfMonth;
		}
	}

	@Override
	public void onTimeSet(RadialPickerLayout _view, int _hourOfDay, int _minute) {
		mCalendar.set(Calendar.HOUR_OF_DAY, _hourOfDay);
		mCalendar.set(Calendar.MINUTE, _minute);
		String format = "kk:mm";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);

		if (isStart) {
			mStartTime.setText(Html.fromHtml("<u>" + sdf.format(mCalendar.getTime()) + "</u>"));
			mStartHours = _hourOfDay;
			mStartMinutes = _minute;
		} else {
			mEndTime.setText(Html.fromHtml("<u>" + sdf.format(mCalendar.getTime()) + "</u>"));
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
					mStartDate.setText(Html.fromHtml("<u>" + sdf.format(mCalendar.getTime()) + "</u>"));
				} else {
					mStartDate.setText(Html.fromHtml("<u>" + getResources().getString(R.string.edit_date) + "</u>"));
				}

				if (mStartHoursTemp != -1) {
					mStartHours = mStartHoursTemp;
					mStartMinutes = mStartMinutesTemp;

					mCalendar.set(Calendar.HOUR_OF_DAY, mStartHours);
					mCalendar.set(Calendar.MINUTE, mStartMinutes);

					String format = "kk:mm";
					SimpleDateFormat sdf = new SimpleDateFormat(format,
							Locale.US);
					mStartTime.setText(Html.fromHtml("<u>" + sdf.format(mCalendar.getTime()) + "</u>"));
				} else {
					mStartTime.setText(Html.fromHtml("<u>" + getResources().getString(R.string.edit_time) + "</u>"));
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
					mEndDate.setText(Html.fromHtml("<u>" + sdf.format(mCalendar.getTime()) + "</u>"));
				} else {
					mEndDate.setText(Html.fromHtml("<u>" + getResources().getString(R.string.edit_date) + "</u>"));
				}

				if (mEndHoursTemp != -1) {
					mEndHours = mEndHoursTemp;
					mEndMinutes = mEndMinutesTemp;

					mCalendar.set(Calendar.HOUR_OF_DAY, mEndHours);
					mCalendar.set(Calendar.MINUTE, mEndMinutes);

					String format = "kk:mm";
					SimpleDateFormat sdf = new SimpleDateFormat(format,
							Locale.US);
					mEndTime.setText(Html.fromHtml("<u>" + sdf.format(mCalendar.getTime()) + "</u>"));
				} else {
					mEndTime.setText(Html.fromHtml("<u>" + getResources().getString(R.string.edit_time) + "</u>"));
				}
			}
		}
	}

	/**
	 * Checks if both dates are valid.
	 * @return true = valid values, false = invalid values
	 */
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

	/**
	 * Shows the alert dialog corresponding to the given status.
	 * @param _status the status of which to show the alert dialog
	 */
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
			
			Uri selectedImage = data.getData();
			if(selectedImage != null){
	            String[] filePathColumn = {MediaStore.Images.Media.DATA};
	            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
	            cursor.moveToFirst();
	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            String filePath = cursor.getString(columnIndex);
	            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
	            
	            mFilePaths.add(filePath);
	            mFileNames.add(fileName);
				
				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = li.inflate(R.layout.image_view_layout, null);
				
				ImageView iv = (ImageView) v.findViewById(R.id.image_view_file);
				iv.setImageURI(Uri.parse(filePath));
				
				View insertPoint = findViewById(R.id.linear_layout_files);
				((ViewGroup) insertPoint).addView(v, addedFiles, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				addedFiles++;
			}
		}
	}
	/**
	 * Shows the attached images
	 * @param fileNames The file names of the attached images
	 */
	private void showFiles(ArrayList<String> fileNames) {
		for (String file : fileNames) {
			String filePath = SyncHandler.getFullPath(file);
			
			LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = li.inflate(R.layout.image_view_layout, null);
			
			ImageView iv = (ImageView) v.findViewById(R.id.image_view_file);
			iv.setImageURI(Uri.parse(filePath));
			
			View insertPoint = findViewById(R.id.linear_layout_files);
			((ViewGroup) insertPoint).addView(v, addedFiles, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			addedFiles++;
		}
	}
	
	@Override
	public void finish() {
	  // Prepare data intent 
	  Intent data = new Intent();
	  data.putExtra(SyncHandler.EXTRA_FILE_LIST, mFileList);
	  data.putExtra(SyncHandler.EXTRA_FILE_LIST_DELETE, mFileListDelete);
	  data.putExtra(SyncHandler.EXTRA_TITLE_LIST, mTitleList);
	  setResult(RESULT_OK, data);
	  super.finish();
	} 
	
	/**
	 * Copies the file from source to the destination directory
	 * @param src source file
	 * @param dst destination file
	 * @throws IOException
	 */
	public void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
}
