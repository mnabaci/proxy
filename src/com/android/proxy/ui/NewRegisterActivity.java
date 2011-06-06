package com.android.proxy.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.proxy.Config;
import com.android.proxy.R;
import com.android.proxy.cache.Request;
import com.android.proxy.internet.RequestHandler;
import com.android.proxy.internet.XMLResponse;
import com.android.proxy.utils.DeviceInfo;

public class NewRegisterActivity extends Activity {
	
	private boolean mIsFirst = false;
	
	private EditText mEmailText;
	private Button mRegisterButton;
	private Button mCancelButton;
	
	private RequestHandler mHandler;
    private Request mRequest;
    private XMLResponse mXMLResponse;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mHandler = new RequestHandler(Config.getInstance(getApplicationContext()).getCloudUrl());
        mRequest = new Request();
        setRegisterUI(true);
        
	}
	
	private void setRegisterUI(boolean isFirst) {
		if (mIsFirst == isFirst) return;
		mIsFirst = isFirst;
		if (isFirst) {
			setContentView(R.layout.newregister);
		} else {
			setContentView(R.layout.wrong_address);
		}
		mEmailText = (EditText)findViewById(R.id.email);
        mRegisterButton = (Button)findViewById(R.id.btn_register);
        mCancelButton = (Button)findViewById(R.id.btn_cancel);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!checkUserId(mEmailText.getText().toString())) {
					Toast.makeText(getApplicationContext(), "wrong user id", Toast.LENGTH_LONG).show();
					return;
				}
				String result = postRegisterRequest();
				Log.d("NewRegisterActivy", "result:" + result);
				if (!TextUtils.isEmpty(result)) {
					XMLResponse xmlInfo = RequestHandler.parseXMLResult(getApplicationContext(),result);
					if (xmlInfo.resultCode.equals(RequestHandler.SUCCESSFUL_RESULT_CODE)) {
						Config.getInstance(getApplicationContext()).setUserId(mEmailText.getText().toString());
                		finish();
                		Intent intent = new Intent(Intent.ACTION_MAIN);
	    				intent.setClass(getApplicationContext(), ActivateActivity.class);
	    				startActivity(intent);
                	} else {
                		setRegisterUI(false);
                	}
				} else {
					Toast.makeText(getApplicationContext(), "Internet error", Toast.LENGTH_LONG).show();
				}	
			}
		});
        mCancelButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	private boolean checkUserId(String userId) {
		return true;
	}
	
	private String postRegisterRequest() {
    	Config config = Config.getInstance(getApplicationContext());
    	mRequest.action = Request.ACTION_POST;
    	mRequest.items = "USERS";
    	mRequest.versionId = "-1";
    	mRequest.body = "";
    	return mHandler.handleRequest(mEmailText.getText().toString(), config.getFlatId(), config.ANONOYMOUS_SESSIONID, mRequest);
    }

}
