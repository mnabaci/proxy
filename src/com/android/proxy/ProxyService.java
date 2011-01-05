package com.android.proxy;

import java.util.Vector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.android.proxy.cache.Request;
import com.android.proxy.cache.RequestProvider;
import com.android.proxy.cache.Response;
import com.android.proxy.cache.ResponseProvider;
import com.android.proxy.utils.DeviceInfo;
import com.android.proxy.utils.Environment;
import com.android.proxy.warn.WarnProvider;

public class ProxyService extends Service {

    private static final String TAG = "ProxyService";
    private final static boolean DEBUG = true;
    
    private static final int LARGE_SPACE_NOTIFICATION_ID = -1;
    
    private Environment mEnvironment;
    private Config mConfig;
    private DeviceInfo mDeviceInfo;
    private DataSpaceObserver mSpaceObserver;
    private PackageManager mPm;
    private Response mResponse;
    private ContentValues mContentValues;
    private Vector<Request> mRequestQueue;
    
    private final IProxyService.Stub mBinder = new IProxyService.Stub() {
        
        public int getPid() throws RemoteException {
            // TODO Auto-generated method stub
            LOGD("getPid");
            return Process.myPid();
        }

		public Response postRequest(Request request) throws RemoteException {
			// TODO Auto-generated method stub
			LOGD("postRequest," + request.action);
			return null;
		}

		public Response getResponse(int id, String packageName) throws RemoteException {
			// TODO Auto-generated method stub
			LOGD("getResponse," + id);
			Response response = new Response();
			response.requestId = -1;
			return response;
		}
    };

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        LOGD("onBind,");
        return mBinder;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        LOGD("onCreate");
        mEnvironment = Environment.getInstance(getApplicationContext());
        mConfig = Config.getInstance();
        mDeviceInfo = DeviceInfo.getInstance(getApplicationContext());
        mSpaceObserver = new DataSpaceObserver(new Handler());
        getContentResolver().registerContentObserver(WarnProvider.CONTENT_URI, true, mSpaceObserver);
        getContentResolver().registerContentObserver(RequestProvider.CONTENT_URI, true, mSpaceObserver);
        getContentResolver().registerContentObserver(ResponseProvider.CONTENT_URI, true, mSpaceObserver);
        mPm = getPackageManager();
        mResponse = new Response();
        mContentValues = new ContentValues();
        mRequestQueue = new Vector<Request>();
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
    
    /**
     * 
     * @param request
     * @return request id in request cache
     */
    private int insertRequestToCache(Request request) {
    	mContentValues.clear();
    	mContentValues.put(RequestProvider.ACTION, request.action);
    	mContentValues.put(RequestProvider.OWNER, request.packageName);
    	mContentValues.put(RequestProvider.ITEMS, request.items);
    	mContentValues.put(RequestProvider.VERSION_ID, request.versionId);
    	mContentValues.put(RequestProvider.BODY, request.body);
    	mContentValues.put(RequestProvider.TIME, System.currentTimeMillis());
    	Uri uri = getContentResolver().insert(RequestProvider.CONTENT_URI, mContentValues);
    	return (int) ContentUris.parseId(uri);
    }
    
    private void deleteRequestFromCache(int id) {
    	Uri deleteUri = ContentUris.withAppendedId(RequestProvider.CONTENT_URI, id);
    	getContentResolver().delete(deleteUri, null, null);
    }
    
    private int insertResponseToCache(Response response) {
    	mContentValues.clear();
    	mContentValues.put(ResponseProvider.OWNER, response.packageName);
    	mContentValues.put(ResponseProvider.BODY, response.body);
    	mContentValues.put(ResponseProvider.REQUEST_ID, response.requestId);
    	mContentValues.put(ResponseProvider.TIME, response.time);
    	Uri uri = getContentResolver().insert(ResponseProvider.CONTENT_URI, mContentValues);
    	return (int) ContentUris.parseId(uri);
    }
    
    private void deleteResponseFromCache(int id) {
    	Uri deleteUri = ContentUris.withAppendedId(ResponseProvider.CONTENT_URI, id);
    	getContentResolver().delete(deleteUri, null, null);
    }
    
    private void notifyLargeSpace() {
        LOGD("notifyLargeSpace");
        PendingIntent pendingNotify = PendingIntent.getActivity(getApplicationContext(), 0, null, 0);
        Notification n = new Notification(R.drawable.icon, getString(R.string.large_space_tip), System.currentTimeMillis());
        n.setLatestEventInfo(getApplicationContext(), getString(R.string.large_space_tip_title), 
                getString(R.string.large_space_tip), pendingNotify);
        n.flags |= Notification.FLAG_SHOW_LIGHTS 
                | Notification.FLAG_ONLY_ALERT_ONCE
                | Notification.FLAG_AUTO_CANCEL;
        n.defaults |= Notification.DEFAULT_LIGHTS;
        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(LARGE_SPACE_NOTIFICATION_ID);
        nm.notify(LARGE_SPACE_NOTIFICATION_ID, n);
    }
    
//    class PkgSizeObserver extends IPackageStatsObserver.Stub {
//        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {
//             LOGD("space:" + pStats.dataSize + ",config max:" + mConfig.getMaxSpace());
//             if (pStats.dataSize >= mConfig.getMaxSpace()) {
//                 notifyLargeSpace();
//             }
//         }
//     }
    
    public class DataSpaceObserver extends ContentObserver {

		public DataSpaceObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}
    	
		public void onChange(boolean selfChange) {
			LOGD("onChange");
//			mPm.getPackageSizeInfo(getPackageName(), mSizeObserver);
		}
    }
    
    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}
