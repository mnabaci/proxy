package com.android.proxy.warn;

import java.util.Vector;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
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
	
	private static WarnManager sWarnManager = null;
	private static String[] projection = {WarnProvider._ID, WarnProvider.OWNER, WarnProvider.TRIGGER_TIME, 
		WarnProvider.REPEAT_TYPE, WarnProvider.REPEAT_INTERVAL_TIME, WarnProvider.FINISH_TIME,
		WarnProvider.MESSAGE, WarnProvider.VIBRATE, WarnProvider.SOUND, WarnProvider.SHOW_TYPE,
		WarnProvider.INTENT_TARGET, WarnProvider.INTENT_ACTION, WarnProvider.INTENT_DATA,
		WarnProvider.CHECKED};
	private static String[] projection_id = {WarnProvider._ID};
	private static final int ID_COLUMN = 0;
	private static final int OWNER_COLUMN = 1;
	private static final int TRIGGER_TIME_COLUMN = 2;
	private static final int REPEAT_TYPE_COLUMN = 3;
	private static final int REPEAT_INTERVAL_TIME_COLUMN = 4;
	private static final int FINISH_TIME_COLUMN = 5;
	private static final int MESSAGE_COLUMN = 6;
	private static final int VIBRATE_COLUMN = 7;
	private static final int SOUND_COLUMN = 8;
	private static final int SHOW_TYPE_COLUMN = 9;
	private static final int INTENT_TARGET_COLUMN = 10;
	private static final int INTENT_ACTION_COLUMN = 11;
	private static final int INTENT_DATA_COLUMN = 12;
	private static final int CHECKED_COLUMN = 13;
	
	public static final String INTENT_EXTRA_WARN = "warn";
	
	public WarnObserver warnObserver = null;
	
	private Context mContext;
	
	private WarnManager(Context context) {
		mContext = context;
		warnObserver = new WarnObserver();
	}
	
	public static synchronized WarnManager getInstance(Context context) {
		if (sWarnManager == null) {
			sWarnManager = new WarnManager(context);
		}
		return sWarnManager;
	}
	
	private void handleWarns(Uri uri, String selection, String[] selectionArgs, boolean add) {
		Cursor cursor = mContext.getContentResolver().query(uri, projection, 
				selection, selectionArgs, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			AlarmManager alarm = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
			do {
				Warn warn = obtainWarnFromCursor(cursor);
				if (add && !warn.isForever() 
						&& warn.getNextAlarmTime() < System.currentTimeMillis()) {
					continue;
				}
			    Intent intent = new Intent(mContext, WarnReceiver.class);
				Parcel out = Parcel.obtain();
				warn.writeToParcel(out, 0);
				out.setDataPosition(0);
				intent.putExtra(INTENT_EXTRA_WARN, out.marshall());
				PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 
						warn.getID(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
				if (add) {
					alarm.set(AlarmManager.RTC_WAKEUP, warn.getNextAlarmTime(), pendingIntent);
				} else {
					alarm.cancel(pendingIntent);
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
	}
	
	public static Warn obtainWarnFromCursor(Cursor cursor) {
		Warn warn = new Warn();
		warn.setID(cursor.getInt(ID_COLUMN));
		warn.setOwner(cursor.getString(OWNER_COLUMN));
		warn.setTriggerTime(cursor.getLong(TRIGGER_TIME_COLUMN));
		warn.setRepeatType(cursor.getInt(REPEAT_TYPE_COLUMN));
		warn.setRepeatInterval(cursor.getLong(REPEAT_INTERVAL_TIME_COLUMN));
		warn.setFinishTime(cursor.getLong(FINISH_TIME_COLUMN));
		warn.setMessage(cursor.getString(MESSAGE_COLUMN));
		warn.setVibrate(Boolean.valueOf(cursor.getString(VIBRATE_COLUMN)));
		warn.setSound(Boolean.valueOf(cursor.getString(SOUND_COLUMN)));
		warn.setShowType(cursor.getInt(SHOW_TYPE_COLUMN));
		warn.setIntentTarget(cursor.getString(INTENT_TARGET_COLUMN));
		warn.setIntentAction(cursor.getString(INTENT_ACTION_COLUMN));
		warn.setIntentData(cursor.getString(INTENT_DATA_COLUMN));
		warn.setChecked(Boolean.valueOf(cursor.getString(CHECKED_COLUMN)));
		return warn;
	}
	
	public void deleteWarnFromDB(int warnID) {
		Uri uri = ContentUris.withAppendedId(WarnProvider.CONTENT_URI, warnID);
		mContext.getContentResolver().delete(uri, null, null);
	}
	
	public void setCheckState(int warnID, boolean checked) {
		Uri uri = ContentUris.withAppendedId(WarnProvider.CONTENT_URI, warnID);
		ContentValues values = new ContentValues();
		values.put(WarnProvider.CHECKED, checked);
		mContext.getContentResolver().update(uri, values, null, null);
	}
	
	public class WarnObserver implements OnProviderChangeListner {
		
		private Vector<Integer> mUpdateIDVector;

		public WarnObserver() {
			mUpdateIDVector = new Vector<Integer>();
		}
		
		public void beforeDelete(Uri uri, String selection,
				String[] selectionArgs) {
			// TODO Auto-generated method stub
			LOGD("beforeDelete");
			handleWarns(uri, selection, selectionArgs, false);
		}

		public void onInsert(Uri uri) {
			LOGD("onInsert," + mContext.getPackageName());
			handleWarns(uri, null, null, true);
		}

		public void afterUpdate() {
			// TODO Auto-generated method stub
			LOGD("afterUpdate");
			for (int i = 0; i < mUpdateIDVector.size(); i++) {
				Uri uri = ContentUris.withAppendedId(WarnProvider.CONTENT_URI, mUpdateIDVector.get(i));
				handleWarns(uri, null, null, true);
			}
		}

		public void beforeUpdate(Uri uri, String selection,
				String[] selectionArgs) {
			// TODO Auto-generated method stub
			LOGD("beforeUpdate");
			handleWarns(uri, selection, selectionArgs, false);
			Cursor cursor = mContext.getContentResolver().query(uri, projection_id, selection, selectionArgs, null);
			if (cursor.getCount() > 0) {
				mUpdateIDVector.clear();
				cursor.moveToFirst();
				do {
					mUpdateIDVector.add(cursor.getInt(0));
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		
	}
	
	private static void LOGD(String text) {
		if (DEBUG) {
			Log.d(TAG, text);
		}
	}

}
