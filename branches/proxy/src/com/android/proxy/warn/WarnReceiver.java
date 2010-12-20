package com.android.proxy.warn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WarnReceiver extends BroadcastReceiver {
	
	private static final String TAG = "WarnReceiver";
	private static final boolean DEBUG = true;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Warn warn = intent.getParcelableExtra(WarnManager.INTENT_EXTRA_NAME);
		LOGD("onReceive," + warn.getID());
	}
	
	private static void LOGD(String text) {
		if (DEBUG) {
			Log.d(TAG, text);
		}
	}

}
