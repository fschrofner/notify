package at.fhhgb.mc.notify.sync;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;

import android.app.Activity;
import android.util.Log;

public class CreateFolder extends NotifyDrive {
	
	private static final String TAG = "CreateFolder";
	private GoogleApiClient mGoogleApiClient;
	private Activity mActivity;
	
	public CreateFolder(GoogleApiClient _GoogleApiClient, Activity _activity) {
		Log.i(TAG, "constructor");
		
		mActivity = _activity;
		mGoogleApiClient = _GoogleApiClient;

		MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(
				"Notify").build();
		Drive.DriveApi.getRootFolder(mGoogleApiClient)
				.createFolder(mGoogleApiClient, changeSet)
				.setResultCallback(callback);
	}

	final ResultCallback<DriveFolderResult> callback = new ResultCallback<DriveFolderResult>() {
		@Override
		public void onResult(DriveFolderResult result) {
			Log.i(TAG, "ResultCalback");
			if (!result.getStatus().isSuccess()) {
				return;
			}

			mExistingFolderId = result.getDriveFolder().getDriveId().getResourceId();

			new CreateFile(mGoogleApiClient, mActivity);
		}
	};

}
