package com.android.proxy.ui;

import com.android.proxy.Config;
import com.android.proxy.ProxyService;
import com.android.proxy.R;
import com.android.proxy.cache.Request;
import com.android.proxy.internet.RequestHandler;
import com.android.proxy.internet.XMLResponse;
import com.android.proxy.utils.DeviceInfo;
import com.android.proxy.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class NewLoginActivity extends Activity {
	
	public static final int MODE_LOGIN = 1;
	public static final int MODE_WRONG_PWD = 2;
	public static final int MODE_RESET_PWD = 3;
	
	public static final String KEY_MODE = "MODE";
	
	private int mMode;
	
	private EditText mEmailEdit;
	private EditText mPasswordEdit;
	private CheckBox mRememberedCheckBox;
	private Button mUpdateButton;
	private Button mResetPwdButton;
	
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
        int mode = getIntent().getIntExtra(KEY_MODE, MODE_LOGIN);
        setLoginMode(mode);
	}
	
	private void setLoginMode(int mode) {
		mMode = mode;
		switch (mode) {
		case MODE_LOGIN:
			setContentView(R.layout.new_login);
			break;
		case MODE_WRONG_PWD:
			setContentView(R.layout.wrongpwd);
			break;
		case MODE_RESET_PWD:
			setContentView(R.layout.resetpwd);
			resetPassword();
			break;
		}
		mEmailEdit = (EditText)findViewById(R.id.email);
		if (Config.getInstance(getApplicationContext()).isRegistered()) {
//			mEmailEdit.setEnabled(false);
		} else {
			mEmailEdit.setEnabled(true);
		}
		mEmailEdit.setText(Config.getInstance(getApplicationContext()).getUserId());
		mPasswordEdit = (EditText)findViewById(R.id.password);
		mRememberedCheckBox = (CheckBox)findViewById(R.id.remember);
		boolean rememberPwd = Config.getInstance(getApplicationContext()).isRememberPwd();
		mRememberedCheckBox.setChecked(rememberPwd);
		if (rememberPwd) {
			mPasswordEdit.setText(Config.getInstance(getApplicationContext()).getPassword());
		}
		mUpdateButton = (Button)findViewById(R.id.btn_update);
		mResetPwdButton = (Button)findViewById(R.id.btn_resetpwd);
		mUpdateButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String result = postLoginRequest();
				Log.d("NewLoginActivy", "result:" + result);
				if (TextUtils.isEmpty(result)) {
					Toast.makeText(getApplicationContext(), "Internet error", Toast.LENGTH_LONG).show();
					return;
				} else {
					XMLResponse xmlInfo = RequestHandler.parseXMLResult(getApplicationContext(), result);
			        mXMLResponse = xmlInfo;
			        if (xmlInfo.resultCode.equals(RequestHandler.SUCCESSFUL_RESULT_CODE)) {
			        	Config config = Config.getInstance(getApplicationContext());
			        	if (mRememberedCheckBox.isChecked()) {
			        		config.setPassword(mPasswordEdit.getText().toString());
			        		config.setRememberPwd(true);
			        	} else {
			        		config.setPassword("");
			        		config.setRememberPwd(false);
			        	}
			        	config.setUserId(mEmailEdit.getText().toString());
			        	config.setSessionId(xmlInfo.sessionId);
			        	postRequestInCache();
			        	finish();
			        } else {
			        	setLoginMode(MODE_WRONG_PWD);
			        }
				}
			}
		});
		mResetPwdButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mMode != MODE_RESET_PWD) {
					setLoginMode(MODE_RESET_PWD);
				} else {
					resetPassword();
				}
			}
		});
	}
	
	private void resetPassword() {
		Config config = Config.getInstance(getApplicationContext());
    	mRequest.action = Request.ACTION_DELETE;
    	mRequest.items = "USERS";
    	mRequest.versionId = "-1";
    	mRequest.body = "";
    	String result = mHandler.handleRequest(config.getUserId(), config.getFlatId(), null, mRequest);
    	Log.d("NewLoginActivy", "reset pwd result:" + result);
	}
	
	private String postLoginRequest() {
		Log.d("NewLoginActivity", "pwd:" + mPasswordEdit.getText().toString());
		Config config = Config.getInstance(getApplicationContext());
    	mRequest.action = Request.ACTION_GET;
    	mRequest.items = "USERINFO";
    	mRequest.versionId = "-1";
    	mRequest.body = "<?xml version='1.0' encoding='utf-8'?><OBJECTS><OBJECT>"
    		+ "<PWD>" + Utils.Encrypt(mPasswordEdit.getText().toString()) + "</PWD>" 
    		+ "<DEVICEPIM>" + config.getFlatId() + "</DEVICEPIM>"
    		+ "<COMMPIM>" + DeviceInfo.getInstance(getApplicationContext()).getIMSI() + "</COMMPIM>"
    		+ "</OBJECT></OBJECTS>";
    	Log.d("NewLoginActiviy", "login:" + mRequest.body);
    	return mHandler.handleRequest(mEmailEdit.getText().toString(), config.getFlatId(), null, mRequest);
	}
	
	private void postRequestInCache() {
    	Intent i = new Intent();
		i.setClass(getApplicationContext(), ProxyService.class);
		i.putExtra(ProxyService.PROXY_INTENT_TYPE, ProxyService.PROXY_INTENT_TYPE_POSTREQUESTS);
		startService(i);
    }

}
