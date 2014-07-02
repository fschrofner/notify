package at.fhhgb.mc.notify.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import at.fhhgb.mc.notify.notification.Notification;
import at.fhhgb.mc.notify.sync.SyncHandler;

/**
 * Parses a xml file and returns a corresponding notification object.
 * @author Dominik Koeltringer & Florian Schrofner
 *
 */
public class XmlParser {
	
	private final static String TAG = "XmlParser";
	
	private Context mContext;
	private Notification mNotification;
	
	public XmlParser(Context _context) {
		mContext = _context;
	}
	
	/**
	 * Reads the file with the given file name and returns a notification object out of the information
	 * @param _fileName the file to read
	 * @return a notification object representing the file
	 * @throws IOException
	 */
	public Notification readXml(String _fileName) throws IOException{
		mNotification = new Notification();
		InputStream input = null;
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			input = new FileInputStream(SyncHandler.getFullPath(_fileName));
			parser.setInput(input, null);
			parser.nextTag();
			read(parser);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(input != null){
				input.close();					//closes the inputstream in the end
			}		
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(_fileName);
		String uniqueIDString;
		String version;
		
		// reads the unique ID
		if (sb.indexOf("_") == -1) {
			Log.w(TAG, "filename " + _fileName + " invalid");
			uniqueIDString = "-1";
		} else {
			uniqueIDString = sb.substring(0, sb.indexOf("_"));
		}
		
		mNotification.setUniqueID(Long.valueOf(uniqueIDString));
		Log.i(TAG, "unique ID set as: " + mNotification.getUniqueIDString());
		
		sb.replace(0, sb.length(), _fileName);
		
		// reads the version
		if (sb.indexOf("_") == -1) {
			Log.w(TAG, "filename " + _fileName + " invalid");
			version = "-1";
		} else {
			version = sb.substring(sb.indexOf("_") + 1, sb.indexOf("."));
		}
		
		mNotification.setVersion(Integer.parseInt(version));
		Log.i(TAG, "Version set as: " + mNotification.getVersion());
		
		return mNotification;
	}
	
	
	/**
	 * Reads the information out of the parser and sets the matching values.
	 * @param _parser the parser of which to read the values
	 * @throws Exception
	 */
	private void read(XmlPullParser _parser) throws Exception{
		_parser.require(XmlPullParser.START_TAG, null, Notification.KEY_ROOT);
		
		//goes through all tags inside the xml file
		while (_parser.next() != XmlPullParser.END_TAG) {
			if (_parser.getEventType() != XmlPullParser.START_TAG) {
				continue;												//skips this turn if the tag is not a start tag
			}
			
			String name = _parser.getName();
			// Starts by looking for the entry tag
			if (name.equals(Notification.KEY_TITLE)) {
				setTitle(_parser);
			} else if (name.equals(Notification.KEY_DATE)) {	
				setDate(_parser);
			} else if (name.equals(Notification.KEY_TIME)){
				setTime(_parser);
			} else if (name.equals(Notification.KEY_MESSAGE)){
				setMessage(_parser);
			} else if (name.equals(Notification.KEY_FILE)) {
				setFiles(_parser);
			} else {
				//invalid tag, will be skipped
				Log.w("XmlParser", "Skip!");
			}
			_parser.nextTag();
		}
	}
	
	private void setTitle(XmlPullParser _parser) throws Exception{
		mNotification.setTitle(_parser.getAttributeValue(null,Notification.ATTRIBUTE_CONTENT));
		Log.i(TAG, "title set as: " + mNotification.getTitle());
	}
	
	private void setDate(XmlPullParser _parser) throws XmlPullParserException, IOException{
		String year = _parser.getAttributeValue(null,Notification.ATTRIBUTE_START_YEAR);
		String month = _parser.getAttributeValue(null,Notification.ATTRIBUTE_START_MONTH);
		String day = _parser.getAttributeValue(null,Notification.ATTRIBUTE_START_DAY);
		mNotification.setStartDate(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day));
		year = _parser.getAttributeValue(null,Notification.ATTRIBUTE_END_YEAR);
		month = _parser.getAttributeValue(null,Notification.ATTRIBUTE_END_MONTH);
		day = _parser.getAttributeValue(null,Notification.ATTRIBUTE_END_DAY);
		mNotification.setEndDate(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day));
		
		Log.i(TAG, "set start date: " + mNotification.getStartYear() + "/" + mNotification.getStartMonth() + "/" + mNotification.getStartDay());
		Log.i(TAG, "set end date: " + mNotification.getEndYear() + "/" + mNotification.getEndMonth() + "/" + mNotification.getEndDay());

		
	}
	
	private void setTime(XmlPullParser _parser){
		String hours = _parser.getAttributeValue(null,Notification.ATTRIBUTE_START_HOURS);
		String minutes = _parser.getAttributeValue(null,Notification.ATTRIBUTE_START_MINUTES);
		mNotification.setStartTime(Integer.valueOf(hours), Integer.valueOf(minutes));
		hours = _parser.getAttributeValue(null,Notification.ATTRIBUTE_END_HOURS);
		minutes = _parser.getAttributeValue(null,Notification.ATTRIBUTE_END_MINUTES);
		mNotification.setEndTime(Integer.valueOf(hours), Integer.valueOf(minutes));
		Log.i(TAG, "set start time as: " + mNotification.getStartHours() + ":" + mNotification.getStartMinutes());
		Log.i(TAG, "set end time as: " + mNotification.getEndHours() + ":" + mNotification.getEndMinutes());
	}
	
	private void setMessage(XmlPullParser _parser){
		mNotification.setMessage(_parser.getAttributeValue(null,Notification.ATTRIBUTE_CONTENT));
		Log.i(TAG, "set message as: " + mNotification.getMessage());
	}
	
	private void setFiles(XmlPullParser _parser){
		if(mNotification.getFiles() == null){
			ArrayList<String> files = new ArrayList<String>();
			files.add(_parser.getAttributeValue(null,Notification.ATTRIBUTE_PATH));
			mNotification.setFiles(files);
			Log.i(TAG, "created new filelist");
			Log.i(TAG, "added file: " + mNotification.getFiles().get(mNotification.getFiles().size() - 1));
		} else {
			ArrayList<String> files = mNotification.getFiles();
			files.add(_parser.getAttributeValue(null,Notification.ATTRIBUTE_PATH));
			mNotification.setFiles(files);
			Log.i(TAG, "added file: " + mNotification.getFiles().get(mNotification.getFiles().size() - 1));
		}
	}
	
}
