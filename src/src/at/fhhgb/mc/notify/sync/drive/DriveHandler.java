package at.fhhgb.mc.notify.sync.drive;

import java.util.Collections;

import android.content.Context;
import android.content.Intent;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

public class DriveHandler {
	static Drive service;
	static GoogleAccountCredential credential;
	final String TAG = "DriveHandler";

	static public void authenticate(Context _context) {
		credential = GoogleAccountCredential.usingOAuth2(_context,
				Collections.singleton(DriveScopes.DRIVE));
		Intent intent = new Intent(_context,at.fhhgb.mc.notify.sync.drive.AuthenticationActivity.class);
		_context.startActivity(intent);
	}
}
