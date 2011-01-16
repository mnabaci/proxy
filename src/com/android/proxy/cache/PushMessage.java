package com.android.proxy.cache;

import java.sql.Date;

import android.util.Log;

public class PushMessage {
	
	public String pushMessageId;
	public String sender;
	public String sendTime;
	public String overTime;
	public String startTime;
	public String subject;
	public String content;
	public int type;
	public int cuteType;
	public String remindType;
	
	public void print() {
		Log.d("PushMessage", "pushMessageId=" + pushMessageId + "; sender=" + sender + ";sendTime="
				+ sendTime + "; overTime=" + overTime
				+ ";startTime=" + startTime + ";subject=" + subject 
				+ "; content=" + content + "; type=" + type + "; cuteType=" + cuteType 
				+ "; remindType=" + remindType);
	}

}
