package com.android.proxy;

import android.util.Log;

import com.android.proxy.utils.Environment;
import com.android.proxy.utils.INIFile;
import com.android.proxy.utils.ThemeUtils;

public class Config {
	
	private static final String TAG = "Config";
    private final static boolean DEBUG = true;
    
    private static final String SECTION_CONFIG = "CONFIG";
    private static final String PROPERTY_MAX_SPACE = "MAX_SPACE";
    
    private static final long VAL_MAX_SPACE = 1000000;
        
    private static Config sConfig = null;
    private String mConfigFilePath;
    private INIFile mConfigFile;
    
    private long mMaxSpace;
    
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
    }
    
    public long getMaxSpace() {
        return mMaxSpace;
    }
    
    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}
