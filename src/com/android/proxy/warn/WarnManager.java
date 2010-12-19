package com.android.proxy.warn;

import android.net.Uri;
import android.util.Log;

import com.android.proxy.warn.WarnProvider.OnProviderChangeListner;

public class WarnManager {
	
	private static final String TAG = "WarnManager";
	private static final boolean DEBUG = true;
	
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
