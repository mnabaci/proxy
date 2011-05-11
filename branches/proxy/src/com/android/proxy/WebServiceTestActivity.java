package com.android.proxy;

import org.apache.http.HttpResponse;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.android.proxy.internet.HttpClients;
import com.android.proxy.internet.RequestHandler;
import com.android.proxy.internet.XMLResponse;
import com.android.proxy.utils.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class WebServiceTestActivity extends Activity {
	 /** Called when the activity is first created. */

	private static final String METHOD_NAME = "post";
	private static final String NAMESPACE = "http://ext.service.eaglelink.cn/";
	 
	private static final String SOAP_ACTION = "CloudService";
	     
	private static final String URL = "http://222.128.78.180:8080/service/services/CloudService";
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.v("gtrgtr", "envelope.getResponse()=============");
		try {
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			
			request.addProperty("arg0", "jun.liu@eaglelink.cn");
			request.addProperty("arg1", "EAGLELINK001");
			request.addProperty("arg2", "eaglelink888");
			request.addProperty("arg3", "-1");
			request.addProperty("arg4", "USERS");
//			request.addProperty("arg5", "<?xml version='1.0' encoding='utf-8'?><OBJECTS><OBJECT>"
//					+ "<DEVICEPIM>" + "0000000000003" 
//					+ "</DEVICEPIM>" + "<SESSIONID>" + Config.getInstance(getApplicationContext()).getEncryptedSessionId() 
//					+ "</SESSIONID>" + "<PROGNAME>" + "AndroidProxy" + "</PROGNAME>" 
//					+ "<PROGID>AndroidProxy</PROGID>" + "<PROGOS>Android-2.2</PROGOS>"
//					+ "<DEVICETYPE>EagleLinkFlat</DEVICETYPE>"
//					+ "<PROGVERSION>1.0</PROGVERSION>"
//					+ "</OBJECT></OBJECTS>");
//			String registerXML = "<?xml version='1.0' encoding='utf-8'?><OBJECTS><OBJECT>"
//				+ "<USERID>fangg05@gmail.com</USERID><FLATID>" + Config.getInstance(getApplicationContext()).getFlatId() + "</FLATID>"
//				+ "</OBJECT></OBJECTS>";
			String registerXML = "";
			request.addProperty("arg5", registerXML);
			
			Log.d("WebServiceActivity", "request:" + request.toString());
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER10);
			// envelope.setOutputSoapObject(request);
			// String uur = "?r=" + new Date().getTime();
			// uur = Base64.encode(URL.getBytes()) + uur;
			// uur = URL + uur;
			// Log.v("gtrgtr", "uur========" + uur);
			envelope.dotNet = false;
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			// envelope.bodyOut = request;
			androidHttpTransport.call(SOAP_ACTION, envelope);
			Object result = envelope.getResponse();
//			String [] str = (String [])result;
			Log.d("result",result.toString());
			XMLResponse response = RequestHandler.parseXMLResult(getApplicationContext(), result.toString());
			if (!TextUtils.isEmpty(response.sessionId)) {
				Config.getInstance(getApplicationContext()).setSessionId(response.sessionId);
			}
			response.print();
			HttpClients mHc = new HttpClients(getApplicationContext());
			mHc.setURL("http://222.128.78.180:8080/service/download/AndroidPorxy1.1.apk");
	        HttpResponse mResponse = mHc.openConnection(0);
	        mHc.downloadFile(mResponse, "/data/data/com.android.proxy/files/proxy.apk", false);
	        int responseCode = mHc.getResponseCode();
	        mHc.disConnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
