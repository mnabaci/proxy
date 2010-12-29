package com.android.proxy.internet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    public static final String KEY1 = "6E09C97EB8798EEB";
    
    private final String mBaseURL;
    private static final String URL_PREFIX = "SogouServlet?cmd=";
    private static final int TIMEOUT = 15 * 1000;
    
    private static Context mContext;
    private static String mCurGetCookieTime;
    private static String mCurSoftwareVersion;
    private static String mProxyHost;
    private static int mProxyPort = 0;
    private static NetWorkSettingInfoManager gNetWorkSettingInfoManager;
    
    public static class SogouHttpHeader {
        public static final String SOGOU_TYPE = "SOGOU_TYPE";
        public static final String SOGOU_COOKIE = "S-COOKIE";
        public static final String SOGOU_PLATFORM = "SOGOU_PLATFORM";
        public static final String SOGOU_VERSION = "SOGOU_VERSION";
        public static final String SOGOU_BIULDTIME = "SOGOU_BIULDID";
    }

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

    public String getURL(int type) {
        String cmd = null;
        switch (type) {
        case TYPE_REGIST:
            cmd = "register";
            break;
        case TYPE_LOGIN:
            cmd = "login";
            break;
        case TYPE_FEEDBACK:
            cmd = "feedback";
            break;
        case TYPE_DICT_UPDATE:
            cmd = "keyupdate";
            break;
        case TYPE_SOFTWARE_UPDATE:
            cmd = "softwareupdate";
            break;
        case TYPE_DICT_BACKUP:
            cmd = "keyupload";
            break;
        case TYPE_DICT_MERGE:
            cmd = "keymerge";
            break;
        case TYPE_SOFTWARE_STATISTIC:
            cmd = "update";
            break;
        }
        StringBuilder url = new StringBuilder();
        url.append(mBaseURL).append(URL_PREFIX).append(cmd);
        if (DEBUG) Log.d(TAG, "--------- The url with cmd info = " + url + " --------------");

        return appendArguInfoForUrl(type, url.toString());
    }

    public void setHeader(HttpRequestBase post, File uploadFile) {
        post.setHeader(HTTP.CONTENT_TYPE, "text/plain");
        post.setHeader("Accept", "*/*");
        //post.setHeader("Accept-Encoding", "gzip,deflate");
        post.setHeader(SogouHttpHeader.SOGOU_PLATFORM, "Android");
        post.setHeader(SogouHttpHeader.SOGOU_VERSION, mCurSoftwareVersion);
        post.setHeader(SogouHttpHeader.SOGOU_BIULDTIME, mCurGetCookieTime);
        if (uploadFile != null) {
            InputStreamEntity entity = null;
            try {
                FileInputStream fis = new FileInputStream(uploadFile);
                entity = new InputStreamEntity(fis, fis.available());
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ((HttpPost) post).setEntity(entity);
        }
    }
    
    public void setHeader(HttpRequestBase post, Map<String, String> uploadContextMap) {
        post.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
        post.setHeader("Accept", "*/*");
        //post.setHeader("Accept-Encoding", "gzip,deflate");
        post.setHeader(SogouHttpHeader.SOGOU_PLATFORM, "Android");
        post.setHeader(SogouHttpHeader.SOGOU_VERSION, mCurSoftwareVersion);
        post.setHeader(SogouHttpHeader.SOGOU_BIULDTIME, mCurGetCookieTime);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (String key : uploadContextMap.keySet()) {
            nvps.add(new BasicNameValuePair(key, uploadContextMap.get(key)));
        }
        try {
            ((HttpPost) post).setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            LOGD(e.getMessage());
        }
        LOGD("-------- HttpPost header Info ----------");
        LOGD("         The Content-Type = " + "application/x-www-form-urlencoded");
        LOGD("         The Accept-Encoding = " + "gzip,deflate");
        LOGD("         The SOGOU_PLATFORM = " + "Android");
        LOGD("         The SOGOU_VERSION = " + mCurSoftwareVersion);
        LOGD("         The SOGOU_BIULDTIME = " + mCurGetCookieTime);
        LOGD("         The entity Content type = " + ((HttpPost) post).getEntity().getContentType());
        LOGD("         The entity body = " + ((HttpPost) post).getEntity().toString());
        LOGD("----------------------------------------");
    }
    
    public void setHeader(HttpGet get) {
        get.setHeader(HTTP.CONTENT_TYPE, "text/plain");
        get.setHeader("Accept", "*/*");
        //get.setHeader("Accept-Encoding", "gzip,deflate");
        get.setHeader(SogouHttpHeader.SOGOU_PLATFORM, "Android");
        get.setHeader(SogouHttpHeader.SOGOU_VERSION, mCurSoftwareVersion);
        get.setHeader(SogouHttpHeader.SOGOU_BIULDTIME, mCurGetCookieTime);
    }

    public void setDefaultHeaderParams(HttpRequestBase requestBase) {
        requestBase.setHeader(HTTP.CONTENT_TYPE, "text/plain");
        requestBase.setHeader("Accept", "*/*");
        requestBase.setHeader(SogouHttpHeader.SOGOU_PLATFORM, "Android");
        requestBase.setHeader(SogouHttpHeader.SOGOU_VERSION, mCurSoftwareVersion);
        requestBase.setHeader(SogouHttpHeader.SOGOU_BIULDTIME, mCurGetCookieTime);
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
    
    private String appendArguInfoForUrl(int type, String baseUrl) {
        StringBuilder argus = new StringBuilder();
        switch (type) {
        case TYPE_REGIST:
            break;
        case TYPE_LOGIN:
            break;
        case TYPE_FEEDBACK:
            break;
        case TYPE_DICT_UPDATE:
            argus.append("&d=").append(mDictInfo.getPreUpdateTime());
            break;
        case TYPE_SOFTWARE_UPDATE:
            break;
        case TYPE_DICT_BACKUP:
            break;
        case TYPE_DICT_MERGE:
            break;
        case TYPE_SOFTWARE_STATISTIC:
            break;
        }
        LOGD("======= The url to connect server = " + baseUrl + argus.toString() + " ======");
        return baseUrl + argus.toString();
    }

    private NetWorkSettingInfoManager(Context context) {
        mContext = context;
        mBaseURL = null;
//        mBaseURL = context.getString(R.string.sogou_base_url);
    }

    private static String getCurTime() {
        Calendar cl = Calendar.getInstance();
        StringBuilder time = new StringBuilder();
        time.append(cl.get(Calendar.YEAR)).append(cl.get(Calendar.MONTH)).append(cl.get(Calendar.DAY_OF_MONTH))
                .append(cl.get(Calendar.HOUR_OF_DAY)).append(cl.get(Calendar.MINUTE))
                .append(cl.get(Calendar.SECOND));
        LOGD("------ The Current tiem is = " + time.toString() + " -------");
        return time.toString();
    }

    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }
}
