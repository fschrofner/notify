package at.fhhgb.mc.notify.sync;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ShareCompat;
import android.util.Log;

public class UploadFile extends NotifyDrive {

	public UploadFile(Activity _context) {
		Log.i("up", "start");
		Uri pdfUri = Uri.parse(_context.getFilesDir() + "/test.xml");
		Intent shareIntent = ShareCompat.IntentBuilder.from(_context)
				.setText("Share PDF doc").setType("application/pdf")
				.setStream(pdfUri).getIntent()
				.setPackage("com.google.android.apps.docs");

		_context.startActivity(shareIntent);
		Log.i("up", "end");
	}

}
