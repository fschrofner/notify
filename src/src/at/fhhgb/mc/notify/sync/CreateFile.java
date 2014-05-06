package at.fhhgb.mc.notify.sync;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;

import android.app.Activity;
import android.util.Log;

public class CreateFile extends NotifyDrive {
	
	private DriveId mFolderDriveId;
	private static final String TAG = "CreateFile";
	private GoogleApiClient mGoogleApiClient; 

    public CreateFile(GoogleApiClient _GoogleApiClient, Activity _activity) {
    	Log.i(TAG, "constructor");
    	
    	mGoogleApiClient = _GoogleApiClient;
    	
        Drive.DriveApi.fetchDriveId(mGoogleApiClient, mExistingFolderId)
                .setResultCallback(idCallback);
    }

    final private ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
        @Override
        public void onResult(DriveIdResult result) {
        	Log.i(TAG, "idCallback");
            if (!result.getStatus().isSuccess()) {
                return;
            }
            mFolderDriveId = result.getDriveId();
            Drive.DriveApi.newContents(mGoogleApiClient)
                    .setResultCallback(contentsResult);
        }
    };

    final private ResultCallback<ContentsResult> contentsResult = new
            ResultCallback<ContentsResult>() {
        @Override
        public void onResult(ContentsResult result) {
        	Log.i(TAG, "contentsResult");
            if (!result.getStatus().isSuccess()) {
                return;
            }
            DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, mFolderDriveId);
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(mFileName)
                    .setMimeType("text/plain")
                    .setStarred(true).build();
            folder.createFile(mGoogleApiClient, changeSet, result.getContents())
                    .setResultCallback(fileCallback);
        }
    };

    final private ResultCallback<DriveFileResult> fileCallback = new
            ResultCallback<DriveFileResult>() {
        @Override
        public void onResult(DriveFileResult result) {
        	Log.i(TAG, "fileCallback");
            if (!result.getStatus().isSuccess()) {
                return;
            }
        }
    };

}
