package com.android.testapp;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

@SuppressWarnings("unused")
public class TestActivity extends Activity {
    /** Called when the activity is first created. */
    
    private static final String TAG = "TestActivity";
    private static final boolean DEBUG = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
    
    private void testAlarm() {
    	Intent intent = new Intent(TestActivity.this, TestReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(TestActivity.this, 0, intent, 0);

        // We want the alarm to go off 30 seconds from now.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        
        PendingIntent toCancel = PendingIntent.getBroadcast(TestActivity.this, 1, intent, 0);
        am.cancel(toCancel);
    }

    private void testCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        LOGD("before modify:" + calendar.toString());
        calendar.add(Calendar.MONTH, 1);
        LOGD("after modify:"  + calendar.toString());
    }
    
    private static final void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }
}