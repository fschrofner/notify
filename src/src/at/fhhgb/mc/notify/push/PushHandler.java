package at.fhhgb.mc.notify.push;

import org.jboss.aerogear.android.unifiedpush.MessageHandler;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import at.fhhgb.mc.notify.R;
import at.fhhgb.mc.notify.sync.SyncHandler;

public class PushHandler implements MessageHandler {

	public static final int NOTIFICATION_ID = 1;

	@Override
	public void onDeleteMessage(Context context, Bundle message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(Context context, Bundle message) {
		
		String msg = message.getString("alert");
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Notify")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        
        //TODO handle message when app is in foreground
        SyncHandler.updateFiles(context.getApplicationContext());
	}

	@Override
	public void onError() {

	}

}
