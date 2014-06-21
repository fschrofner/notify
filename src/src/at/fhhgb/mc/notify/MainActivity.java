package at.fhhgb.mc.notify;

import java.util.ArrayList;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.unifiedpush.MessageHandler;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.Registrations;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
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
		
		//TODO register push on system start-up
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	String alias = preferences.getString(PushConstants.PUSH_ALIAS, null);
    	
    	if(alias != null){
    		// access the registration object
    	    PushRegistrar push = ((PushApplication) getApplication()).
    	    		getRegistration();

    	    // fire up registration..

    	    // The method will attempt to register the device with GCM and the UnifiedPush server
    	    push.register(getApplicationContext(),new Callback<Void>() { 
    	        private static final long serialVersionUID = 1L;

    	        @Override
    	        public void onSuccess(Void ignore) {
    	            Log.i(TAG, "registration to push service succeeded");
    	        }

    	        @Override
    	        public void onFailure(Exception exception) {
    	            Log.e("MainActivity", exception.getMessage(), exception);
    	        }
    	    });
    	} else {
    		Log.i(TAG, "no push alias saved, did not register for pushes");
    	}
    	
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
		PushSender.sendPushToAlias(text.getText().toString());
	}

}
