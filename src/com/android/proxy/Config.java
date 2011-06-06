package com.android.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.proxy.utils.DeviceInfo;
import com.android.proxy.utils.Environment;
import com.android.proxy.utils.INIFile;
import com.android.proxy.utils.ThemeUtils;
import com.android.proxy.utils.Utils;

public class Config {
	
	private static final String TAG = "Config";
    private final static boolean DEBUG = true;
    
    private static final long DEFAULT_VALUE_HEARTBEAT_INTERVAL = 5*60*1000;
    private static final long DEFAULT_VALUE_MAX_SPACE = 1000000;
    
    private static final String SECTION_CONFIG = "CONFIG";
    private static final String PROPERTY_MAX_SPACE = "MAX_SPACE";
    private static final String PROPERTY_URL = "URL";
    private static final String PROPERTY_FLATID = "FLAT_ID";
    private static final String PROPERTY_HEARTBEAT_INTERVAL = "HEARTBEAT_INTERVAL";
    
    private static final String RANDOM = "12345678912345678912345678912345";  //random
    
    public static final String PREF_USERID = "pref_userid";
    public static final String PREF_PWD = "pref_pwd";
    public static final String PREF_SESSIONID = "pref_sessionid";
    public static final String PREF_REMEMBER_PWD = "pref_remember_pwd";
    public static final String PREF_FLATID = "pref_flatid";
    public static final String EMPTY_STRING = "";
    
    public static final String ANONOYMOUS_SESSIONID = "anonymous";
    
    private static final String TAG_PACKAGE = "package";
    private static final String LOCAL_PWD_KEY = "a09771a3e9601631";
        
    private static Config sConfig = null;
    private String mConfigFilePath;
    private INIFile mConfigFile;
    
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    
    private long mMaxSpace;
    private String mCloudUrl;
    private long mHeartBeatInterval;
    
    private String mUserId;
    private String mFlatId;
    private String mSessionId;
    private Set<String> mTrustPackages;
    
    private StringBuffer mStringBuffer;
    
    public static Config getInstance(Context context) {
    	if (sConfig == null) {
    		sConfig = new Config(context);
    	}
    	return sConfig;
    }
    
    public static void releaseInstance() {
    	sConfig.release();
    	sConfig = null;
    }
    
    private Config(Context context) {
    	mContext = context;
    	mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEditor = mSharedPreferences.edit();
    	mConfigFilePath = Environment.getInstance(mContext).CONFIG_FILE_PATH;
    	mConfigFile = new INIFile(mConfigFilePath);
    	mMaxSpace = ThemeUtils.getLong(mConfigFile, SECTION_CONFIG, PROPERTY_MAX_SPACE, DEFAULT_VALUE_MAX_SPACE);
    	mCloudUrl = ThemeUtils.getText(mConfigFile, SECTION_CONFIG, PROPERTY_URL, "");
    	mFlatId = mSharedPreferences.getString(PREF_FLATID, EMPTY_STRING);
    	if (TextUtils.isEmpty(mFlatId)) {
    		mFlatId = genUUID();
    		mEditor.putString(PREF_FLATID, mFlatId);
    		mEditor.commit();
    	}
    	mHeartBeatInterval = ThemeUtils.getLong(mConfigFile, SECTION_CONFIG, 
    			PROPERTY_HEARTBEAT_INTERVAL, DEFAULT_VALUE_HEARTBEAT_INTERVAL);
    	mTrustPackages = new HashSet<String>();
    	loadTrustPackages();
    	mStringBuffer = new StringBuffer();
    }
    
    private void release() {
    	mContext = null;
    	mSharedPreferences = null;
    	mEditor = null;
    	mConfigFilePath = null;
    	mConfigFile = null;
    	mCloudUrl = null;
    	mTrustPackages.clear();
    	mTrustPackages = null;
    	mStringBuffer.delete(0, mStringBuffer.length());
    	mStringBuffer.setLength(0);
    	mStringBuffer = null;
    }
    
