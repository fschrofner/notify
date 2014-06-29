package at.fhhgb.mc.notify;


import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import at.fhhgb.mc.notify.notification.Notification;
import at.fhhgb.mc.notify.notification.NotificationService;
import at.fhhgb.mc.notify.push.*;
import at.fhhgb.mc.notify.ui.NotificationEditActivity;
import at.fhhgb.mc.notify.ui.NotificationFragment;
import at.fhhgb.mc.notify.ui.SettingsFragment;
import at.fhhgb.mc.notify.sync.SyncHandler;

public class MainActivity extends Activity implements OnClickListener{
	
	private static final String TAG = "MainActivity";
	private String[] mDrawerTitles;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	
	final static int NOTIFICATION_REQUEST = 42;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTitle = mDrawerTitle = getTitle();
		mDrawerTitles = getResources().getStringArray(R.array.drawer_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mDrawerTitles));
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		 if (savedInstanceState == null) {
		 selectItem(0);
		 }

		//TODO register push on system start-up
		
//		DriveHandler.setup(this);
//		Intent intent = new Intent(this, PushRegisterReceiver.class);
//		sendBroadcast(intent);
		//SyncHandler.updateFiles(this,this);
	}

@Override
	protected void onStart() {
		Intent intent = new Intent(this, NotificationService.class);
		intent.setAction(Notification.ACTION_START_SERVICE);
		startService(intent);
		super.onStart();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_add).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		} else if (item.getItemId() == R.id.action_add) {
			Intent i = new Intent(this, NotificationEditActivity.class);
			startActivityForResult(i, NOTIFICATION_REQUEST);
		}
		// Handle your other action bar items...

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ArrayList<String> fileList = data.getStringArrayListExtra("uploadList"); //TODO replace with constant
		if(fileList != null){
			SyncHandler.uploadFiles(this, this, fileList);
		} else {
			Log.w(TAG, "error! saved notification did not return a filelist!");
		}
		
	}
	
	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
//		EditText text = (EditText) findViewById(R.id.alias_text);
//		PushSender.sendPushToAlias(text.getText().toString());
	}

	private void selectItem(int _position) {
		Log.i(TAG, "onItemClick: " + _position);

		Fragment fragment = null;
		Bundle args = null;

		switch (_position) {
		case 0:
			fragment = new NotificationFragment();
			args = new Bundle();
			args.putBoolean(NotificationFragment.ARG_NOTI_STATUS, true);
			fragment.setArguments(args);
			Log.i(TAG, "case 0");
			break;
		case 1:
			fragment = new NotificationFragment();
			args = new Bundle();
			args.putBoolean(NotificationFragment.ARG_NOTI_STATUS, false);
			fragment.setArguments(args);
			break;
		case 2:
			fragment = new SettingsFragment();
			break;
		default:
			Log.e(TAG, "Failure in Navigation Drawer");
			fragment = new NotificationFragment();
			args = new Bundle();
			args.putBoolean(NotificationFragment.ARG_NOTI_STATUS, true);
			fragment.setArguments(args);
		}

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment).commit();

		mDrawerList.setItemChecked(_position, true);
		setTitle(mDrawerTitles[_position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	public class DrawerItemClickListener implements
			ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> _parent, View _view,
				int _position, long _id) {
			selectItem(_position);

		}

	}

}
