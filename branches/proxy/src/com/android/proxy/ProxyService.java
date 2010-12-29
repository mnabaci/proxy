package com.android.proxy;

import com.android.proxy.cache.RequestProvider;
import com.android.proxy.cache.ResponseProvider;
import com.android.proxy.utils.Environment;
import com.android.proxy.warn.WarnProvider;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.IPackageStatsObserver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

public class ProxyService extends Service {

    private static final String TAG = "ProxyService";
    private final static boolean DEBUG = true;
    
    private Environment mEnvironment;
    private Config mConfig;
    private DataSpaceObserver mSpaceObserver;
    private PackageManager mPm;
    private PkgSizeObserver mSizeObserver;
    
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
        mEnvironment = Environment.getInstance(getApplicationContext());
        mConfig = Config.getInstance();
        mSpaceObserver = new DataSpaceObserver(new Handler());
        getContentResolver().registerContentObserver(WarnProvider.CONTENT_URI, true, mSpaceObserver);
        getContentResolver().registerContentObserver(RequestProvider.CONTENT_URI, true, mSpaceObserver);
        getContentResolver().registerContentObserver(ResponseProvider.CONTENT_URI, true, mSpaceObserver);
        mPm = getPackageManager();
        mSizeObserver = new PkgSizeObserver();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mSpaceObserver);
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
    
    class PkgSizeObserver extends IPackageStatsObserver.Stub {
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {
             LOGD("space:" + pStats.dataSize);
         }
     }
    
    public class DataSpaceObserver extends ContentObserver {

		public DataSpaceObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}
    	
		public void onChange(boolean selfChange) {
			LOGD("onChange");
			mPm.getPackageSizeInfo(getPackageName(), mSizeObserver);
		}
    }
    
    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}
