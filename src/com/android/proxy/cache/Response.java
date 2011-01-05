package com.android.proxy.cache;

import android.os.Parcel;
import android.os.Parcelable;

public class Response implements Parcelable {
	
	public static final int ASYNC_RESPONSE = -1;
	
	public String packageName;
	public int requestId;
	public long time;
	public String body;
	
	public Response() {
		
	}
	
	public void reset() {
		packageName = null;
		requestId = ASYNC_RESPONSE;
		time = 0;
		body = null;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.writeString(packageName);
		arg0.writeInt(requestId);
		arg0.writeLong(time);
		arg0.writeString(body);
	}
	
	public void readFromParcel(Parcel in) {
		packageName = in.readString();
		requestId = in.readInt();
		time = in.readLong();
		body = in.readString();
	}

	
	public static final Parcelable.Creator<Response> CREATOR = new Parcelable.Creator<Response>() {
		public Response createFromParcel(Parcel in) {
		    return new Response(in);
		}

		public Response[] newArray(int size) {
		    return new Response[size];
		}
	};

	private Response(Parcel in) {
        readFromParcel(in);
    }
	
}
