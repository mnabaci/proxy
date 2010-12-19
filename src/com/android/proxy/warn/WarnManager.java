package com.android.proxy.warn;

import android.net.Uri;
import android.util.Log;

import com.android.proxy.warn.WarnProvider.OnProviderChangeListner;

public class WarnManager {
	
	private static final String TAG = "WarnManager";
	private static final boolean DEBUG = true;
	
	public static final int REPEAT_TYPE_NONE = 0;
	public static final int REPEAT_TYPE_MILLISECOND = 1;
	public static final int REPEAT_TYPE_DAY = 2;
	public static final int REPEAT_TYPE_WEEK = 3;
	public static final int REPEAT_TYPE_MONTH = 4;
	public static final int REPEAT_TYPE_YEAR = 5;
	
	public static final int WARN_TYPE_DIALOG = 0;
	public static final int WARN_TYPE_NOTIFY = 1;
	
	public static WarnObserver sWarnObserver = new WarnObserver();
	
	
	public static class WarnObserver implements OnProviderChangeListner {

		public void beforeDelete(Uri uri, String selection,
				String[] selectionArgs) {
			// TODO Auto-generated method stub
			LOGD("beforeDelete");
		}

		public void onInsert(Uri uri) {
			// TODO Auto-generated method stub
			LOGD("onInsert");
		}

		public void onUpdate(Uri uri, String selection, String[] selectionArgs) {
			// TODO Auto-generated method stub
			LOGD("onUpdate");
		}
		
	}
	
	private static void LOGD(String text) {
		if (DEBUG) {
			Log.d(TAG, text);
		}
	}

}
