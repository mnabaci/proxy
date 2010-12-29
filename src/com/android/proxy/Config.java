package com.android.proxy;

import android.util.Log;

import com.android.proxy.utils.Environment;
import com.android.proxy.utils.INIFile;

public class Config {
	
	private static final String TAG = "Config";
    private final static boolean DEBUG = true;
        
    private static Config sConfig = null;
    private String mConfigFilePath;
    private INIFile mConfigFile;
    
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
    }
    
    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}
