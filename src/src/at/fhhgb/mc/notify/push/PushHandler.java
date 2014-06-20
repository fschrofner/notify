package at.fhhgb.mc.notify.push;

import org.jboss.aerogear.android.unifiedpush.MessageHandler;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import at.fhhgb.mc.notify.R;
import at.fhhgb.mc.notify.sync.SyncHandler;

public class PushHandler implements MessageHandler {

	public static final int NOTIFICATION_ID = 1;
	final static String TAG = "PushHandler";

	@Override
	public void onDeleteMessage(Context context, Bundle message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(Context context, Bundle message) {		
		Log.i(TAG, "received push!");
        SyncHandler.updateFiles(context.getApplicationContext());
	}

	@Override
	public void onError() {

	}

}