    private void loadTrustPackages() {
    	mTrustPackages.clear();
    	File trustFile = null;
        XmlPullParserFactory factory = null;
        FileInputStream is = null;
        try {
            trustFile = new File(Environment.TRUST_LIST_FILE_PATH);
            if (!trustFile.exists() || !trustFile.isFile()) {
                return;
            }

            is = new FileInputStream(trustFile);
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(is, null);
            int event;
            boolean inText = false;
            while ((event = xpp.next()) != XmlPullParser.END_DOCUMENT) {          	
            	if (event == XmlPullParser.START_TAG && xpp.getName().equals(TAG_PACKAGE)) {
        			inText = true;	
            	} else if (event == XmlPullParser.END_TAG && xpp.getName().equals(TAG_PACKAGE)) {
            		inText = false;
            	} else if (event == XmlPullParser.TEXT && inText) {
            		mTrustPackages.add(xpp.getText());
            	}
            }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            trustFile = null;
            factory = null;
            is = null;
        }
    }
    
    public boolean isTrustedPackage(String packageName) {
    	return (mTrustPackages != null && mTrustPackages.contains(packageName));
    }
    
    public long getMaxSpace() {
        return mMaxSpace;
    }
    
    public String getCloudUrl() {
    	return mCloudUrl;
    }
    
    public String getUserId() {
    	return mSharedPreferences.getString(PREF_USERID, EMPTY_STRING);
    }
    
    public String getPassword() {
    	String pwd = mSharedPreferences.getString(PREF_PWD, EMPTY_STRING);
    	return Utils.Decrypt(pwd, LOCAL_PWD_KEY);
    }
    
    public boolean isRegistered() {
    	return !TextUtils.isEmpty(getUserId());
    }
    
    public boolean isRememberPwd() {
    	return mSharedPreferences.getBoolean(PREF_REMEMBER_PWD, false);
    }
    
    public String getFlatId() {
//    	return DeviceInfo.getInstance(mContext).getIMEI();
    	return mFlatId; 
    }
    
    public String getSessionId() {
    	return mSharedPreferences.getString(PREF_SESSIONID, EMPTY_STRING);
    }
    
    public long getHeartBeatInterval() {
    	return mHeartBeatInterval;
    }
    
    public int getVersionCode() {
    	int versionCode = 0;
        try {
            versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            LOGD(e.toString());
        }
        return versionCode;
    }
    
    public void setUserId(String id) {
    	mEditor.putString(PREF_USERID, id);
    	mEditor.commit();
    }
    
    public void setPassword(String pwd) {
    	mEditor.putString(PREF_PWD, Utils.Encrypt(pwd, LOCAL_PWD_KEY));
    	mEditor.commit();
    }
    
    public void setRememberPwd(boolean remember) {
    	mEditor.putBoolean(PREF_REMEMBER_PWD, remember);
    	mEditor.commit();
    }
    
    public void setSessionId(String id) {
    	mEditor.putString(PREF_SESSIONID, id);
    	mEditor.commit();
    }
    
    public String getEncryptedSessionId() {
    	
    	String sessionid = getSessionId();  //sessionId
    	if (TextUtils.isEmpty(sessionid)) {
    		return null;
    	}

        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sha.update((sessionid+RANDOM).getBytes());
        byte[] sessioid2 = sha.digest();
        mStringBuffer.delete(0, mStringBuffer.length());
        mStringBuffer.setLength(0);
        for (int i = 0; i < sessioid2.length; i++) {
            mStringBuffer.append(sessioid2[i]);
        }
        return mStringBuffer.toString()+RANDOM;
    }
    
    public static String genUUID() {
    	// TODO 检测，如果使用标准办法，采集不到平板ID，则采用此办法随机生成UUID，此ID将作为该平板电脑的唯一标识存在；
    	// 需要注意的是：第一次生成UUID后应该永久保存起来，哪怕是重启平板或者更新升级本程序，UUID都应保持不变；
    	UUID uuid = UUID.randomUUID();
    	String str = uuid.toString();
    	String temp = str.substring(0,8)+str.substring(9, 13)+str.substring(14, 18)+str.substring(19, 23)+str.substring(24);
    	return temp;
    }
    
    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}
