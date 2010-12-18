package com.android.testapp;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class TestActivity extends Activity {
    /** Called when the activity is first created. */
    
    private static final String TAG = "TestActivity";
    private static final boolean DEBUG = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ContentValues values = new ContentValues();
        long time = System.currentTimeMillis();
        values.put("owner", "com.android.proxy");
        values.put("trigger", time);
        getContentResolver().insert(Uri.parse("content://com.android.proxy.warn/warns"), values);
        String[] projection = new String[]{"_id", "owner", "trigger"};
        Cursor c = getContentResolver().query(Uri.parse("content://com.android.proxy.warn/warns"), projection, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            Log.d(TAG, "count:" + c.getCount());
            Log.d(TAG, "owner:" + c.getString(1));
        }
        c.close();
    }
    
    private static final void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }
}