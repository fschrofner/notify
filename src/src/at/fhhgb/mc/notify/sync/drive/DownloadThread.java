package at.fhhgb.mc.notify.sync.drive;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import android.content.Context;
import android.util.Log;
import at.fhhgb.mc.notify.sync.SyncHandler;

public class DownloadThread implements Runnable {

	final static String TAG = "DownloadThread";
	Context mContext;
	ArrayList<File> mFiles;
	
	public DownloadThread(ArrayList<File> _files,Context _context){
		mContext = _context;
		mFiles = _files;
	}
	
	@Override
	public void run() {
		Log.i(TAG, "started download thread");
		at.fhhgb.mc.notify.sync.drive.DriveHandler.service = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), at.fhhgb.mc.notify.sync.drive.DriveHandler.credential).build();
		InputStream inputStream = null;
		OutputStream outputStream = null;
		 try {
			for(int i=0;i<mFiles.size();i++){
				HttpResponse resp = at.fhhgb.mc.notify.sync.drive.DriveHandler.service.getRequestFactory().
						 buildGetRequest(new GenericUrl(mFiles.get(i).getDownloadUrl())).execute();
				inputStream = resp.getContent();
				outputStream = new FileOutputStream(new java.io.File(mContext.getFilesDir() + "/" + 
						SyncHandler.NOTIFICATION_FOLDER + mFiles.get(i).getOriginalFilename()));
				int read = 0;
				byte[] bytes = new byte[1024];
		 
				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
				Log.i(TAG, "file complete!");
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO organise files in sub-folders
	}

}
