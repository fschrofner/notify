package at.fhhgb.mc.notify;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.unifiedpush.MessageHandler;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.Registrations;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;
import at.fhhgb.mc.notify.push.*;

public class MainActivity extends Activity implements MessageHandler{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// access the registration object
	    PushRegistrar push = ((PushApplication) getApplication()).
	    		getRegistration();

	    // fire up registration..

	    // The method will attempt to register the device with GCM and the UnifiedPush server
	    push.register(getApplicationContext(),new Callback<Void>() {   // 2
	        private static final long serialVersionUID = 1L;

	        @Override
	        public void onSuccess(Void ignore) {
	            Toast.makeText(MainActivity.this, "Registration Succeeded!",
	                    Toast.LENGTH_LONG).show();
	        }

	        @Override
	        public void onFailure(Exception exception) {
	            Log.e("MainActivity", exception.getMessage(), exception);
	        }
	    });
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
	    Registrations.registerMainThreadHandler(this);  // 1
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    Registrations.unregisterMainThreadHandler(this); // 2
	}

	@Override
	public void onMessage(Context context, Bundle message) {   // 3
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

}
