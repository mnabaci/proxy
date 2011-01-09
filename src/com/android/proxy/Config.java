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

import android.util.Log;

import com.android.proxy.utils.Environment;
import com.android.proxy.utils.INIFile;
import com.android.proxy.utils.ThemeUtils;

public class Config {
	
	private static final String TAG = "Config";
    private final static boolean DEBUG = true;
    
    private static final String SECTION_CONFIG = "CONFIG";
    private static final String PROPERTY_MAX_SPACE = "MAX_SPACE";
    private static final String PROPERTY_URL = "URL";
    
    private static final String TAG_PACKAGE = "package";
    
    private static final long VAL_MAX_SPACE = 1000000;
        
    private static Config sConfig = null;
    private String mConfigFilePath;
    private INIFile mConfigFile;
    
    private long mMaxSpace;
    private String mCloudUrl;
    
    private String mUserId;
    private String mFlatId;
    private String mSessionId;
    private Set<String> mTrustPackages;
    
    public static Config getInstance() {
    	if (sConfig == null) {
    		sConfig = new Config();
    	}
    	return sConfig;
    }
    
    public static void releaseInstance() {
    	sConfig = null;
    }
    
    private Config() {
    	mConfigFilePath = Environment.CONFIG_FILE_PATH;
    	mConfigFile = new INIFile(mConfigFilePath);
    	mMaxSpace = ThemeUtils.getLong(mConfigFile, SECTION_CONFIG, PROPERTY_MAX_SPACE, VAL_MAX_SPACE);
    	mCloudUrl = ThemeUtils.getText(mConfigFile, SECTION_CONFIG, PROPERTY_URL, "");
    	mTrustPackages = new HashSet<String>();
    	loadTrustPackages();
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
    	return "0";
    }
    
    public String getFlatId() {
    	return "0";
    }
    
    public String getSessionId() {
    	return "0";
    }
    
    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}
