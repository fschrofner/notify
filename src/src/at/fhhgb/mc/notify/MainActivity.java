package at.fhhgb.mc.notify;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.unifiedpush.MessageHandler;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.Registrations;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import at.fhhgb.mc.notify.push.*;
import at.fhhgb.mc.notify.sync.CreateFile;
import at.fhhgb.mc.notify.sync.CreateFileAppFolder;
import at.fhhgb.mc.notify.sync.CreateFolder;
import at.fhhgb.mc.notify.sync.ListFiles;
import at.fhhgb.mc.notify.sync.NotifyDrive;

public class MainActivity extends Activity implements MessageHandler, OnClickListener {

	private static final String TAG = "MainActivity";
	private NotifyDrive nd;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
						Log.e("MainActivity", exception.getMessage(), exception);
					}
				});

		Button buttonFile = (Button) findViewById(R.id.createFile);
		buttonFile.setOnClickListener(this);
		Button buttonAppFolder = (Button) findViewById(R.id.createFileAppFolder);
		buttonAppFolder.setOnClickListener(this);
		Button buttonListFiles = (Button) findViewById(R.id.listFiles);
		buttonListFiles.setOnClickListener(this);
		
		nd = new NotifyDrive(this, this);

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
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onMessage(Context context, Bundle message) { // 3
		// display the message contained in the payload
		TextView text = (TextView) findViewById(R.id.label);
		text.setText(message.getString("alert"));
		text.invalidate();
	}

	@Override
	public void onDeleteMessage(Context context, Bundle message) {
		// handle GoogleCloudMessaging.MESSAGE_TYPE_DELETED
	}

	@Override
	public void onError() {
		// handle GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
	}


	/**
	 * Handles resolution callbacks.
	 */
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode,
			Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
		if (_requestCode == NotifyDrive.getRequestCodeResolution() && _resultCode == RESULT_OK) {
			nd.getmGoogleApiClient().connect();
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.createFile: {
			nd.createFile();
		}
			break;
		case R.id.createFileAppFolder: {
			Intent intent = new Intent(getBaseContext(),
					CreateFileAppFolder.class);
			startActivity(intent);
		}
			break;
		case R.id.listFiles: {
			Intent intent = new Intent(getBaseContext(), ListFiles.class);
			startActivity(intent);
		}
			break;
		default: {
//			showMessage("Error");
		}
		}

	}

}
