package com.android.proxy;

import java.io.File;
import java.io.FileInputStream;
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
import android.util.Log;

import com.android.proxy.utils.DeviceInfo;
import com.android.proxy.utils.Environment;
import com.android.proxy.utils.INIFile;
import com.android.proxy.utils.ThemeUtils;

public class Config {
	
	private static final String TAG = "Config";
    private final static boolean DEBUG = true;
    
    private static final String SECTION_CONFIG = "CONFIG";
    private static final String PROPERTY_MAX_SPACE = "MAX_SPACE";
    private static final String PROPERTY_URL = "URL";
    
    public static final String PREF_USERID = "pref_userid";
    public static final String PREF_SESSIONID = "pref_sessionid";
    public static final String EMPTY_STRING = "";
    
    private static final String TAG_PACKAGE = "package";
    
    private static final long VAL_MAX_SPACE = 1000000;
        
    private static Config sConfig = null;
    private String mConfigFilePath;
    private INIFile mConfigFile;
    
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    
    private long mMaxSpace;
    private String mCloudUrl;
    
    private String mUserId;
    private String mFlatId;
    private String mSessionId;
    private Set<String> mTrustPackages;
    
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
    	mMaxSpace = ThemeUtils.getLong(mConfigFile, SECTION_CONFIG, PROPERTY_MAX_SPACE, VAL_MAX_SPACE);
    	mCloudUrl = ThemeUtils.getText(mConfigFile, SECTION_CONFIG, PROPERTY_URL, "");
    	mTrustPackages = new HashSet<String>();
    	loadTrustPackages();
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
    	return DeviceInfo.getInstance(mContext).getIMEI();
    }
    
    public String getSessionId() {
    	return mSharedPreferences.getString(PREF_SESSIONID, EMPTY_STRING);
    }
    
    public void setUserId(String id) {
    	mEditor.putString(PREF_USERID, id);
    	mEditor.commit();
    }
    
    public void setSessionId(String id) {
    	mEditor.putString(PREF_SESSIONID, id);
    	mEditor.commit();
    }
    
    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}
