package com.android.proxy.utils;

import java.io.File;
import java.io.IOException;

import android.content.Context;

public class Environment {
	
	private static final String TAG = "Environment";
	
	public static final String CONFIG_FILE_NAME = "config.ini";
	public static final String CACHE_DIR_NAME = ".cache";
	
	private Context mContext;
	private static Environment sEnvironment = null;
	public static String FILES_DIR_PATH;
	public static String CONFIG_FILE_PATH;
	
	public static Environment getInstance(Context context) {
		if (sEnvironment == null) {
			sEnvironment = new Environment(context);
		}
		return sEnvironment;
	}
	
	public static void releaseInstance() {
		sEnvironment = null;
	}
	
	private Environment(Context context) {
		mContext = context;
		File files = mContext.getFilesDir();
		if (!files.exists()) {
			files.mkdirs();
		}
		FILES_DIR_PATH = mContext.getFilesDir().getAbsolutePath();
		CONFIG_FILE_PATH = FILES_DIR_PATH + "/" + CONFIG_FILE_NAME;
		init();
	}
	
	private void init() {
		try {
			ZipUtil.outputFile(mContext.getAssets().open(CONFIG_FILE_NAME), 
			        FILES_DIR_PATH + File.separator, CONFIG_FILE_NAME);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
