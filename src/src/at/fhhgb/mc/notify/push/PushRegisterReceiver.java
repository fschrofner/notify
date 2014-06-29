package at.fhhgb.mc.notify.push;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.unifiedpush.PushConfig;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.Registrations;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import at.fhhgb.mc.notify.notification.Notification;
import at.fhhgb.mc.notify.notification.NotificationService;
import at.fhhgb.mc.notify.sync.SyncHandler;

public class PushRegisterReceiver extends BroadcastReceiver {

	final static String TAG = "PushRegisterReceiver";
	@Override
	public void onReceive(Context _context, Intent _intent) {
		
		Log.i(TAG, "received intent! registering for pushes now..");
		registerForPushes(_context.getApplicationContext());
		
		//scheduling a download for the first network connection
		if(_intent.getAction() != null && _intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			Log.i(TAG, "device rebooted, scheduling first file sync");
			SharedPreferences outstanding = _context.getSharedPreferences(SyncHandler.OUTSTANDING_TASKS, Context.MODE_PRIVATE); 
			outstanding.edit().putBoolean(SyncHandler.OUTSTANDING_DOWNLOAD, true).commit();
			
			Intent intent = new Intent(_context, NotificationService.class);
			intent.setAction(Notification.ACTION_START_SERVICE);
			_context.getApplicationContext().startService(intent);
		}
	}
	
	private void registerForPushes(Context _context){
		//schedules the push registration if there's currently no internet connection
    	if(SyncHandler.networkConnected(_context)){
    		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
        	String alias = preferences.getString(PushConstants.PUSH_ALIAS, null);
    		if(alias != null){
        		// access the registration object     		
        	    PushRegistrar push = ((PushApplication) _context).
        	    		getRegistration();

        	    // fire up registration..

        	    // The method will attempt to register the device with GCM and the UnifiedPush server
        	    push.register(_context ,new Callback<Void>() { 
        	        private static final long serialVersionUID = 1L;

        	        @Override
        	        public void onSuccess(Void ignore) {
        	            Log.i(TAG, "registration to push service succeeded");
        	        }

        	        @Override
        	        public void onFailure(Exception exception) {
        	            Log.e(TAG, exception.getMessage(), exception);
        	        }
        	    });
        	} else {
        		Log.i(TAG, "no push alias saved, did not register for pushes");
        	}	
    	} else {
    		Log.i(TAG, "currently no internet connection, scheduling registration for pushes");
    		SharedPreferences outstanding = _context.getSharedPreferences(SyncHandler.OUTSTANDING_TASKS, Context.MODE_PRIVATE); 
			outstanding.edit().putBoolean(SyncHandler.OUTSTANDING_PUSH_REGISTRATION, true).commit();
    	}
	}

}
