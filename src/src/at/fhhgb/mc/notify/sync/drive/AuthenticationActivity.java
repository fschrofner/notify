package at.fhhgb.mc.notify.sync.drive;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import at.fhhgb.mc.notify.sync.SyncHandler;

public class AuthenticationActivity extends Activity {
	final static String TAG = "AuthenticationActivity";
	final static int REQUEST_ACCOUNT_PICKER = 1;
	final static int REQUEST_AUTHENTICATION = 2;
	
	String[] mFileList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mFileList = intent.getStringArrayExtra(SyncHandler.UPLOAD_FILE_LIST);
		startActivityForResult(at.fhhgb.mc.notify.sync.drive.DriveHandler.credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "activity result received!");
		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:	
			Log.i(TAG, "account picker requested");
			if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
		        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
		        Log.i(TAG, "selected account: " + accountName);
		        if (accountName != null) {
		          at.fhhgb.mc.notify.sync.drive.DriveHandler.credential.setSelectedAccountName(accountName);
		          at.fhhgb.mc.notify.sync.drive.DriveHandler.service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), 
		        		  new GsonFactory(), at.fhhgb.mc.notify.sync.drive.DriveHandler.credential).build();
		          
		          SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		          preferences.edit().putString(SyncHandler.GOOGLE_DRIVE, accountName).commit();
		          Log.i(TAG, "saved account name in shared preferences");
		          
		          //uploads and updates the files again after authentication
		          if(mFileList != null){
		        	  SyncHandler.uploadFiles(getApplicationContext(), this, new ArrayList<String>(Arrays.asList(mFileList)));
		          } else {
			          SyncHandler.updateFiles(getApplicationContext());
		          }	          
		        }
		      }
			break;
		case REQUEST_AUTHENTICATION:
			Log.i(TAG, "authentication requested");
			if (resultCode == Activity.RESULT_OK) {
				Log.i(TAG, "authentication given!");
			} else {
			    	 startActivityForResult(at.fhhgb.mc.notify.sync.drive.DriveHandler.credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
			}
			break;
		}
	}
}
