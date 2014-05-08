package at.fhhgb.mc.notify.xml;

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

public class XmlParser {
	Context mContext;
	Notification mNotification;
	
	public XmlParser(Context _context) {
		mContext = _context;
		mNotification = new Notification();
	}
	
	public Notification readXml(String _uniqueID) throws IOException{
		InputStream input = null;
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			input = mContext.openFileInput(_uniqueID+".xml");
			parser.setInput(input, null);
			parser.nextTag();
			read(parser);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
				input.close();					//closes the inputstream in the end
		}
		mNotification.setUniqueID(Long.valueOf(_uniqueID));
		return mNotification;
	}
	
	private void read(XmlPullParser _parser) throws XmlPullParserException, IOException{
		_parser.require(XmlPullParser.START_TAG, null, Notification.KEY_ROOT);
		
		//goes through all tags inside the xml file
		while (_parser.next() != XmlPullParser.END_TAG) {
			if (_parser.getEventType() != XmlPullParser.START_TAG) {
				continue;												//skips this turn if the tag is not a start tag
			}
			
			String name = _parser.getName();
			// Starts by looking for the entry tag
			if (name.equals(Notification.KEY_TITLE)) {
				try {
					mNotification.setTitle(_parser.getAttributeValue(null,Notification.ATTRIBUTE_CONTENT));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				_parser.nextTag();
			} else if (name.equals(Notification.KEY_DATE)) {	
				String year = _parser.getAttributeValue(null,Notification.ATTRIBUTE_START_YEAR);
				String month = _parser.getAttributeValue(null,Notification.ATTRIBUTE_START_MONTH);
				String day = _parser.getAttributeValue(null,Notification.ATTRIBUTE_START_DAY);
				mNotification.setStartDate(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day));
				year = _parser.getAttributeValue(null,Notification.ATTRIBUTE_END_YEAR);
				month = _parser.getAttributeValue(null,Notification.ATTRIBUTE_END_MONTH);
				day = _parser.getAttributeValue(null,Notification.ATTRIBUTE_END_DAY);
				mNotification.setEndDate(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day));
				_parser.nextTag();
			} else if (name.equals(Notification.KEY_TIME)){
				String hours = _parser.getAttributeValue(null,Notification.ATTRIBUTE_START_HOURS);
				String minutes = _parser.getAttributeValue(null,Notification.ATTRIBUTE_START_MINUTES);
				mNotification.setStartTime(Integer.valueOf(hours), Integer.valueOf(minutes));
				hours = _parser.getAttributeValue(null,Notification.ATTRIBUTE_END_HOURS);
				minutes = _parser.getAttributeValue(null,Notification.ATTRIBUTE_END_MINUTES);
				mNotification.setEndTime(Integer.valueOf(hours), Integer.valueOf(minutes));
				_parser.nextTag();
			} else if (name.equals(Notification.KEY_MESSAGE)){
				mNotification.setMessage(_parser.getAttributeValue(null,Notification.ATTRIBUTE_CONTENT));
				_parser.nextTag();
			} else if (name.equals(Notification.KEY_FILE)) {
				if(mNotification.getFiles() == null){
					ArrayList<String> files = new ArrayList<String>();
					files.add(_parser.getAttributeValue(null,Notification.ATTRIBUTE_PATH));
					mNotification.setFiles(files);
					_parser.nextTag();
				} else {
					ArrayList<String> files = mNotification.getFiles();
					files.add(_parser.getAttributeValue(null,Notification.ATTRIBUTE_PATH));
					mNotification.setFiles(files);
					_parser.nextTag();
				}
			} else {
				//invalid tag, will be skipped
				Log.w("XmlParser", "Skip!");
				_parser.nextTag();
			}
			
		}
	}
	
}
