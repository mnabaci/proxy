package com.android.proxy.warn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.util.Log;

public class WarnReceiver extends BroadcastReceiver {
	
	private static final String TAG = "WarnReceiver";
	private static final boolean DEBUG = true;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
	    final byte[] data = intent.getByteArrayExtra(WarnManager.INTENT_EXTRA_WARN);
	    if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            Warn warn = Warn.CREATOR.createFromParcel(in);
            LOGD("onReceive," + warn.getID());
        }
	}
	
	private static void LOGD(String text) {
		if (DEBUG) {
			Log.d(TAG, text);
		}
	}

}
