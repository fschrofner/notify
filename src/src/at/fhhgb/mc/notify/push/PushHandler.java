package at.fhhgb.mc.notify.push;

import org.jboss.aerogear.android.unifiedpush.MessageHandler;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import at.fhhgb.mc.notify.sync.SyncHandler;

/**
 * MessageHandler that handles incoming pushes.
 * Initiates download of files from host.
 * @author Dominik Koeltringer & Florian Schrofner
 *
 */
public class PushHandler implements MessageHandler {

	public static final int NOTIFICATION_ID = 1;
	final static String TAG = "PushHandler";

	@Override
	public void onDeleteMessage(Context context, Bundle message) {

	}

	@Override
	public void onMessage(Context context, Bundle message) {		
		Log.i(TAG, "received push!");
        SyncHandler.updateFiles(context.getApplicationContext(),null);
	}

	@Override
	public void onError() {

	}

}
