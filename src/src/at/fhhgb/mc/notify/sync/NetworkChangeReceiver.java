package at.fhhgb.mc.notify.sync;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;
import at.fhhgb.mc.notify.push.PushConstants;
import at.fhhgb.mc.notify.push.PushRegisterReceiver;

public class NetworkChangeReceiver extends BroadcastReceiver {

	final static String TAG = "NetworkChangeReceiver";
	
	@Override
	public void onReceive(Context _context, Intent _intent) {
		if(_intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION){
			boolean isConnected = SyncHandler.networkConnected(_context);
			if(isConnected){
				Log.i(TAG, "network change received! internet now available");
				SharedPreferences outstanding = _context.getSharedPreferences(SyncHandler.OUTSTANDING_TASKS, Context.MODE_PRIVATE); 
				
				//push registration
				if(outstanding.getBoolean(SyncHandler.OUTSTANDING_PUSH_REGISTRATION, false)){
        			Log.i(TAG, "connected to internet, handling outstanding push registration");
        			Intent intent = new Intent(_context.getApplicationContext(), PushRegisterReceiver.class);
        			_context.sendBroadcast(intent);
        		} else {
        			Log.i(TAG, "connected to internet, but no outstanding push registration");
        		}
				
				//send push
        		if(outstanding.getBoolean(SyncHandler.OUTSTANDING_PUSH, false)){
        			Log.i(TAG, "connected to internet, handling outstanding pushes");
        			//use application context here, because receivers are not allowed to bind to services (= connect to host)
        			SyncHandler.sendPush(_context.getApplicationContext());
        		} else {
        			Log.i(TAG, "connected to internet, but no outstanding pushes");
        		}
        		
        		//deletion
        		if(outstanding.contains(SyncHandler.OUTSTANDING_DELETION)){
        			Log.i(TAG, "connected to internet, handling outstanding deletion");
        			HashSet<String> redoFileList = (HashSet<String>) outstanding.getStringSet(SyncHandler.OUTSTANDING_DELETION, null);
        			ArrayList<String> fileList = new ArrayList<String>(redoFileList);
        			SyncHandler.deleteFiles(_context.getApplicationContext(), null, fileList);
        		} else {
        			Log.i(TAG, "connected to internet, but no outstanding deletion");
        		}
        		
        		//download
        		if(outstanding.getBoolean(SyncHandler.OUTSTANDING_DOWNLOAD, false)){
        			Log.i(TAG, "connected to internet, handling outstanding download");
        			SyncHandler.updateFiles(_context.getApplicationContext());
        		} else {
        			Log.i(TAG, "connected to internet, but no outstanding download");
        		}
        		
        		//upload
        		if(outstanding.contains(SyncHandler.OUTSTANDING_UPLOAD)){
        			Log.i(TAG, "connected to internet, handling outstanding upload");
        			HashSet<String> redoFileList = (HashSet<String>) outstanding.getStringSet(SyncHandler.OUTSTANDING_UPLOAD, null);
        			ArrayList<String> fileList = new ArrayList<String>(redoFileList);
        			SyncHandler.uploadFiles(_context.getApplicationContext(), null, fileList);
        		} else {
        			Log.i(TAG, "connected to internet, but no outstanding upload");
        		}
        		
        		outstanding.edit().clear().commit();
			} else {
				Log.i(TAG, "network change received! internet not available");
			}
		} 
	}

}
