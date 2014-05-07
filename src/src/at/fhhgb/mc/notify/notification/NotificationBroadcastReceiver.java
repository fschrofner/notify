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
		Log.i(TAG, "broadcast received");
		//check the broadcast, eventhough it should be the only one
		if (_intent.getAction().equals(Notification.ACTION_ALARM)) {
			Log.i(TAG, "alarm triggered!");
			Intent intent = new Intent(_context,NotificationService.class);
			intent.setAction(Notification.ACTION_ALARM);
			_context.startService(intent);
		}

	}

}
