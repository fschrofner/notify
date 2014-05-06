package at.fhhgb.mc.notify.sync;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;

import android.os.Bundle;
import android.widget.ListView;
import at.fhhgb.mc.notify.MainActivity;
import at.fhhgb.mc.notify.R;

public class ListFiles {
	
	private ListView mResultsListView;
    private ResultsAdapter mResultsAdapter;

//    @Override
//    public void onConnected(Bundle connectionHint) {
//        super.onCreate(connectionHint);
//        setContentView(R.layout.activity_listfiles);
//        mResultsListView = (ListView) findViewById(R.id.listViewResults);
//        mResultsAdapter = new ResultsAdapter(this);
//        mResultsListView.setAdapter(mResultsAdapter);
//
//        Drive.DriveApi.fetchDriveId(getGoogleApiClient(), existingFolderId)
//                .setResultCallback(idCallback);
//    }
//
//    final private ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
//        @Override
//        public void onResult(DriveIdResult result) {
//            if (!result.getStatus().isSuccess()) {
//                showMessage("Cannot find DriveId. Are you authorized to view this file?");
//                return;
//            }
//            DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(), result.getDriveId());
//            folder.listChildren(getGoogleApiClient())
//                    .setResultCallback(metadataResult);
//        }
//    };
//
//    final private ResultCallback<MetadataBufferResult> metadataResult = new
//            ResultCallback<MetadataBufferResult>() {
//        @Override
//        public void onResult(MetadataBufferResult result) {
//            if (!result.getStatus().isSuccess()) {
//                showMessage("Problem while retrieving files");
//                return;
//            }
//            mResultsAdapter.clear();
//            mResultsAdapter.append(result.getMetadataBuffer());
//            showMessage("Successfully listed files.");
//        }
//    };

}
