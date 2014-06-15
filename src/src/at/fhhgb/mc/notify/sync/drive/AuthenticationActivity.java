package at.fhhgb.mc.notify.sync.drive;

import java.util.ArrayList;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import at.fhhgb.mc.notify.sync.SyncHandler;

public class AuthenticationActivity extends Activity {
	final static String TAG = "AuthenticationActivity";
	final static int REQUEST_ACCOUNT_PICKER = 1;
	final static int REQUEST_AUTHENTICATION = 2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		          //TODO place the methods in the right class
		          //SyncHandler.updateFiles(this);
		          ArrayList<String> fileList = new ArrayList<String>();
		          fileList.add("test.md");
		          fileList.add("test.xml");
		          SyncHandler.uploadFiles(this, this,fileList);
		        }
		      }
			break;
		case REQUEST_AUTHENTICATION:
			Log.i(TAG, "authentication requested");
			if (resultCode == Activity.RESULT_OK) {
//			         UploadThread commThread = new UploadThread(getApplicationContext(), this, FILENAME);
//			         Thread thread = new Thread(commThread);
//			  		 thread.start();
			} else {
			    	 startActivityForResult(at.fhhgb.mc.notify.sync.drive.DriveHandler.credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
			}
			break;
		}
	}
}
