package com.android.proxy;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.android.proxy.cache.Request;
import com.android.proxy.internet.RequestHandler;
import com.android.proxy.internet.XMLResponse;
import com.android.proxy.utils.DeviceInfo;

public class HeartBeatService extends Service {
	
	public static final String ACTION = "com.android.proxy.heartbeat";

	private RequestHandler mHandler;
	private Request mRequest;
	private HeatBeatTask mTask;
	private Timer mTimer;
	private boolean mIsHeartBeating;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mTask = new HeatBeatTask();
		mTimer = new Timer();
		mIsHeartBeating = false;
		mHandler = new RequestHandler(
				Config.getInstance(getApplicationContext()).getCloudUrl());
		mRequest = new Request();
		mRequest.action = Request.ACTION_GET;
		mRequest.items = "IAMHERE";
		mRequest.versionId = "-1";
		mRequest.body = "<?xml version='1.0' encoding='utf-8'?><OBJECTS><OBJECT>"
			+ "<DEVICEPIM>" + Config.getInstance(getApplicationContext()).getFlatId() 
			+ "</DEVICEPIM>" + "</OBJECT></OBJECTS>";
	}
	
	private void heartBeat() {
		if (!DeviceInfo.getInstance(getApplicationContext()).isNetworkAvailable()) {
			return;
		}
		Config config = Config.getInstance(getApplicationContext());
		String sessionid = config.isRegistered() ? config.getEncryptedSessionId() : config.ANONOYMOUS_SESSIONID;
		String result = mHandler.handleRequest(config.getUserId(), config.getFlatId(), 
				sessionid, mRequest);
		XMLResponse xmlResponse = RequestHandler.parseXMLResult(getApplicationContext(), result);
		if (xmlResponse != null && xmlResponse.resultCode.equals(RequestHandler.SUCCESSFUL_RESULT_CODE)) {
			config.setSessionId(xmlResponse.sessionId);
		}
		Log.d("HeartBeatService", "heart beat, result=" + result);
	}
	
	private void startHeatBeat() {
		if (mIsHeartBeating) return;
		mTimer.schedule(mTask, 0, Config.getInstance(getApplicationContext()).getHeartBeatInterval());
		mIsHeartBeating = true;
	}
	
	private void stopHeartBeat() {
		mTimer.cancel();
		mIsHeartBeating = false;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d("HeartBeatService", "onDestroy");
		stopHeartBeat();
		mTimer.purge();
		mHandler = null;
		mTimer = null;
		mTask = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d("HeartBeatService", "onStartCommand");
		startHeatBeat();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private class HeatBeatTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			heartBeat();
		}
		
	}
}
