package at.fhhgb.mc.notify.sync;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.stream.StreamResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;

public class NotifyDrive implements SyncIF, ConnectionCallbacks,
		OnConnectionFailedListener {

	private static final String TAG = "NotifyDrive";

	/**
	 * Google API client.
	 */
	private GoogleApiClient mGoogleApiClient;

	/**
	 * Request code for auto Google Play Services error resolution.
	 */
	private static final int REQUEST_CODE_RESOLUTION = 1;
	public static String mExistingFolderId = null;
	public static String mFileName = null;
	private Context mContext;
	private Activity mActivity;
	
	public NotifyDrive() {
	}

	public NotifyDrive(Context _context) {
		mContext = _context;
		init();
	}

	public NotifyDrive(Context _context, Activity _activity) {
		mContext = _context;
		mActivity = _activity;
		init();
	}

	private void init() {
		Log.i(TAG, "init");
		if (mGoogleApiClient == null) {
			Log.i(TAG, "new GoogleApiClient");
			mGoogleApiClient = new GoogleApiClient.Builder(mContext)
					.addApi(Drive.API).addScope(Drive.SCOPE_FILE)
					.addScope(Drive.SCOPE_APPFOLDER)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).build();
		}
		mGoogleApiClient.connect();

	}

	@Override
	public void sync() {
		// TODO Auto-generated method stub

	}

	@Override
	public void createFile() {
		
		if (mExistingFolderId == null) {
			new CreateFolder(mGoogleApiClient, mActivity);
		} else {
			new CreateFile(mGoogleApiClient, mActivity);
		}
		
		new UploadFile(mActivity);

	}

	@Override
	public void onConnectionFailed(ConnectionResult _result) {
		Log.i(TAG, "GoogleApiClient connection failed: " + _result.toString());

		if (mActivity != null) {
			if (!_result.hasResolution()) {
				// show the localized error dialog.
				GooglePlayServicesUtil.getErrorDialog(_result.getErrorCode(),
						mActivity, 0).show();
				return;
			}
			try {
				_result.startResolutionForResult(mActivity,
						REQUEST_CODE_RESOLUTION);
			} catch (SendIntentException e) {
				Log.e(TAG, "Exception while starting resolution activity", e);
			}
		}

	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.i(TAG, "GoogleApiClient connected");

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.i(TAG, "GoogleApiClient connection suspended");

	}

	public GoogleApiClient getmGoogleApiClient() {
		return mGoogleApiClient;
	}

	public static int getRequestCodeResolution() {
		return REQUEST_CODE_RESOLUTION;
	}
	
	 /**
     * Create a new file and save it to Drive.
     */
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        final Bitmap image = mBitmapToSave;
        Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(new ResultCallback<ContentsResult>() {

            @Override
            public void onResult(ContentsResult _result) {
                // If the operation was not successful, we cannot do anything
                // and must
                // fail.
                if (!_result.getStatus().isSuccess()) {
                    Log.i(TAG, "Failed to create new contents.");
                    return;
                }
                // Otherwise, we can write our data to the new contents.
                Log.i(TAG, "New contents created.");
                // Get an output stream for the contents.
                OutputStream outputStream = _result.getContents().getOutputStream();
                // Write the bitmap data from it.
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                StreamResult streamResult = new StreamResult(bos);
                image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                try {
                    outputStream.write(bitmapStream.toByteArray());
                } catch (IOException e1) {
                    Log.i(TAG, "Unable to write file contents.");
                }
                // Create the initial metadata - MIME type and title.
                // Note that the user will be able to change the title later.
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setMimeType("image/jpeg").setTitle("Android Photo.png").build();
                // Create an intent for the file chooser, and start it.
                IntentSender intentSender = Drive.DriveApi
                        .newCreateFileActivityBuilder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialContents(_result.getContents())
                        .build(mGoogleApiClient);
                try {
                    startIntentSenderForResult(
                            intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                } catch (SendIntentException e) {
                    Log.i(TAG, "Failed to launch file chooser.");
                }
            }
        });
    }

}
