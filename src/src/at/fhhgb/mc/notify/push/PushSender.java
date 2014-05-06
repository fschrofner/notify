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

import android.util.Log;

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
	public static void sendPushToAlias(String _alias){
		try {
			PushThread pushThread = new PushThread(_alias);
			Thread thread = new Thread(pushThread);
			thread.start();
			Log.i(TAG, "started push thread");
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
			Log.i(TAG, "alias was set to: " + alias);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

	    }
    }
}
