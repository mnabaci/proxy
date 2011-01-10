package com.android.proxy.internet;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

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
	     
	private String mUrl;
	private SoapObject mSoapRequest;
	private SoapSerializationEnvelope mEnvelope;
	HttpTransportSE mAndroidHttpTransport;
	
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

}
