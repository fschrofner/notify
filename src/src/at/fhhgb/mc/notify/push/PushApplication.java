package at.fhhgb.mc.notify.push;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.aerogear.android.unifiedpush.PushConfig;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.Registrations;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import at.fhhgb.mc.notify.sync.SyncHandler;
import at.fhhgb.mc.notify.sync.drive.DriveHandler;

public class PushApplication extends Application {
	
	 // used for 'selective send' to target a specific user
    // it can be any arbitary value (e.g. name, email etc)
    private final String MY_ALIAS = "devtest";

    final static String TAG = "PushApplication";
    private PushRegistrar registration;

    @Override
    public void onCreate() {
    	
    	Log.d(TAG, "ON_CREATE");
        super.onCreate();

        Registrations registrations = new Registrations();
        DriveHandler.setup(getApplicationContext());

        try {
        	
        	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        	String alias = preferences.getString(PushConstants.PUSH_ALIAS, null);
        	
        	if(alias != null){
                PushConfig config = new PushConfig(new URI(PushConstants.UNIFIED_PUSH_URL), 
                		PushConstants.GCM_SENDER_ID);
                config.setVariantID(PushConstants.VARIANT_ID);
                config.setSecret(PushConstants.SECRET);
                config.setAlias(alias);

                registration = registrations.push("unifiedpush", config);
                Log.i(TAG, "registered pushes for alias " + alias + " in shared preferences");
        	} else {
        		Log.i(TAG, "no alias saved, did not register for pushes!");
        	}


        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // Accessor method for Activities to access the 'PushRegistrar' object
    public PushRegistrar getRegistration() {
        return registration;
    }

	@Override
	public void onTerminate() {
		Log.d(TAG, "ON_Terminate");
		super.onTerminate();
	}
    
    
}
