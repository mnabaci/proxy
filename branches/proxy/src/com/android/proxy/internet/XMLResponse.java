package com.android.proxy.internet;

import java.util.Vector;

import android.util.Log;

import com.android.proxy.cache.PushMessage;

public class XMLResponse {

	public String resultCode;
	public String errorDescription;
	public String sessionId;
//	public Vector<PushMessage> pushMessages;
	
	public void print() {
		Log.d("XMLResponse", "resultCode = " + resultCode + ";errorDesc=" + errorDescription
				+ ";sessionId=" + sessionId);
	}
}
