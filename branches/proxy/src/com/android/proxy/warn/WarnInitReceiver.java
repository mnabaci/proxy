package com.android.proxy.warn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WarnInitReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
    	Log.d("WarnInitReceiver", "onReceive");
    	WarnManager.getInstance(context).initWarns();
    }

}
