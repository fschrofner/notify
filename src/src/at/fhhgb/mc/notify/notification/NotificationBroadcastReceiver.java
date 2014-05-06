package at.fhhgb.mc.notify.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context _context, Intent _intent) {
		
		//check the broadcast, eventhough it should be the only one
		if (_intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
			Intent intent = new Intent(_context,NotificationService.class);
			_context.startService(intent);
		}

	}

}
