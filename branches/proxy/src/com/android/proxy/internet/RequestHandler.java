package com.android.proxy.internet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParserException;

import android.text.TextUtils;

import com.android.proxy.cache.Request;
import com.android.proxy.cache.Response;

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
	public static final String SUCCESSFUL_RESULT_CODE = "0";
	     
	private String mUrl;
	private SoapObject mSoapRequest;
	private SoapSerializationEnvelope mEnvelope;
	HttpTransportSE mAndroidHttpTransport;
	
	private static DocumentBuilderFactory factory;
	private static DocumentBuilder builder;
	
	static {
		factory = DocumentBuilderFactory.newInstance();
        factory.setCoalescing(true);
        try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
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
	
	private static StringBuilder stringBuilder = new StringBuilder();
    /**
     * if succeed, return sessionid, else return error description
     * @param result
     * @return
     */
    public static XMLResponse parseXMLResult(String result) {
    	if (TextUtils.isEmpty(result)) {
    		return null;
    	}
    	XMLResponse xmlResponse = new XMLResponse();
    	int index = result.lastIndexOf("ITEMS");
    	if (index > 0 && result.codePointAt(index-1) != '/') {
    		stringBuilder.setLength(0);
        	stringBuilder.append(result, 0, index);
        	stringBuilder.append('/');
        	stringBuilder.append(result, index, result.length());
    	} else {
    		stringBuilder.setLength(0);
        	stringBuilder.append(result);
    	}
    	try {
			Document doc = builder.parse( new ByteArrayInputStream(stringBuilder.toString().getBytes()));
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

}
