package com.android.testapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TestActivity extends Activity {
    /** Called when the activity is first created. */
    
    private static final String TAG = "TestActivity";
    private static final boolean DEBUG = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//        ContentValues values = new ContentValues();
//        long time = System.currentTimeMillis();
//        values.put("owner", "com.android.proxy");
//        values.put("trigger", time);
//        getContentResolver().insert(Uri.parse("content://com.android.proxy.warn/warns"), values);
//        String[] projection = new String[]{"_id", "owner", "trigger"};
//        Cursor c = getContentResolver().query(Uri.parse("content://com.android.proxy.warn/warns"), projection, null, null, null);
//        if (c.getCount() > 0) {
//            c.moveToFirst();
//            Log.d(TAG, "count:" + c.getCount());
//            Log.d(TAG, "owner:" + c.getString(1));
//        }
//        c.close();
        Button insertButton = (Button)findViewById(R.id.insert);
        Button viewButton = (Button)findViewById(R.id.view);
        if (insertButton != null) {
            insertButton.setOnClickListener(new View.OnClickListener() {
                
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setClass(getApplicationContext(), EditWarnActivity.class);
                    startActivity(intent);
                }
            });
        }
        if (viewButton != null) {
            viewButton.setOnClickListener(new View.OnClickListener() {
                
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClass(getApplicationContext(), ViewWarnActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
    
    private static final void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }
}