package com.android.proxy;

import com.android.proxy.utils.DeviceInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d("NetReceiver", "onReceive...........................");
		if (DeviceInfo.getInstance(context).isNetworkAvailable()) {
			Intent i = new Intent();
			i.setClass(context, ProxyService.class);
			i.putExtra(ProxyService.PROXY_INTENT_TYPE, ProxyService.PROXY_INTENT_TYPE_POSTREQUESTS);
			context.startService(i);
		}
	}

}
