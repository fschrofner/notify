package at.fhhgb.mc.notify;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.unifiedpush.MessageHandler;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.fhhgb.mc.notify.notification.NotificationService;
import at.fhhgb.mc.notify.push.*;
import at.fhhgb.mc.notify.ui.ActualNotificationFragment;
import at.fhhgb.mc.notify.ui.FutureNotificationFragment;
import at.fhhgb.mc.notify.ui.SettingsFragment;

public class MainActivity extends Activity implements MessageHandler,
		OnClickListener {

	private static final String TAG = "MainActivity";
	private String[] mDrawerTitles;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

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

		// access the registration object
		PushRegistrar push = ((PushApplication) getApplication())
				.getRegistration();

		// fire up registration..

		// The method will attempt to register the device with GCM and the
		// UnifiedPush server
		push.register(getApplicationContext(), new Callback<Void>() { // 2
					private static final long serialVersionUID = 1L;

					@Override
					public void onSuccess(Void ignore) {
						Toast.makeText(MainActivity.this,
								"Registration Succeeded!", Toast.LENGTH_LONG)
								.show();
					}

					@Override
					public void onFailure(Exception exception) {
						Log.e(TAG, exception.getMessage(), exception);
					}
				});

//		Button pushButton = (Button) findViewById(R.id.push_button);
//		pushButton.setOnClickListener(this);
		Intent intent = new Intent(this, NotificationService.class);
		intent.setAction("bla");
		startService(intent);
//		Log.i(TAG, "End of onCreate");
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
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...

		return super.onOptionsItemSelected(item);
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
	protected void onResume() {
		super.onResume();
		Registrations.registerMainThreadHandler(this); // 1
	}

	@Override
	protected void onPause() {
		super.onPause();
		Registrations.unregisterMainThreadHandler(this); // 2
	}

	@Override
	public void onMessage(Context context, Bundle message) { // 3
//		// display the message contained in the payload
//		TextView text = (TextView) findViewById(R.id.label);
//		text.setText(message.getString("alert"));
//		text.invalidate();
	}

	@Override
	public void onDeleteMessage(Context context, Bundle message) {
		// handle GoogleCloudMessaging.MESSAGE_TYPE_DELETED
	}

	@Override
	public void onError() {
		// handle GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
	}

	@Override
	public void onClick(View v) {
//		EditText text = (EditText) findViewById(R.id.alias_text);
//		PushSender.sendPushToAlias(text.getText().toString());
	}

	private void selectItem(int _position) {
		Log.i(TAG, "onItemClick: " + _position);

		Fragment fragment = null;

		switch (_position) {
		case 0:
			fragment = new ActualNotificationFragment();
			break;
		case 1:
			fragment = new FutureNotificationFragment();
			break;
		case 2:
			fragment = new SettingsFragment();
			break;
		default:
			Log.e(TAG, "Failure in Navigation Drawer");
			fragment = new ActualNotificationFragment();
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
