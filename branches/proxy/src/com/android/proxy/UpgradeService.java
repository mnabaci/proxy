package com.android.proxy;

import java.io.File;

import org.apache.http.HttpResponse;

import com.android.proxy.cache.Request;
import com.android.proxy.internet.HttpClients;
import com.android.proxy.internet.RequestHandler;
import com.android.proxy.internet.XMLResponse;
import com.android.proxy.utils.DeviceInfo;
import com.android.proxy.utils.Environment;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpgradeService extends Service {
	
	public static final String ACTION = "com.android.proxy.upgrade";
	
	private RequestHandler mHandler;
	private Request mRequest;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mHandler = new RequestHandler(
				Config.getInstance(getApplicationContext()).getCloudUrl());
		mRequest = new Request();
		mRequest.action = Request.ACTION_GET;
		mRequest.items = "VERSION";
		mRequest.versionId = String.valueOf(Config.getInstance(getApplicationContext()).getVersionCode());
		mRequest.body = "<?xml version='1.0' encoding='utf-8'?><OBJECTS><OBJECT>"
			+ "<DEVICEPIM>" + "0000000000003" 
			+ "</DEVICEPIM>" + "<SESSIONID>" + Config.getInstance(getApplicationContext()).getEncryptedSessionId() 
			+ "</SESSIONID>" + "<PROGNAME>" + "AndroidProxy" + "</PROGNAME>" 
			+ "<PROGID>AndroidProxy</PROGID>" + "<PROGOS>Android-2.2</PROGOS>"
			+ "<DEVICETYPE>EagleLinkFlat</DEVICETYPE>"
			+ "<PROGVERSION>" + Config.getInstance(getApplicationContext()).getVersionCode() +"</PROGVERSION>"
			+ "</OBJECT></OBJECTS>";
	}
	
	private void upgrade() {
		File file = new File(Environment.getInstance(getApplicationContext()).UPGRADE_APK_FILE_PATH);
		if (file.exists()) {
			file.delete();
		}
		if (!DeviceInfo.getInstance(getApplicationContext()).isNetworkAvailable()) {
			return;
		}
		Config config = Config.getInstance(getApplicationContext());
		String result = mHandler.handleRequest(config.getUserId(), config.getFlatId(), 
				config.getEncryptedSessionId(), mRequest);
		XMLResponse xmlResponse = RequestHandler.parseXMLResult(getApplicationContext(), result);
		if (xmlResponse.resultCode.equals(RequestHandler.SUCCESSFUL_RESULT_CODE)) {
			config.setSessionId(xmlResponse.sessionId);
		}
		Log.d("UpgradeService", "upgrade, result=" + result);
		int versionCode = config.getVersionCode();
		Double newestVersion = Double.parseDouble(xmlResponse.version);
		if (versionCode < newestVersion) {
			HttpClients httpClients = new HttpClients(getApplicationContext());
			httpClients.setURL(xmlResponse.url);
	        HttpResponse httpResponse = httpClients.openConnection(0);
	        int downloadResult = httpClients.downloadFile(httpResponse, Environment.UPGRADE_APK_FILE_PATH, false);
	        httpClients.disConnect();
	        if (downloadResult == HttpClients.SAVE_FILE_SUCCESS) {
	        	notifyUpgrade();
	        }
		}
	}
	
	private void notifyUpgrade() {
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		upgrade();
		return super.onStartCommand(intent, flags, startId);
	}

}
