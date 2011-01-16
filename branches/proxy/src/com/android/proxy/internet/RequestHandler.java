package com.android.proxy.internet;

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.proxy.cache.PushMessage;
import com.android.proxy.cache.MessageProvider;
import com.android.proxy.cache.Request;
import com.android.proxy.cache.RequestProvider;

public class RequestHandler {
	
	public static final int METHOD_GET = 1;
	public static final int METHOD_POST = 2;
	public static final int METHOD_DELETE = 3;
	public static final int METHOD_PUT = 4;
	public static final String METHOD_NAME_GET = "get";
	public static final String METHOD_NAME_POST = "post";
	public static final String METHOD_NAME_DELETE = "delete";
	public static final String METHOD_NAME_PUT = "put";
	
	private static final String NAMESPACE = "http://ext.service.eaglelink.cn/";
	 
	private static final String SOAP_ACTION = "CloudService";
	
	public static final String TAG_RESULT_CODE = "RESULTCODE";
	public static final String TAG_ERROR_DESCRIPTION = "ERRORDESC";
	public static final String TAG_SESSIONID = "SESSIONID";
	public static final String TAG_PUSHMSGS = "PUSHMSGS";
	public static final String TAG_MSG_OBJECT = "OBJECT";
	public static final String TAG_MSG_ID = "PUSHMSGID";
	public static final String TAG_MSG_SENDER = "SENDER";
	public static final String TAG_MSG_SENDTIME = "SENDTIME";
	public static final String TAG_MSG_OVERTIME = "OVERTIME";
	public static final String TAG_MSG_STARTTIME = "STARTTIME";
	public static final String TAG_MSG_SUBJECT = "SUBJECT";
	public static final String TAG_MSG_CONTENT = "CONTENT";
	public static final String TAG_MSG_TYPE = "TYPE";
	public static final String TAG_MSG_CUTETYPE = "CUETYPE";
	public static final String TAG_MSG_REMINDTYPE = "REMIDETYPE";
	
	public static final String SUCCESSFUL_RESULT_CODE = "0";
	     
	private String mUrl;
	private SoapObject mSoapRequest;
	private SoapSerializationEnvelope mEnvelope;
	HttpTransportSE mAndroidHttpTransport;
	
	private static DocumentBuilderFactory factory;
	private static DocumentBuilder builder;
	
	private static XMLHandler mHandler;
    private static SAXParser mSaxparser;
    private static ContentValues mContentValues;
	
