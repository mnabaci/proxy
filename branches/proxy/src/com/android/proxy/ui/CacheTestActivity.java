package com.android.proxy.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.proxy.R;
import com.android.proxy.cache.Response;

public class CacheTestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cachetest);
		Button requestButton = (Button)findViewById(R.id.viewRequest);
		Button responseButton = (Button)findViewById(R.id.viewResponse);
		Button messageButton = (Button)findViewById(R.id.viewMessages);
		if (requestButton != null) {
			requestButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.setClass(getApplicationContext(), RequestActivity.class);
					startActivity(intent);
				}
			});
		}
		if (responseButton != null) {
			responseButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.setClass(getApplicationContext(), ResponseActivity.class);
					startActivity(intent);
				}
			});
		}
		if (messageButton != null) {
			messageButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.setClass(getApplicationContext(), MessageActivity.class);
					startActivity(intent);
				}
			});
		}
	}

}
