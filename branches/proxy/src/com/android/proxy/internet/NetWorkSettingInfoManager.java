package com.android.proxy.internet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

public class NetWorkSettingInfoManager {
    private static final String TAG = "NetWorkSettingInfo";
    private static final boolean DEBUG = false;

    public static final int TYPE_REGIST = 0;
    public static final int TYPE_LOGIN = 1;
    public static final int TYPE_FEEDBACK = 2;
    public static final int TYPE_DICT_UPDATE = 3;
    public static final int TYPE_SOFTWARE_UPDATE = 4;
    public static final int TYPE_DICT_BACKUP = 5;
    public static final int TYPE_DICT_MERGE = 6;
    public static final int TYPE_SOFTWARE_STATISTIC = 7;
    
    private static final int TIMEOUT = 15 * 1000;
    
    private static Context mContext;
    private static String mCurGetCookieTime;
    private static String mCurSoftwareVersion;
    private static String mProxyHost;
    private static int mProxyPort = 0;
    private static NetWorkSettingInfoManager gNetWorkSettingInfoManager;

    public interface DictInfo {
        public String getPreUpdateTime();

        public String getDictName();

        public String getDictSize();

        public String getDictTotalSize();

        public String getDictStartOffset();

        public String getDictMaxSize();
    }

    private DictInfo mDictInfo;

    public static NetWorkSettingInfoManager getInstance(Context context) {
        if (gNetWorkSettingInfoManager == null) {
            gNetWorkSettingInfoManager = new NetWorkSettingInfoManager(context);
        }
        return gNetWorkSettingInfoManager;
    }

    public void setDictInfoInterface(DictInfo info) {
        mDictInfo = info;
    }
    
    public HttpParams getParams() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        if (getProxy() == true) {
            final HttpHost proxy = new HttpHost(mProxyHost, mProxyPort, "http");
            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
        return params;
    }

    private static boolean getProxy() {
        ConnectivityManager ConnMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = ConnMgr.getActiveNetworkInfo();
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                //comment for XT800, it work well without the proxy settings
                //under wifi env
//                mProxyHost = android.net.Proxy.getHost(mContext);
//                mProxyPort = android.net.Proxy.getPort(mContext);
                mProxyPort = 0;
                mProxyHost = null;
            } else {
                mProxyHost = android.net.Proxy.getDefaultHost();
                mProxyPort = android.net.Proxy.getDefaultPort();
            }
            LOGD("[[getProxy]] host = " + mProxyHost + " port = " + mProxyPort);
            return (!TextUtils.isEmpty(mProxyHost) && mProxyPort != 0);
        }
        return false;
    }

    private NetWorkSettingInfoManager(Context context) {
        mContext = context;
    }

    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }
}
