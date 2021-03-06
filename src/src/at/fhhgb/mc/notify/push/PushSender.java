package at.fhhgb.mc.notify.push;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import at.fhhgb.mc.notify.sync.SyncHandler;

/**
 * Class to simplify pushes to other devices.
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */
public final class PushSender {
	
	private static String TAG = "PushSender";
	
	/**
	 * Static method that is used to send a push notification to the specified alias.
	 * Starts a separate thread.
	 * @param _alias the alias you want to send a push to
	 */
	public static void sendPushToAlias(String _alias, Context _context){
		try {
			//TODO check for internet connectivity first
        	boolean isConnected = SyncHandler.networkConnected(_context);
        	if(isConnected){
    			PushThread pushThread = new PushThread(_alias);
    			Thread thread = new Thread(pushThread);
    			thread.start();
    			Log.i(TAG, "connected to internet, started push thread");
        	} else {
        		//TODO schedule push when connected
        		Log.i(TAG, "no internet connection! push scheduled on connectivity change");
        		SharedPreferences outstanding = _context.getSharedPreferences(SyncHandler.OUTSTANDING_TASKS, Context.MODE_PRIVATE); 
        		outstanding.edit().putBoolean(SyncHandler.OUTSTANDING_PUSH, true).commit();
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}

/**
 * Class that is used to run the HTTP Request in a separate thread.
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */
class PushThread implements Runnable {
	private static String TAG = "PushThread";
	private String alias;
	
	public PushThread(String _alias) throws Exception{
		if(_alias == null){
			throw new Exception("alias must not be null!");
		} else {
			alias = _alias;
			Log.i(TAG, "alias " + alias + " will receive push");
		}
	}
	
    @Override
    public void run() {
    	HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(at.fhhgb.mc.notify.push.PushConstants.UNIFIED_PUSH_BROKER_URL);
		
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>(1);
		parameters.add(new BasicNameValuePair("alias", alias));
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
			    InputStream instream = entity.getContent();
			    Log.i(TAG, "http result: " + instream.toString());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

	    }
    }
}
