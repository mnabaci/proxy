package com.android.proxy.ui;

import com.android.proxy.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class ActivateActivity extends Activity {
	
	private Button mSkipButton;
	private Button mResetPwdButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activate);
		mSkipButton = (Button)findViewById(R.id.btn_skip);
		mResetPwdButton = (Button)findViewById(R.id.btn_resetpwd);
		mSkipButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
        		Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.setClass(getApplicationContext(), NewLoginActivity.class);
				startActivity(intent);
			}
		});
		mResetPwdButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
        		Intent intent = new Intent(Intent.ACTION_MAIN);
        		intent.putExtra(NewLoginActivity.KEY_MODE, NewLoginActivity.MODE_RESET_PWD);
				intent.setClass(getApplicationContext(), NewLoginActivity.class);
				startActivity(intent);
			}
		});
	}

}
