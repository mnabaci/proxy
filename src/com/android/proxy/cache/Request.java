package com.android.proxy.cache;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Request implements Parcelable {
	
	public static final int ACTION_GET = 1;
	public static final int ACTION_POST = 2;
	public static final int ACTION_DELETE = 3;
	public static final int ACTION_PUT = 4;
	
	public int action;
	public String packageName;
	public String items;
	public String versionId;
	public String body;
	
	//for proxy use, not for client apps
	public int cacheId;
	
	public Request() {

	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.writeInt(action);
		arg0.writeString(packageName);
		arg0.writeString(items);
		arg0.writeString(versionId);
		arg0.writeString(body);
	}
	
	public void readFromParcel(Parcel in) {
		action = in.readInt();
		packageName = in.readString();
		items = in.readString();
		versionId = in.readString();
		body = in.readString();
		Log.d("Request", "before read list");
	}

	
	public static final Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>() {
		public Request createFromParcel(Parcel in) {
		    return new Request(in);
		}

		public Request[] newArray(int size) {
		    return new Request[size];
		}
	};

	private Request(Parcel in) {
        readFromParcel(in);
    }
	
}
