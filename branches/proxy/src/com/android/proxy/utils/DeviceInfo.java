package com.android.proxy.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class DeviceInfo {
	
	private static final String TAG = "DeviceInfo";
    private final static boolean DEBUG = true;
    
    private Context mContext;
    
    private static DeviceInfo sDeviceInfo;
    
    public static DeviceInfo getInstance(Context context) {
    	if (sDeviceInfo == null) {
    		sDeviceInfo = new DeviceInfo(context);
    	}
    	return sDeviceInfo;
    }
    
    public static void releaseInstance() {
    	sDeviceInfo.release();
    	sDeviceInfo = null;
    }
    
    private DeviceInfo(Context context) {
    	mContext = context;
    }
    
    private void release() {
    	mContext = null;
    }
    
    public boolean isNetworkAvailable() {
    	ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);  
    	NetworkInfo network = cm.getActiveNetworkInfo();   
    	if(network != null){  
    		return network.isAvailable();  
    	}  
    	return false;  
    }
    
    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}
