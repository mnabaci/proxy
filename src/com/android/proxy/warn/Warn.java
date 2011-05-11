package com.android.proxy.warn;

import java.util.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

public class Warn implements Parcelable {
	
	private int mID;
	private String mOwner;
	private long mTriggerTime;
	private int mRepeatType;
	private long mRepeatInterval;
	private long mFinishTime;
	private String mMessage;
	private boolean mVibrate;
	private boolean mSound;
	private int mShowType;
	private String mIntentTarget;
	private String mIntentAction;
	private String mIntentData;
	private boolean mChecked;
	private String mTitle;
	private String mEventId;
	
	public Warn() {
		
	}
	
	public void setID(int id) {
		mID = id;
	}
	
	public int getID() {
		return mID;
	}
	
	public String getOwner() {
		return mOwner;
	}

	public void setOwner(String owner) {
		this.mOwner = owner;
	}

	public long getTriggerTime() {
		return mTriggerTime;
	}

	public void setTriggerTime(long triggerTime) {
		this.mTriggerTime = triggerTime;
	}

	public int getRepeatType() {
		return mRepeatType;
	}

	public void setRepeatType(int repeatType) {
		this.mRepeatType = repeatType;
	}

	public long getRepeatInterval() {
		return mRepeatInterval;
	}

	public void setRepeatInterval(long repeatInterval) {
		this.mRepeatInterval = repeatInterval;
	}

	public long getFinishTime() {
		return mFinishTime;
	}

	public void setFinishTime(long finishTime) {
		this.mFinishTime = finishTime;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		this.mMessage = message;
	}

	public boolean isVibrate() {
		return mVibrate;
	}

	public void setVibrate(boolean vibrate) {
		this.mVibrate = vibrate;
	}

	public boolean isSound() {
		return mSound;
	}

	public void setSound(boolean sound) {
		this.mSound = sound;
	}

	public int getShowType() {
		return mShowType;
	}

	public void setShowType(int showType) {
		this.mShowType = showType;
	}

	public String getIntentTarget() {
		return mIntentTarget;
	}

	public void setIntentTarget(String intentTarget) {
		this.mIntentTarget = intentTarget;
	}

	public String getIntentAction() {
		return mIntentAction;
	}

	public void setIntentAction(String intentAction) {
		this.mIntentAction = intentAction;
	}

	public String getIntentData() {
		return mIntentData;
	}

	public void setIntentData(String intentData) {
		this.mIntentData = intentData;
	}

	public boolean isChecked() {
		return mChecked;
	}

