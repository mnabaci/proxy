package com.android.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.proxy.utils.DeviceInfo;
import com.android.proxy.utils.Environment;
import com.android.proxy.utils.INIFile;
import com.android.proxy.utils.ThemeUtils;

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
    public static final String PREF_SESSIONID = "pref_sessionid";
    public static final String EMPTY_STRING = "";
    
    private static final String TAG_PACKAGE = "package";
        
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
    	mFlatId = ThemeUtils.getText(mConfigFile, SECTION_CONFIG, PROPERTY_FLATID, "");
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
    
    public void setUserId(String id) {
    	mEditor.putString(PREF_USERID, id);
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
    
    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}
