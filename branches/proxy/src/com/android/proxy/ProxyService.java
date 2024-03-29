package com.android.proxy;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedList;
import java.util.Vector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.proxy.cache.Request;
import com.android.proxy.cache.RequestProvider;
import com.android.proxy.cache.Response;
import com.android.proxy.cache.ResponseProvider;
import com.android.proxy.internet.RequestHandler;
import com.android.proxy.internet.XMLResponse;
import com.android.proxy.ui.LoginActivity;
import com.android.proxy.ui.NewLoginActivity;
import com.android.proxy.ui.NewRegisterActivity;
import com.android.proxy.utils.DeviceInfo;
import com.android.proxy.utils.Environment;
import com.android.proxy.utils.Utils;
import com.android.proxy.warn.WarnKlaxon;
import com.android.proxy.warn.WarnProvider;

public class ProxyService extends Service {

    private static final String TAG = "ProxyService";
    private final static boolean DEBUG = true;
    
    public static final String INTENT_PROPERTY_RANDOM = "RANDOM";
    public static final String INTENT_PROPERTY_AUTHORITY = "AUTHORITY";
    public static final String PROXY_INTENT_TYPE = "type";
    
    public static final int PROXY_INTENT_TYPE_POSTREQUESTS = 1;
    
    private static final int LARGE_SPACE_NOTIFICATION_ID = -1;
    
    private static final String[] RESPONSE_PROJECTION = {ResponseProvider._ID, 
    	ResponseProvider.OWNER, ResponseProvider.BODY, ResponseProvider.REQUEST_ID, 
    	ResponseProvider.TIME};
    private static final int RESPONSE_ID_COL = 0;
    private static final int RESPONSE_OWNER_COL = 1;
    private static final int RESPONSE_BODY_COL = 2;
    private static final int RESPONSE_RID_COL = 3;
    private static final int RESPONSE_TIME_COL = 4;
    
    private static final String TIMEOUT_RESPONSE = "<?xml version='1.0' encoding='UTF-8'?>"+
    			"<RESPONSE><RESULT><RESULTCODE>-1</RESULTCODE>"+
    			"<ERRORDESC>sessionId illegal, re login please.</ERRORDESC></RESULT>"+
    			"<SESSIONID></SESSIONID></RESPONSE>";
    
    private static final String[] REQUEST_PROJECTION = {RequestProvider._ID, RequestProvider.ACTION,
    	RequestProvider.ITEMS, RequestProvider.BODY, RequestProvider.OWNER, RequestProvider.VERSION_ID,
    	RequestProvider.RESPONSE};
    private static final int REQUEST_ID_COL = 0;
    private static final int REQUEST_ACTION_COL = 1;
    private static final int REQUEST_ITEMS_COL = 2;
    private static final int REQUEST_BODY_COL = 3;
    private static final int REQUEST_OWNER_COL = 4;
    private static final int REQUEST_VERSION_COL = 5;
    private static final int REQUEST_RESPONSE_COL = 6;
    
    private Environment mEnvironment;
    private Config mConfig;
    private DeviceInfo mDeviceInfo;
    private DataSpaceObserver mSpaceObserver;
    private PackageManager mPm;
    private Response mResponse;
    private Request mRequest;
    private ContentValues mContentValues;
    private RequestHandler mRequestHandler;
    
    private final IProxyService.Stub mBinder = new IProxyService.Stub() {
        
        public int getPid() throws RemoteException {
            // TODO Auto-generated method stub
            LOGD("getPid");
            return Process.myPid();
        }

		public Response postRequest(Request request, boolean sync) throws RemoteException {
			// TODO Auto-generated method stub
			LOGD("postRequest," + request.action);
			Response response = new Response();
			if (checkRequest(request)) {
				handleRequest(request, false, response, sync);
				if (response.requestId >= 0) {
					response.errorId = Response.ERROR_WEBSERVICE_ERROR;
				}
			} else {
				response.requestId = Response.ERROR_RESPONSE;
				response.errorId = Response.ERROR_PARAMETER_PROBLEM;
			}
			LOGD("after post request");
			return response;
		}

		public Response getResponse(int id, String packageName) throws RemoteException {
			// TODO Auto-generated method stub
			LOGD("getResponse," + id);
			return getResponseFromCache(id, packageName);
		}
		
    };
    
    private boolean checkRequest(Request request) {
    	return true;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        LOGD("onBind,");
        if (verifyBind(arg0)) {
        	return mBinder;
        } else {
        	return null;
        }
    }
    
