package com.android.proxy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

public class ProxyService extends Service {

    private static final String TAG = "ProxyService";
    private final static boolean DEBUG = true;
    
    private final IProxyService.Stub mBinder = new IProxyService.Stub() {
        
        public int getPid() throws RemoteException {
            // TODO Auto-generated method stub
            LOGD("getPid");
            return Process.myPid();
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        LOGD("onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        LOGD("onCreate");
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        LOGD("onDestroy");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        LOGD("onStart");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        LOGD("onUnbind");
        return super.onUnbind(intent);
    }
    
    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}
