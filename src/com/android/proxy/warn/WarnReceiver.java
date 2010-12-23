package com.android.proxy.warn;

import com.android.proxy.R;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;
import android.view.WindowManager;

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
            if (mWarn.getShowType() == WarnManager.WARN_TYPE_DIALOG) {
            	launchWarnDialog(mWarn);
            } else if (mWarn.getShowType() == WarnManager.WARN_TYPE_NOTIFY) {
            	notifyWarn(mWarn);
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
					intent.setClassName(mContext, mWarn.getIntentTarget());
				}
				if (mWarn.getIntentData() != null) {
					intent.setData(Uri.parse(mWarn.getIntentData()));
				}
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
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
				mDialog.dismiss();
			}
		});
	}
	
	private void launchWarnDialog(Warn warn) {
		if (mDialog == null) createWarnDialog();
		if (mDialog != null) {
			mDialog.setMessage(warn.getMessage());
			mDialog.show();
		}
	}
	
	private void notifyWarn(Warn warn) {
		
	}
	
	private void invokeNextAlarm() {
		
	}
	
	private static void LOGD(String text) {
		if (DEBUG) {
			Log.d(TAG, text);
		}
	}

}
