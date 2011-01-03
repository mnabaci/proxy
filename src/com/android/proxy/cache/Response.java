package com.android.proxy.cache;

import android.os.Parcel;
import android.os.Parcelable;

public class Response implements Parcelable {
	
	private static final int CAPACITY = 100;
	
	public int resultCode;
	public int[] objectIds = new int[CAPACITY];
	
	public Response() {
		
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.writeInt(resultCode);
		arg0.writeIntArray(objectIds);
	}
	
	public void readFromParcel(Parcel in) {
		resultCode = in.readInt();
		in.readIntArray(objectIds);
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
