package at.fhhgb.mc.notify;

import org.jboss.aerogear.android.unifiedpush.MessageHandler;
import org.jboss.aerogear.android.unifiedpush.Registrations;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import at.fhhgb.mc.notify.notification.Notification;
import at.fhhgb.mc.notify.notification.NotificationService;
import at.fhhgb.mc.notify.push.PushRegisterReceiver;
import at.fhhgb.mc.notify.ui.NotificationFragment;
import at.fhhgb.mc.notify.ui.SettingsFragment;
import at.fhhgb.mc.notify.sync.SyncHandler;

/**
 * MainActivity of the application that manages the fragments, handles pushes when in foreground
 * and handles the option menu on the left.
 * @author Dominik Koeltringer & Florian Schrofner
 *
 */
public class MainActivity extends Activity implements MessageHandler {

	private static final String TAG = "MainActivity";
	private String[] mDrawerTitles;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	public final static int NOTIFICATION_REQUEST = 42;
	
	//defines the positions inside the side menu
	private static final int MENU_CURRENT_NOTIFICATIONS = 0;
	private static final int MENU_FUTURE_NOTIFICATIONS = 1;
	private static final int MENU_SETTINGS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setProgressBarVisible(false);

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

		//sync with online files on startup
		Intent intent = new Intent(this, PushRegisterReceiver.class);
		sendBroadcast(intent);
		SyncHandler.updateFiles(this,this);
	}
	
	public void setProgressBarVisible(final boolean _enabled) {
		runOnUiThread(new Runnable() {
		     @Override
		     public void run() {
		    	 ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
		 		if (_enabled) {
		 			bar.setVisibility(View.VISIBLE);
		 		} else {
		 			bar.setVisibility(View.INVISIBLE);
		 		}
		    }
		});
	}

	@Override
	/**
	 * Compares notifications when started.
	 */
	protected void onStart() {
		Intent intent = new Intent(this.getApplicationContext(), NotificationService.class);
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
		
		FragmentManager fragmentManager = getFragmentManager();
 		Fragment fragment = fragmentManager.findFragmentByTag(NotificationFragment.NOTIFICATION_FRAGMENT_TAG);
 		
 		if (fragment instanceof NotificationFragment) {
 			NotificationFragment nFragment = (NotificationFragment) fragment;
 			
 			if (drawerOpen) {
 	 			nFragment.hideAddOption();
 	 		} else {
 	 			nFragment.showAddOption();
 	 		}
 		}
 		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Tells the notification fragments to refresh their lists.
	 * Can be called from another thread, since the code will be run on the main thread.
	 */
	public void refreshFragments(){
		runOnUiThread(new Runnable() {
		     @Override
		     public void run() {
		    	FragmentManager fragmentManager = getFragmentManager();
		 		Fragment fragment = fragmentManager.findFragmentByTag(NotificationFragment.NOTIFICATION_FRAGMENT_TAG);
		 		if(fragment != null){
		 			NotificationFragment notificationFragment = (NotificationFragment) fragment;
		 			notificationFragment.updateFragment();
		 			Log.i(TAG, "refreshed notification fragments");
		 		} else {
		 			Log.i(TAG, "no notification fragment to refresh!");
		 		}
		    }
		});
		
		
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	/**
	 * Will be called when an item at the side menu was pressed.
	 * Switches fragments accordingly.
	 * @param _position the position of the selected option inside the side menu
	 */
	private void selectItem(int _position) {
		Log.i(TAG, "onItemClick: " + _position);

		Fragment fragment = null;
		Bundle args = null;

		switch (_position) {
		case MENU_CURRENT_NOTIFICATIONS:
			fragment = new NotificationFragment();
			args = new Bundle();
			args.putBoolean(NotificationFragment.ARG_NOTI_STATUS, true);
			fragment.setArguments(args);
			Log.i(TAG, "case 0");
			break;
		case MENU_FUTURE_NOTIFICATIONS:
			fragment = new NotificationFragment();
			args = new Bundle();
			args.putBoolean(NotificationFragment.ARG_NOTI_STATUS, false);
			fragment.setArguments(args);
			break;
		case MENU_SETTINGS:
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
		
		if(_position == MENU_SETTINGS){
			fragmentManager.beginTransaction()
			.replace(R.id.content_frame, fragment).commit();
		} else {
			fragmentManager.beginTransaction()
			.replace(R.id.content_frame, fragment,NotificationFragment.NOTIFICATION_FRAGMENT_TAG).commit();
		}


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

	@Override
	public void onDeleteMessage(Context context, Bundle message) {
		//do nothing
	}

	@Override
	/**
	 * This method will be called, when a push arrives and the application currently
	 * runs in foreground. Handles the push (updates the files).
	 */
	public void onMessage(Context context, Bundle message) {
		Log.i(TAG, "received push in MainActivity!");
        SyncHandler.updateFiles(context.getApplicationContext(),this);
	}

	@Override
	public void onError() {
		//ain't no error handling here
	}

	@Override
	/**
	 * Reregisters the activity for handling pushes when the application is resumed.
	 */
	protected void onResume() {
		super.onResume();
		Registrations.registerMainThreadHandler(this);
		Log.i(TAG, "registered MainActivity to handle pushes");
	}

	@Override
	/**
	 * Unregisters the activity from handling pushes when the application is put on pause.
	 */
	protected void onPause() {
		super.onPause();
		Registrations.unregisterMainThreadHandler(this);
		Log.i(TAG, "unregistered MainActivity from handling pushes");
	}

	@Override
	/**
	 * Refreshes the fragments when a new intent is received.
	 */
	protected void onNewIntent(Intent intent) {
		refreshFragments();
		super.onNewIntent(intent);
	}

}
