package at.fhhgb.mc.notify.sync.drive;

import java.io.IOException;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class UploadThread implements Runnable {

	final String TAG = "CommunicationThread";
	Context mContext;
	Activity mActivity;
	
	public UploadThread(Context _context, Activity _activity){
		mContext = _context;
		mActivity = _activity;
	}
	
	@Override
	public void run() {
		// credential.setSelectedAccountName(accountName);
		try{
			Log.i(TAG, "started upload thread");
			at.fhhgb.mc.notify.sync.drive.DriveHandler.service = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
					new GsonFactory(), at.fhhgb.mc.notify.sync.drive.DriveHandler.credential).build();
			java.io.File file = new java.io.File("/storage/sdcard0/Download/test.md");
			File body = new File();
			body.setTitle(file.getName());
			//body.setDescription("A test document");
			body.setMimeType("text/plain");
			FileContent mediaContent = new FileContent("text/plain", file);
			File resultFile;
			resultFile = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.files().insert(body, mediaContent).execute();
			Log.i(TAG, "File ID: " + resultFile.getId());
		} catch (UserRecoverableAuthIOException e) {
	          mActivity.startActivityForResult(e.getIntent(), at.fhhgb.mc.notify.sync.drive.AuthenticationActivity.REQUEST_AUTHENTICATION);
	        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
