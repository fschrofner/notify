package at.fhhgb.mc.notify.xml;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;
import android.util.Log;
import at.fhhgb.mc.notify.notification.Notification;
import at.fhhgb.mc.notify.sync.SyncHandler;

/**
 * Class used to build a xml file out of a notification object.
 * @author Dominik Koeltringer & Florian Schrofner
 *
 */
public class XmlCreator {
	
	final static String TAG = "XmlCreator";
	
	DocumentBuilderFactory mBuildFactory;
	DocumentBuilder mBuilder;
	TransformerFactory mTransFactory;
	Transformer mTransformer;
	Document mDocument;
	
	public XmlCreator(){
		mBuildFactory = DocumentBuilderFactory.newInstance();
		mTransFactory = TransformerFactory.newInstance();
		try {
			mBuilder = mBuildFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a xml file out of the specified notification object.
	 * @param _notification the notification object out of which to create the xml file.
	 * @param _context context needed for some methods
	 */
	public void create(Notification _notification, Context _context){
		mDocument = mBuilder.newDocument();
		
		//create the root element
		Element rootElement = mDocument.createElement(Notification.KEY_ROOT);
		mDocument.appendChild(rootElement);
		
		try {
			ArrayList<String> attributes = new ArrayList<String>();
			ArrayList<String> values = new ArrayList<String>();
			
			//set title
			createElement(rootElement,Notification.KEY_TITLE,Notification.ATTRIBUTE_CONTENT,_notification.getTitle());
			
			//set date
			attributes.add(Notification.ATTRIBUTE_START_YEAR);
			values.add(String.valueOf(_notification.getStartYear()));
			attributes.add(Notification.ATTRIBUTE_START_MONTH);
			values.add(String.valueOf(_notification.getStartMonth()));
			attributes.add(Notification.ATTRIBUTE_START_DAY);
			values.add(String.valueOf(_notification.getStartDay()));
			attributes.add(Notification.ATTRIBUTE_END_YEAR);
			values.add(String.valueOf(_notification.getEndYear()));
			attributes.add(Notification.ATTRIBUTE_END_MONTH);
			values.add(String.valueOf(_notification.getEndMonth()));
			attributes.add(Notification.ATTRIBUTE_END_DAY);
			values.add(String.valueOf(_notification.getEndDay()));
			
			createElement(rootElement,Notification.KEY_DATE,attributes,values);
			
			attributes.clear();
			values.clear();
			
			//set time
			attributes.add(Notification.ATTRIBUTE_START_HOURS);
			values.add(String.valueOf(_notification.getStartHours()));
			attributes.add(Notification.ATTRIBUTE_START_MINUTES);
			values.add(String.valueOf(_notification.getStartMinutes()));
			attributes.add(Notification.ATTRIBUTE_END_HOURS);
			values.add(String.valueOf(_notification.getEndHours()));
			attributes.add(Notification.ATTRIBUTE_END_MINUTES);
			values.add(String.valueOf(_notification.getEndMinutes()));
			
			createElement(rootElement,Notification.KEY_TIME,attributes,values);
			
			//set message, if there is one
			if(_notification.getMessage() != null){
				createElement(rootElement,Notification.KEY_MESSAGE,Notification.ATTRIBUTE_CONTENT,_notification.getMessage());
			}
			
			//set files if there are any
			if(_notification.getFiles() != null){
				for(int i=0;i<_notification.getFiles().size();i++){
					createElement(rootElement,Notification.KEY_FILE,Notification.ATTRIBUTE_PATH,String.valueOf(_notification.getFiles().get(i)));
				}
			}
			
			writeToFile(_notification.getUniqueIDString() + "_" + _notification.getVersion() + "." + SyncHandler.NOTIFICATION_FILE_EXTENSION, generateString(mDocument), _context);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Creates a string out of the given document.
	 * @param _notification the document out of which to create a string
	 * @return a string defining the given document
	 */
	private String generateString(Document _notification){	
		// specifies the final output
		Properties outputProperties = new Properties();
		outputProperties.setProperty(OutputKeys.INDENT, "yes");
		outputProperties.setProperty(OutputKeys.METHOD, "xml");
		outputProperties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		outputProperties.setProperty(OutputKeys.VERSION, "1.0");
		outputProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
		
		String xmlString = new String();
		try {
			mTransformer = mTransFactory.newTransformer();
			mTransformer.setOutputProperties(outputProperties);
			DOMSource domSource = new DOMSource(_notification.getDocumentElement());

			OutputStream output = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(output);
			mTransformer.transform(domSource, result);
			xmlString = output.toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return xmlString;

	}
	
	/**
	 * Writes the given string into a file with the given filename.
	 * @param _fileName the filename the created file should have
	 * @param _xml the content the file should have
	 * @param _context context needed for some methods
	 */
	private void writeToFile(String _fileName,String _xml,Context _context){
		try {
			java.io.File directory = new java.io.File(SyncHandler.ROOT_NOTIFICATION_FOLDER + "/" + SyncHandler.NOTIFICATION_FOLDER);
			directory.mkdirs();
			Log.i(TAG, "dir: " + directory.isDirectory());
			java.io.File outputFile = new java.io.File(SyncHandler.getFullPath(_fileName));
			
			
			outputFile.createNewFile();
			FileOutputStream output = new FileOutputStream(outputFile);
			
			output.write(_xml.getBytes());
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Creates a new element below the given root element with the given key, values and attributes
	 * @param _rootElement the root element 
	 * @param _key the key the new element should have
	 * @param _attribute a list of attributes for the new element
	 * @param _value the value of the element
	 * @throws Exception
	 */
	private void createElement(Element _rootElement, String _key, ArrayList<String> _attribute,ArrayList<String> _value) throws Exception{
		if(_attribute.size() != _value.size()){
			Log.e(TAG, "number of attributes does not match number of values!");
			throw(new Exception("Not equal number of key/values"));
		} else {
		Element element = mDocument.createElement(_key);
		for(int i=0; i<_attribute.size();i++){
			element.setAttribute(_attribute.get(i), String.valueOf(_value.get(i)));
		}
		Log.i(TAG, "created element: " + _key);
		_rootElement.appendChild(element);
		}
	}
	
	/**
	 * Creates a new element below the given root element with the given key, values and attribute
	 * @param _rootElement the root element 
	 * @param _key the key the new element should have
	 * @param _attribute an attribute for the element
	 * @param _value the value of the element
	 * @throws Exception
	 */
	private void createElement(Element _rootElement, String _key, String _attribute,String _value) throws Exception{
		Element element = mDocument.createElement(_key);
		element.setAttribute(_attribute, _value);
		Log.i(TAG, "created element: " + _key);
		_rootElement.appendChild(element);
	}
}
