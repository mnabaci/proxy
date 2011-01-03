package com.android.proxy.cache;

import android.os.Parcel;
import android.os.Parcelable;

public class Request implements Parcelable {
	
	public int action;
	
	public Request() {
		
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.writeInt(action);
	}
	
	public void readFromParcel(Parcel in) {
		action = in.readInt();
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
