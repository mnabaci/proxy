package com.android.proxy.warn;

import android.os.Parcel;
import android.os.Parcelable;

public class Warn implements Parcelable {
	
	private int mID;
	private String mOwner;
	private long mTriggerTime;
	private int mRepeatType;
	private int mRepeatInterval;
	private long mFinishTime;
	private String mMessage;
	private boolean mVibrate;
	private boolean mSound;
	private int mShowType;
	private String mIntentTarget;
	private String mIntentAction;
	private String mIntentData;
	private boolean mChecked;
	
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

	public int getRepeatInterval() {
		return mRepeatInterval;
	}

	public void setRepeatInterval(int repeatInterval) {
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
		dest.writeInt(mRepeatInterval);
		dest.writeLong(mFinishTime);
		dest.writeString(mMessage);
		dest.writeString(Boolean.toString(mVibrate));
		dest.writeString(Boolean.toString(mSound));
		dest.writeInt(mShowType);
		dest.writeString(mIntentTarget);
		dest.writeString(mIntentAction);
		dest.writeString(mIntentData);
		dest.writeString(Boolean.toString(mChecked));
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
        mRepeatInterval = in.readInt();
        mFinishTime = in.readLong();
        mMessage = in.readString();
        mVibrate = Boolean.valueOf(in.readString());
        mSound = Boolean.valueOf(in.readString());
        mShowType = in.readInt();
        mIntentTarget = in.readString();
        mIntentAction = in.readString();
        mIntentData = in.readString();
        mChecked = Boolean.valueOf(in.readString());
    }


}
