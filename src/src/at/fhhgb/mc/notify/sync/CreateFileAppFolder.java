package at.fhhgb.mc.notify.sync;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;

import android.os.Bundle;
import at.fhhgb.mc.notify.MainActivity;

public class CreateFileAppFolder {

//	@Override
//	public void onConnected(Bundle connectionHint) {
//		super.onConnected(connectionHint);
//		// create new contents resource
//		Drive.DriveApi.newContents(getGoogleApiClient()).setResultCallback(
//				contentsCallback);
//	}
//
//	final private ResultCallback<ContentsResult> contentsCallback = new ResultCallback<ContentsResult>() {
//		@Override
//		public void onResult(ContentsResult result) {
//			if (!result.getStatus().isSuccess()) {
//				showMessage("Error while trying to create new file contents");
//				return;
//			}
//
//			MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
//					.setTitle("fileName").setMimeType("text/plain").build();
//			Drive.DriveApi
//					.getAppFolder(getGoogleApiClient())
//					.createFile(getGoogleApiClient(), changeSet,
//							result.getContents())
//					.setResultCallback(fileCallback);
//		}
//	};
//
//	final private ResultCallback<DriveFileResult> fileCallback = new ResultCallback<DriveFileResult>() {
//		@Override
//		public void onResult(DriveFileResult result) {
//			if (!result.getStatus().isSuccess()) {
//				showMessage("Error while trying to create the file");
//				return;
//			}
//			showMessage("Created a file in App Folder: "
//					+ result.getDriveFile().getDriveId());
//		}
//	};

}
