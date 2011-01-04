package com.android.testapp;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.proxy.IProxyService;
import com.android.proxy.cache.Request;
import com.android.proxy.cache.Response;

@SuppressWarnings("unused")
public class TestActivity extends Activity {
    /** Called when the activity is first created. */
    
    private static final String TAG = "TestActivity";
    private static final boolean DEBUG = true;
    
    private IProxyService mService = null;
    
    private ServiceConnection mConnection = new ServiceConnection() {
        
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            LOGD("onServiceDisconnected");
            mService = null;
        }
        
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            LOGD("onServiceConnected");
            mService = IProxyService.Stub.asInterface(service);
            Request request = new Request();
            request.action = 100;
            Map map = new HashMap<String, String>();
            map.put("attribute", "value");
            request.objects.add(map);
            try {
				mService.postRequest(request);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Response response = mService.getResponse(1, getPackageName());
				LOGD("getResponse:" + response.resultCode + ",object id:" + response.objectIds[0]);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button insertButton = (Button)findViewById(R.id.insert);
        Button viewButton = (Button)findViewById(R.id.view);
        Button bindButton = (Button)findViewById(R.id.bind);
        Button unbindButton = (Button)findViewById(R.id.unbind);
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
        if (bindButton != null) {
            bindButton.setOnClickListener(new View.OnClickListener() {
                
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    bindService(new Intent(IProxyService.class.getName()), mConnection, Context.BIND_AUTO_CREATE);
                }
            });
        }
        if (unbindButton != null) {
            unbindButton.setOnClickListener(new View.OnClickListener() {
                
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (mService != null) {
                        unbindService(mConnection);
                    }
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