	static {
		factory = DocumentBuilderFactory.newInstance();
        factory.setCoalescing(true);
        mContentValues = new ContentValues();
        mHandler = new XMLHandler();
        try {
			builder = factory.newDocumentBuilder();
			mSaxparser = SAXParserFactory.newInstance().newSAXParser();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public RequestHandler(String url) {
		mUrl = url;
		mEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
		mAndroidHttpTransport = new HttpTransportSE(mUrl);
	}
	
	public String handleRequest(String userId, String flatId, String sessionId, Request request) {
		if (request == null) return null;
		mSoapRequest = new SoapObject(NAMESPACE, getMethodName(request.action));
		mSoapRequest.addProperty("arg0", userId);
		mSoapRequest.addProperty("arg1", flatId);
		mSoapRequest.addProperty("arg2", sessionId);
		mSoapRequest.addProperty("arg3", request.versionId);
		mSoapRequest.addProperty("arg4", request.items);
		mSoapRequest.addProperty("arg5", request.body);
		mEnvelope.dotNet = false;
		mEnvelope.setOutputSoapObject(mSoapRequest);
		try {
			mAndroidHttpTransport.call(SOAP_ACTION, mEnvelope);
			Object result = mEnvelope.getResponse();
			return result.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getMethodName(int method) {
		switch(method) {
		case METHOD_GET:
			return METHOD_NAME_GET;
		case METHOD_POST:
			return METHOD_NAME_POST;
		case METHOD_DELETE:
			return METHOD_NAME_DELETE;
		case METHOD_PUT:
			return METHOD_NAME_PUT;
		}
		return null;
	}
	
    public synchronized static XMLResponse parseXMLResult2(String result) {
    	if (TextUtils.isEmpty(result)) {
    		return null;
    	}
    	XMLResponse xmlResponse = new XMLResponse();
    	try {
			Document doc = builder.parse( new ByteArrayInputStream(result.getBytes()));
			NodeList resultNodes = doc.getElementsByTagName(TAG_RESULT_CODE);
			if (resultNodes.getLength() > 0 && resultNodes.item(0).getChildNodes().getLength() > 0) {
				String resultCode =  resultNodes.item(0).getChildNodes().item(0).getNodeValue();
				xmlResponse.resultCode = resultCode;
			}		
			NodeList sessionNodes = doc.getElementsByTagName(TAG_SESSIONID);
			if (sessionNodes.getLength() > 0 && sessionNodes.item(0).getChildNodes().getLength() > 0) {
				xmlResponse.sessionId = sessionNodes.item(0).getChildNodes().item(0).getNodeValue();
			}
			NodeList errorNodes = doc.getElementsByTagName(TAG_ERROR_DESCRIPTION);
			if (errorNodes.getLength() > 0 && errorNodes.item(0).getChildNodes().getLength() > 0) {
				xmlResponse.errorDescription = errorNodes.item(0).getChildNodes().item(0).getNodeValue();
			}
			return xmlResponse;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    public synchronized static XMLResponse parseXMLResult(Context context, String result) {
    	if (TextUtils.isEmpty(result)) {
    		return null;
    	}
    	mHandler.setContext(context);
    	if (mSaxparser != null) {
    		try {
				mSaxparser.parse(new ByteArrayInputStream(result.getBytes()), mHandler);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
    	}
    	return mHandler.getXMLResponse();
    }
    
    public static int insertMessageToCache(Context context, PushMessage message) {
    	if (context == null || message == null) return 0;
    	mContentValues.clear();
    	mContentValues.put(MessageProvider.MSG_ID, message.pushMessageId);
    	mContentValues.put(MessageProvider.SENDER, message.sender);
    	mContentValues.put(MessageProvider.SENDTIME, message.sendTime);
    	mContentValues.put(MessageProvider.OVERTIME, message.overTime);
    	mContentValues.put(MessageProvider.STARTTIME, message.startTime);
    	mContentValues.put(MessageProvider.SUBJECT, message.subject);
    	mContentValues.put(MessageProvider.CONTENT, message.content);
    	mContentValues.put(MessageProvider.TYPE, message.type);
    	mContentValues.put(MessageProvider.CUTETYPE, message.cuteType);
    	mContentValues.put(MessageProvider.REMINDTYPE, message.remindType);
    	Uri uri = context.getContentResolver().insert(MessageProvider.CONTENT_URI, mContentValues);
    	return (int) ContentUris.parseId(uri);
    }
    
    public static class XMLHandler extends DefaultHandler {
    	
    	private XMLResponse mXmlResponse;
    	private PushMessage mMessage;
    	private boolean mIsPushMsg;
    	private StringBuilder mCurrentCharacters;
    	private Context mContext;
    	
    	public XMLHandler() {
    		mCurrentCharacters = new StringBuilder();
    	}
    	
    	public XMLResponse getXMLResponse() {
    		return mXmlResponse;
    	}
    	
    	public void setContext(Context context) {
    		mContext = context;
    	}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			// TODO Auto-generated method stub
			super.characters(ch, start, length);
			mCurrentCharacters.setLength(0);
			mCurrentCharacters.append(ch, start, length);
		}

		@Override
		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub
			super.endDocument();
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			// TODO Auto-generated method stub
			super.endElement(uri, localName, qName);
			if (localName.equals(TAG_RESULT_CODE)) {
				mXmlResponse.resultCode = mCurrentCharacters.toString();
			} else if (localName.equals(TAG_ERROR_DESCRIPTION)) {
				mXmlResponse.errorDescription = mCurrentCharacters.toString();
			} else if (localName.equals(TAG_SESSIONID)) {
				mXmlResponse.sessionId = mCurrentCharacters.toString();
			} else if (localName.equals(TAG_PUSHMSGS)) {
				mIsPushMsg = false;
			} else if (mIsPushMsg) {
				if (localName.equals(TAG_MSG_OBJECT)) {
					insertMessageToCache(mContext, mMessage);
				} else if (localName.equals(TAG_MSG_ID)) {
					mMessage.pushMessageId = mCurrentCharacters.toString();
				} else if (localName.equals(TAG_MSG_SENDER)) {
					mMessage.sender = mCurrentCharacters.toString();
				} else if (localName.equals(TAG_MSG_SENDTIME)) {
					mMessage.sendTime = mCurrentCharacters.toString();
				} else if (localName.equals(TAG_MSG_OVERTIME)) {
					mMessage.overTime = mCurrentCharacters.toString();		
				} else if (localName.equals(TAG_MSG_STARTTIME)) {
					mMessage.startTime = mCurrentCharacters.toString();
				} else if (localName.equals(TAG_MSG_SUBJECT)) {
					mMessage.subject = mCurrentCharacters.toString();
				} else if (localName.equals(TAG_MSG_CONTENT)) {
					mMessage.content = mCurrentCharacters.toString();
				} else if (localName.equals(TAG_MSG_TYPE)) {
					mMessage.type = Integer.parseInt(mCurrentCharacters.toString());
				} else if (localName.equals(TAG_MSG_CUTETYPE)) {
					mMessage.cuteType = Integer.parseInt(mCurrentCharacters.toString());
				} else if (localName.equals(TAG_MSG_REMINDTYPE)) {
					mMessage.remindType = mCurrentCharacters.toString();
				}
			}
		}

		@Override
		public void startDocument() throws SAXException {
			// TODO Auto-generated method stub
			super.startDocument();
			Log.d("XMLHandler", "startDocument");
			mXmlResponse = new XMLResponse();
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// TODO Auto-generated method stub
			super.startElement(uri, localName, qName, attributes);
			if (localName.equals(TAG_PUSHMSGS)) {
				mIsPushMsg = true;
			} else if (mIsPushMsg && localName.equals(TAG_MSG_OBJECT)) {
				mMessage = new PushMessage();
			}
		}
    	
    }

}
