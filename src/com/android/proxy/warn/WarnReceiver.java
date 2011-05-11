package com.android.proxy.warn;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;
import android.view.WindowManager;

import com.android.proxy.R;

public class WarnReceiver extends BroadcastReceiver {
	
	private static final String TAG = "WarnReceiver";
	private static final boolean DEBUG = true;
	private Context mContext;
	private AlertDialog mDialog;
	private Warn mWarn;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		mContext = context;
	    final byte[] data = intent.getByteArrayExtra(WarnManager.INTENT_EXTRA_WARN);
	    if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            mWarn = Warn.CREATOR.createFromParcel(in);
            if (mWarn.getShowType() == WarnManager.SHOW_TYPE_DIALOG) {
            	launchWarnDialog(mWarn);
            	launchAlertService();
            } else if (mWarn.getShowType() == WarnManager.SHOW_TYPE_NOTIFY) {
            	notifyWarn(mWarn);
            }
            
            if (mWarn.getRepeatType() != WarnManager.REPEAT_TYPE_NONE) {
                invokeNextAlarm(mWarn);
            }
            LOGD("onReceive," + mWarn.getID());
        }
	}
	
	private void createWarnDialog() {
		mDialog = new AlertDialog.Builder(mContext).create();
		mDialog.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		mDialog.setTitle(mContext.getString(R.string.warn_dialog_title));
		mDialog.setButton(DialogInterface.BUTTON_POSITIVE, mContext.getString(R.string.button_check), 
				new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				WarnManager.getInstance(mContext).setCheckState(mWarn.getID(), true);
				Intent intent = new Intent();
				if (mWarn.getIntentAction() != null) {
					intent.setAction(mWarn.getIntentAction());
				}
				if (mWarn.getIntentTarget() != null) {
					int splitterIndex = mWarn.getIntentTarget().lastIndexOf('/');
					if (splitterIndex > 0) {
			        	String packageName = mWarn.getIntentTarget().substring(0, splitterIndex);
			        	String className = mWarn.getIntentTarget().substring(splitterIndex+1);
			        	LOGD("packageName:" + packageName + ",className:" + className);
			            intent.setClassName(packageName, className);
					}
				}
				if (mWarn.getIntentData() != null) {
					intent.setData(Uri.parse(mWarn.getIntentData()));
				}
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
				stopAlertService();
				mDialog.dismiss();
			}
		});
		mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.button_close), 
				new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (mWarn.getRepeatType() != WarnManager.REPEAT_TYPE_NONE) {
					WarnManager.getInstance(mContext).setCheckState(mWarn.getID(), false);
				}
				stopAlertService();
				mDialog.dismiss();
			}
		});
	}
	
	private void launchWarnDialog(Warn warn) {
		if (mDialog == null) createWarnDialog();
		if (mDialog != null) {
			mDialog.setMessage(warn.getMessage());
			mDialog.setTitle(warn.getTitle());
			mDialog.show();
		}
	}
	
	private void notifyWarn(Warn warn) {
	    Intent intent = new Intent();
        if (warn.getIntentAction() != null) {
            intent.setAction(warn.getIntentAction());
        }
        if (warn.getIntentTarget() != null) {
        	int splitterIndex = warn.getIntentTarget().lastIndexOf('/');
        	if (splitterIndex > 0) {
	        	String packageName = warn.getIntentTarget().substring(0, splitterIndex);
	        	String className = warn.getIntentTarget().substring(splitterIndex+1);
	        	LOGD("packageName:" + packageName + ",className:" + className);
	            intent.setClassName(packageName, className);
        	}
        }
        if (warn.getIntentData() != null) {
            intent.setData(Uri.parse(warn.getIntentData()));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingNotify = PendingIntent.getActivity(mContext, warn.getID(), intent, 0);
		Notification n = new Notification(R.drawable.icon, warn.getMessage(), System.currentTimeMillis());
		n.setLatestEventInfo(mContext, warn.getTitle(), warn.getMessage(), pendingNotify);
		n.flags |= Notification.FLAG_SHOW_LIGHTS 
		        | Notification.FLAG_ONLY_ALERT_ONCE
		        | Notification.FLAG_AUTO_CANCEL;
		n.defaults |= Notification.DEFAULT_LIGHTS;
		if (warn.isSound()) {
		    n.defaults |= Notification.DEFAULT_SOUND;
		}
		if (warn.isVibrate()) {
		    n.defaults |= Notification.DEFAULT_VIBRATE;
		}
		NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(warn.getID());
		nm.notify(warn.getID(), n);
	}
	
	private void invokeNextAlarm(Warn warn) {
		WarnManager.getInstance(mContext).invokeNextWarn(warn);
	}
	
	private void launchAlertService() {
	    Intent intent = new Intent(WarnKlaxon.ACTION);
        intent.putExtra(WarnProvider.SOUND, mWarn.isSound());
        intent.putExtra(WarnProvider.VIBRATE, mWarn.isVibrate());
        intent.putExtra(WarnProvider.SHOW_TYPE, mWarn.getShowType());
        LOGD("sound:" + mWarn.isSound() + ",vibrate:" + mWarn.isVibrate() + ",showType:" + mWarn.getShowType());
        mContext.startService(intent);
	}
	
	private void stopAlertService() {
	    Intent intent = new Intent(WarnKlaxon.ACTION);
	    mContext.stopService(intent);
	}
	
	private static void LOGD(String text) {
		if (DEBUG) {
			Log.d(TAG, text);
		}
	}

}
