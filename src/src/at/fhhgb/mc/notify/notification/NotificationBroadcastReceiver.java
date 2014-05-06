package at.fhhgb.mc.notify.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;
import android.util.Log;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
	
	static final String TAG = "NotificationBroadcastReceiver";
	
	@Override
	public void onReceive(Context _context, Intent _intent) {
		
		Log.i(TAG, "time tick!");
		
		//check the broadcast, eventhough it should be the only one
		if (_intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
			Log.i(TAG, "time tick!");
			Intent intent = new Intent(_context,NotificationService.class);
			_context.startService(intent);
		}

	}

}
