package at.fhhgb.mc.notify.sync;

import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class DriveNotify {
	
	private static final String TAG = "DriveNotify";

	private static String CLIENT_ID = "428508677848-l4d3k4rs4s7r9oca7dgk0j4c0hbqei55.apps.googleusercontent.com";
	private static String CLIENT_SECRET = "PqD3UQeheLDFRVrd41fyBwfF";

	private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

	public void test() throws IOException {
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET,
				Arrays.asList(DriveScopes.DRIVE)).setAccessType("online")
				.setApprovalPrompt("auto").build();

		String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI)
				.build();
		Log.i(TAG,
				"Please open the following URL in your browser then type the authorization code:");
		Log.i(TAG, "  " + url);
//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String code = "asdf";
		
//		if (code == null) {
//			Log.w(TAG, "flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute() == null");
//			if (flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI) == null) {
//				Log.w(TAG, "flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI) == null"); 
//				if (flow.newTokenRequest(code) == null) {
//					Log.w(TAG, "flow.newTokenRequest(code) == null");
//				} else {
//					Log.i(TAG, "flow.newTokenRequest(code) != null");
//				}
//			} else {
//				Log.i(TAG, "flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI) != null");
//			}
//		} else {
//			Log.i(TAG, "flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute() != null");
//		}

		GoogleTokenResponse response = flow.newTokenRequest(code)
				.setRedirectUri(REDIRECT_URI).execute();
		GoogleCredential credential = new GoogleCredential()
				.setFromTokenResponse(response);

		// Create a new authorized API client
		Drive service = new Drive.Builder(httpTransport, jsonFactory,
				credential).build();

		// Insert a file
		File body = new File();
		body.setTitle("My document");
		body.setDescription("A test document");
		body.setMimeType("text/plain");

		java.io.File fileContent = new java.io.File("document.txt");
		FileContent mediaContent = new FileContent("text/plain", fileContent);

		File file = service.files().insert(body, mediaContent).execute();
		Log.i(TAG, "File ID: " + file.getId());
	}
}