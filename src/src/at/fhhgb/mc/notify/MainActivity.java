package at.fhhgb.mc.notify;

import java.util.ArrayList;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.unifiedpush.MessageHandler;
import org.jboss.aerogear.android.unifiedpush.PushConfig;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.Registrations;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import at.fhhgb.mc.notify.push.*;
import at.fhhgb.mc.notify.sync.SyncHandler;

public class MainActivity extends Activity implements OnClickListener{
	
	static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		Intent intent = new Intent(this, PushRegisterReceiver.class);
		sendBroadcast(intent);
    	    	
		SyncHandler.updateFiles(this);
		
	    Button pushButton = (Button)findViewById(R.id.push_button);
	    pushButton.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		EditText text = (EditText)findViewById(R.id.alias_text);
		PushSender.sendPushToAlias(text.getText().toString(),this);
	}

}