	public void setChecked(boolean checked) {
		this.mChecked = checked;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setEventId(String id) {
		mEventId = id;
	}
	
	public String getEventId() {
		return mEventId;
	}
	
	public boolean isOutOfDate() {
		return mRepeatType == WarnManager.REPEAT_TYPE_NONE ? (System.currentTimeMillis() > mTriggerTime) : 
			(mFinishTime > mTriggerTime && System.currentTimeMillis() > mFinishTime);
	}
	
	public boolean isRepeated() {
	    return mRepeatType != WarnManager.REPEAT_TYPE_NONE;
	}
	
	public boolean isForever () {
		return mRepeatType != WarnManager.REPEAT_TYPE_NONE && mRepeatInterval > 0 && 
				mFinishTime <= mTriggerTime;
	}
	
	//calculate next alarm time from now, don't consider finish time
	public long getNextAlarmTime() {
		long now = System.currentTimeMillis();
		if (now < mTriggerTime) {
		    return mTriggerTime;
		}
		Calendar triggerCalendar;
		Calendar nowCalendar;
		long interval = mRepeatInterval;
		switch (mRepeatType) {
		case WarnManager.REPEAT_TYPE_NONE:
			return mTriggerTime;
		case WarnManager.REPEAT_TYPE_MILLISECOND:
		    long milisecondInterval = (now - mTriggerTime)/interval;
		    return mTriggerTime + (milisecondInterval + 1) * interval;
		case WarnManager.REPEAT_TYPE_DAY:
			interval = mRepeatInterval*24*3600*1000;
			long dayInterval = (now - mTriggerTime)/interval;
			return mTriggerTime + (dayInterval+1)*interval;
		case WarnManager.REPEAT_TYPE_WEEK:
			interval = mRepeatInterval*7*24*3600*1000;
			long weekInterval = (now - mTriggerTime)/interval;
			return mTriggerTime + (weekInterval+1)*interval;
		case WarnManager.REPEAT_TYPE_MONTH:
			triggerCalendar = Calendar.getInstance();
			triggerCalendar.setTimeInMillis(mTriggerTime);
			nowCalendar = Calendar.getInstance();
			nowCalendar.setTimeInMillis(now);
			int monthDistance =  nowCalendar.get(Calendar.MONTH) - triggerCalendar.get(Calendar.MONTH)
			                    +(nowCalendar.get(Calendar.YEAR) - triggerCalendar.get(Calendar.YEAR))*12;
			int monthInterval = monthDistance/(int)interval;
			triggerCalendar.add(Calendar.MONTH, monthInterval*(int)interval);
			if (triggerCalendar.before(nowCalendar)) {
			    triggerCalendar.add(Calendar.MONTH, (int)interval);
			} else {
			    triggerCalendar.add(Calendar.MONTH, 0-(int)interval);
			}
			return triggerCalendar.getTimeInMillis();
		case WarnManager.REPEAT_TYPE_YEAR:
		    triggerCalendar = Calendar.getInstance();
            triggerCalendar.setTimeInMillis(mTriggerTime);
            nowCalendar = Calendar.getInstance();
            nowCalendar.setTimeInMillis(now);
            int yearDistance = nowCalendar.get(Calendar.YEAR) - triggerCalendar.get(Calendar.YEAR);
            int yearInterval = yearDistance/(int)interval;
            triggerCalendar.add(Calendar.YEAR, yearInterval*(int)interval);
            if (triggerCalendar.before(nowCalendar)) {
                triggerCalendar.add(Calendar.YEAR, (int)interval);
            } else {
                triggerCalendar.add(Calendar.YEAR, 0-(int)interval);
            }
            return triggerCalendar.getTimeInMillis();
		}
		return mTriggerTime;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(mID);
		dest.writeString(mOwner);
		dest.writeLong(mTriggerTime);
		dest.writeInt(mRepeatType);
		dest.writeLong(mRepeatInterval);
		dest.writeLong(mFinishTime);
		dest.writeString(mMessage);
		dest.writeString(Boolean.toString(mVibrate));
		dest.writeString(Boolean.toString(mSound));
		dest.writeInt(mShowType);
		dest.writeString(mIntentTarget);
		dest.writeString(mIntentAction);
		dest.writeString(mIntentData);
		dest.writeString(Boolean.toString(mChecked));
		dest.writeString(mTitle);
		dest.writeString(mEventId);
	}
	
	public static final Parcelable.Creator<Warn> CREATOR = new Parcelable.Creator<Warn>() {
		public Warn createFromParcel(Parcel in) {
		    return new Warn(in);
		}

		public Warn[] newArray(int size) {
		    return new Warn[size];
		}
	};

	private Warn(Parcel in) {
        mID = in.readInt();
        mOwner = in.readString();
        mTriggerTime = in.readLong();
        mRepeatType = in.readInt();
        mRepeatInterval = in.readLong();
        mFinishTime = in.readLong();
        mMessage = in.readString();
        mVibrate = Boolean.valueOf(in.readString());
        mSound = Boolean.valueOf(in.readString());
        mShowType = in.readInt();
        mIntentTarget = in.readString();
        mIntentAction = in.readString();
        mIntentData = in.readString();
        mChecked = Boolean.valueOf(in.readString());
        mTitle = in.readString();
        mEventId = in.readString();
    }


}
