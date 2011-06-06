package com.android.proxy.ui;

import com.android.proxy.R;
import com.android.proxy.cache.Request;
import com.android.proxy.internet.RequestHandler;

import android.app.Activity;
import android.os.Bundle;

public class ResetPasswordActivity extends Activity {
	
	private RequestHandler mHandler;
    private Request mRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resetpwd);
	}
	
	private void resetPassword() {
		
	}

}