    private boolean verifyBind(Intent intent) {
    	String random = intent.getStringExtra(INTENT_PROPERTY_RANDOM);
    	String authority = intent.getStringExtra(INTENT_PROPERTY_AUTHORITY);
    	if(TextUtils.isEmpty(random) || TextUtils.isEmpty(authority)) {
    		return false;
    	}
    	String decrypt = Utils.Decrypt(authority, Utils.TRUST_KEY);
    	int index = decrypt.lastIndexOf(random);
    	if (index > 0) {
    		String packageName = decrypt.substring(0, index);
    		return mConfig.isTrustedPackage(packageName);
    	}
    	return false;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        LOGD("onCreate");
        mEnvironment = Environment.getInstance(getApplicationContext());
        mConfig = Config.getInstance(getApplicationContext());
        mDeviceInfo = DeviceInfo.getInstance(getApplicationContext());
        mSpaceObserver = new DataSpaceObserver(new Handler());
        getContentResolver().registerContentObserver(WarnProvider.CONTENT_URI, true, mSpaceObserver);
        getContentResolver().registerContentObserver(RequestProvider.CONTENT_URI, true, mSpaceObserver);
        getContentResolver().registerContentObserver(ResponseProvider.CONTENT_URI, true, mSpaceObserver);
        mPm = getPackageManager();
        mResponse = new Response();
        mRequest = new Request();
        mContentValues = new ContentValues();
        mRequestHandler = new RequestHandler(mConfig.getCloudUrl());
        launchHeartBeatService();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			public void uncaughtException(Thread thread, Throwable ex) {
				// TODO Auto-generated method stub
				LOGD("uncaughtException");
				ex.printStackTrace();
				System.exit(0);
			}
		});
    }
    
    private synchronized void handleRequestsInCache() {
    	Cursor cursor = getContentResolver().query(RequestProvider.CONTENT_URI, REQUEST_PROJECTION,
    			RequestProvider.RESPONSE + " = " + RequestProvider.NO_RESPONSE, null, RequestProvider.TYPE + " asc");
    	if (cursor.getCount() > 0) {
    		cursor.moveToFirst();
    		do {
    			obtainRequestFromCursor(cursor, mRequest);
    			handleRequest(mRequest, true, mResponse, false);
    		} while (cursor.moveToNext());
    	}
    	cursor.close();
    }
    
    private void obtainRequestFromCursor(Cursor cursor, Request request) {
    	if (request == null)  return;
    	request.cacheId = cursor.getInt(REQUEST_ID_COL);
    	request.packageName = cursor.getString(REQUEST_OWNER_COL);
    	request.action = cursor.getInt(REQUEST_ACTION_COL);
    	request.items = cursor.getString(REQUEST_ITEMS_COL);
    	request.versionId = cursor.getString(REQUEST_VERSION_COL);
    	request.body = cursor.getString(REQUEST_BODY_COL);
    }
    
    private synchronized void handleRequestDirectly(Request request, boolean fromCache, Response response, boolean sync) {
    	if (!mDeviceInfo.isNetworkAvailable()) {
			if (!fromCache) {
				int rId = insertRequestToCache(request, response);
    			request.cacheId = rId;
			}
    		response.reset();
    		response.requestId = request.cacheId;
    	} else {
    		String result = mRequestHandler.handleRequest(mConfig.getUserId(), mConfig.getFlatId(), mConfig.getEncryptedSessionId(), 
    				request);
    		if (TextUtils.isEmpty(result)) {
    			if (!fromCache) {
    				int rId = insertRequestToCache(request, response);
    				request.cacheId = rId;
    			} else {
    				LOGD("return null, request from cache, to be deleted," + request.cacheId);
    				deleteRequestFromCache(request.cacheId);
    			}
        		response.reset();
        		response.requestId = request.cacheId;
    		} else {
    			LOGD("handle request, response=" + result);
    			XMLResponse xmlInfo = RequestHandler.parseXMLResult(getApplicationContext(), result);
    			if (xmlInfo == null) {
    				response.requestId = Response.ERROR_RESPONSE;
    				response.errorId = Response.ERROR_XML_ERROR;
    				return;
    			}
    			LOGD("sessionId=" + xmlInfo.sessionId);
    			if (xmlInfo.sessionId == null || xmlInfo.sessionId.equals("null") 
    					|| TextUtils.isEmpty(xmlInfo.sessionId)) {	
    				if (!fromCache) {
    					LOGD("+++++++++++++++++++++++++++++++++");
	    				int rId = insertRequestToCache(request, response);
	    				request.cacheId = rId;
    				}
    				response.reset();
    				response.requestId = request.cacheId;
    			} else {
    				response.body = result;
        			response.requestId = Response.REAL_RESPONSE;
        			response.packageName = request.packageName;
        			response.time = System.currentTimeMillis();
    				mConfig.setSessionId(xmlInfo.sessionId);
	    			if (fromCache) {
	    				response.requestId = request.cacheId;
	    				int responseId = insertResponseToCache(response);
	    				setRequestHandledInCache(request.cacheId, responseId);
	    			}
    			}
    		}
    	}
		return;
    }
    
    private synchronized void handleRequest(Request request, boolean fromCache, Response response, boolean sync) {
    	if (!mDeviceInfo.isNetworkAvailable()) {
    		LOGD("netword unavailable");
			if (!fromCache) {
				int rId = insertRequestToCache(request, response);
    			request.cacheId = rId;
			}
    		response.reset();
    		response.requestId = request.cacheId;
    	} else {
    		String result = mRequestHandler.handleRequest(mConfig.getUserId(), mConfig.getFlatId(), mConfig.getEncryptedSessionId(), 
    				request);
    		if (TextUtils.isEmpty(result)) {
    			LOGD("empty result");
    			if (!fromCache) {
    				int rId = insertRequestToCache(request, response);
    				request.cacheId = rId;
    			} else {
    				LOGD("return null, request from cache, to be deleted," + request.cacheId);
    				deleteRequestFromCache(request.cacheId);
    			}
        		response.reset();
        		response.requestId = request.cacheId;
    		} else {
    			LOGD("handle request, response=" + result);
    			XMLResponse xmlInfo = RequestHandler.parseXMLResult(getApplicationContext(), result);
    			if (xmlInfo == null) {
    				response.requestId = Response.ERROR_RESPONSE;
    				response.errorId = Response.ERROR_XML_ERROR;
    				return;
    			}
    			LOGD("sessionId=" + xmlInfo.sessionId);
    			if (xmlInfo.sessionId == null || xmlInfo.sessionId.equals("null") 
    					|| TextUtils.isEmpty(xmlInfo.sessionId)) {	
    				if (!fromCache) {
    					LOGD("+++++++++++++++++++++++++++++++++");
    					boolean loginSuccess = false;
	    				if (mConfig.isRegistered() && mConfig.isRememberPwd()) {
	    					loginSuccess = postLoginRequest();
	    					if (!loginSuccess) {
	    						response.errorId = Response.ERROR_LOGIN;
	    					}
	    				}
	    				if (!loginSuccess) {
	    					int rId = insertRequestToCache(request, response);
	    					request.cacheId = rId;
	    				}
	    				if (sync) {
		    				Intent intent = new Intent(Intent.ACTION_MAIN);
		    				if (!mConfig.isRegistered()) {
		    					intent.setClass(getApplicationContext(), NewRegisterActivity.class);
		    					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    				startActivity(intent);
		    				} else if (!loginSuccess){
		    					intent.setClass(getApplicationContext(), NewLoginActivity.class);
		    					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    				startActivity(intent);
		    				} else {
		    					handleRequestDirectly(request, fromCache, response, sync);
		    					return;
		    				}
	    				}
    				}
    				response.reset();
    				response.requestId = request.cacheId;
    			} else {
    				response.body = result;
        			response.requestId = Response.REAL_RESPONSE;
        			response.packageName = request.packageName;
        			response.time = System.currentTimeMillis();
    				mConfig.setSessionId(xmlInfo.sessionId);
	    			if (fromCache) {
	    				response.requestId = request.cacheId;
	    				int responseId = insertResponseToCache(response);
	    				setRequestHandledInCache(request.cacheId, responseId);
	    			}
    			}
    		}
    	}
		return;
    }
    
    private boolean postLoginRequest() {
    	Config config = Config.getInstance(getApplicationContext());
    	Request request = new Request();
    	request.action = Request.ACTION_GET;
    	request.items = "USERINFO";
    	request.versionId = "-1";
    	request.body = "<?xml version='1.0' encoding='utf-8'?><OBJECTS><OBJECT>"
    		+ "<PWD>" + Utils.Encrypt(config.getPassword()) + "</PWD>" 
    		+ "<DEVICEPIM>" + config.getFlatId() + "</DEVICEPIM>"
    		+ "<COMMPIM>" + DeviceInfo.getInstance(getApplicationContext()).getIMSI() + "</COMMPIM>"
    		+ "</OBJECT></OBJECTS>";
    	Log.d("postLoginRequest", "login:" + mRequest.body);
    	String result = mRequestHandler.handleRequest(config.getUserId(), config.getFlatId(), null, request);
    	if (TextUtils.isEmpty(result)) {
			return false;
		} else {
			XMLResponse xmlInfo = RequestHandler.parseXMLResult(getApplicationContext(), result);
	        if (xmlInfo.resultCode.equals(RequestHandler.SUCCESSFUL_RESULT_CODE)) {
	        	config.setSessionId(xmlInfo.sessionId);
	        	return true;
	        }
		}
    	return false;
    }
    
    private void launchHeartBeatService() {
    	Intent intent = new Intent(HeartBeatService.ACTION);
    	startService(intent);
    }
    
    private void stopHeartBeatService() {
    	Intent intent = new Intent(HeartBeatService.ACTION);
    	stopService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mSpaceObserver);
        stopHeartBeatService();
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
    	LOGD("onStartCommand,");
    	if (intent != null && intent.getIntExtra(PROXY_INTENT_TYPE, -1) == PROXY_INTENT_TYPE_POSTREQUESTS) {
    		handleRequestsInCache();
    	}
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
    private int insertRequestToCache(Request request, Response response) {
    	mContentValues.clear();
    	mContentValues.put(RequestProvider.ACTION, request.action);
    	mContentValues.put(RequestProvider.OWNER, request.packageName);
    	mContentValues.put(RequestProvider.ITEMS, request.items);
    	mContentValues.put(RequestProvider.VERSION_ID, request.versionId);
    	mContentValues.put(RequestProvider.BODY, request.body);
    	mContentValues.put(RequestProvider.TIME, System.currentTimeMillis());
    	mContentValues.put(RequestProvider.RESPONSE, RequestProvider.NO_RESPONSE);
    	if ("PUSHMSG.GETPUSHMSG".equals(request.items)) {
    		mContentValues.put(RequestProvider.TYPE, RequestProvider.TYPE_PUSHMESSAGE);
    	} else {
    		mContentValues.put(RequestProvider.TYPE, RequestProvider.TYPE_OTHERS);
    	}
    	Uri uri = null;
    	try {
    		uri = getContentResolver().insert(RequestProvider.CONTENT_URI, mContentValues);
    	} catch (Exception e) {
    		response.errorId = Response.ERROR_QUEUE_QUERY;
    	}
    	return (int) ContentUris.parseId(uri);
    }
    
    private void deleteRequestFromCache(int id) {
    	Uri deleteUri = ContentUris.withAppendedId(RequestProvider.CONTENT_URI, id);
    	getContentResolver().delete(deleteUri, null, null);
    }
    
    private void setRequestHandledInCache(int requestId, int responseId) {
    	mContentValues.clear();
    	mContentValues.put(RequestProvider.RESPONSE, responseId);
    	Uri uri = ContentUris.withAppendedId(RequestProvider.CONTENT_URI, requestId);
    	getContentResolver().update(uri, mContentValues, null, null);
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
    
    private void deleteResponseFromCacheWithRequestId(int requestId) {
    	getContentResolver().delete(ResponseProvider.CONTENT_URI, 
    			ResponseProvider.REQUEST_ID + " = " + requestId, null);
    }
    
    private Response getResponseFromCache(int requestId, String packageName) {
    	Cursor cursor = null;
    	try {
	    	cursor = getContentResolver().query(ResponseProvider.CONTENT_URI, 
	    			RESPONSE_PROJECTION, ResponseProvider.REQUEST_ID + " = " + requestId 
	    			+ " AND " + ResponseProvider.OWNER + " = '" + packageName + "'", null, null);
    	} catch (Exception e) {
    		mResponse.errorId = Response.ERROR_CACHE_QUERY;
    	}
    	if (cursor.getCount() > 0) {
    		cursor.moveToFirst();
    		mResponse.reset();
    		mResponse.requestId = Response.REAL_RESPONSE;
    		mResponse.body = cursor.getString(RESPONSE_BODY_COL);
    		mResponse.packageName = cursor.getString(RESPONSE_OWNER_COL);
    		mResponse.time = cursor.getLong(RESPONSE_TIME_COL);
    		deleteRequestFromCache(requestId);
    		deleteResponseFromCacheWithRequestId(requestId);
    	} else {
    		mResponse.reset();
    		mResponse.requestId = requestId;
    		mResponse.errorId = Response.ERROR_NOT_EXIST_IN_CACHE;
    	}
    	cursor.close();
    	return mResponse;
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
