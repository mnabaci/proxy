package com.android.proxy.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.proxy.Config;
import com.android.proxy.ProxyService;
import com.android.proxy.R;
import com.android.proxy.UpgradeService;
import com.android.proxy.cache.Request;
import com.android.proxy.internet.RequestHandler;
import com.android.proxy.internet.XMLResponse;
import com.android.proxy.utils.DeviceInfo;
import com.android.proxy.utils.Utils;

public class LoginActivity extends Activity{
    private static final String TAG = "LoginActivity";
    private static final boolean DEBUG = true;
    
    private static final int DIALOG_NETWORK_DISABLE = 1;
    private static final int DIALOG_LOGIN_FAULT = 2;
        
    private DialogInterface.OnClickListener mNULLClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
        }
    };
    
    private static final int DIALOG_SHOW = 0;
    private static final int LOGIN_FINISH = 1;
    private static final int INIT_DIALOG = 2;
    private static final int DISMISS_DIALOG = 3;
    private static final int SHOW_WARNING_DIALOG = 4;
    
    private AlertDialog mTipsDialog;
    private TextView mTipTextView;
    private String username;
    private String password;
    
    private EditText mEditUserName;
    private EditText mEditPassword;
    private ToggleButton mRememberPWDButton;
    
    private RequestHandler mHandler;
    private Request mRequest;
    private XMLResponse mXMLResponse;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        resetViewComponents();
        mHandler = new RequestHandler(Config.getInstance(getApplicationContext()).getCloudUrl());
        mRequest = new Request(); 
    }
    
    private void resetViewComponents() {
    	getWindow().requestFeature(Window.FEATURE_NO_TITLE); 
    	getWindow().setBackgroundDrawableResource(R.drawable.login_bg);
        setContentView(R.layout.login);
        
        mEditUserName = (EditText)findViewById(R.id.log_username);
        mEditPassword = (EditText)findViewById(R.id.log_password);
        mRememberPWDButton = (ToggleButton)findViewById(R.id.remember_pwd);
        
        Config config = Config.getInstance(getApplicationContext());
        boolean remember = config.isRememberPwd();
        mRememberPWDButton.setChecked(remember);
        username = config.getUserId();
        if (remember) {
        	password = config.getPassword();
        }
        
        mEditUserName.setText(username);
        mEditPassword.setText(password);
        mEditUserName.requestFocus();
        
        Button btnLogin = (Button)findViewById(R.id.btn_login);
        Button btnRegister = (Button)findViewById(R.id.btn_register);
        Button btnCancel = (Button)findViewById(R.id.btn_log_cancel);
        
        btnLogin.setOnClickListener(new OnClickListener(){
            public void onClick(View arg0) {
                processLogin();
            }
        });
        btnRegister.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent, 0);
            }
            
        });
        btnCancel.setOnClickListener(new OnClickListener(){
            public void onClick(View arg0) {
                finish();
            }
        });
        mRememberPWDButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				Config.getInstance(getApplicationContext()).setRememberPwd(isChecked);
			}
		});
    }
    
    private void processLogin() {
        username = mEditUserName.getText().toString();
        password = mEditPassword.getText().toString();
        if(!checkInput(username, password)) {
            return;
        }
        String result = postLoginRequest();
        LOGD("login result:" + result);
        if (TextUtils.isEmpty(result)) {
        	showDialog(DIALOG_NETWORK_DISABLE);
        	return;
        }
        XMLResponse xmlInfo = RequestHandler.parseXMLResult(getApplicationContext(), result);
        mXMLResponse = xmlInfo;
        if (xmlInfo.resultCode.equals(RequestHandler.SUCCESSFUL_RESULT_CODE)) {
        	Config.getInstance(getApplicationContext()).setSessionId(xmlInfo.sessionId);
        	Config.getInstance(getApplicationContext()).setUserId(username);
        	if (mRememberPWDButton.isChecked()) {
        		Config.getInstance(getApplicationContext()).setPassword(password);
        	} else {
        		Config.getInstance(getApplicationContext()).setPassword("");
        	}
        	postRequestInCache();
        	upgradeProxy();
    		finish();
        } else {
        	Toast.makeText(getApplicationContext(), 
    				xmlInfo.errorDescription , Toast.LENGTH_LONG).show();
        	showDialog(DIALOG_LOGIN_FAULT);
        }
    }
    
    private void postRequestInCache() {
    	Intent i = new Intent();
		i.setClass(getApplicationContext(), ProxyService.class);
		i.putExtra(ProxyService.PROXY_INTENT_TYPE, ProxyService.PROXY_INTENT_TYPE_POSTREQUESTS);
		startService(i);
    }
    
    private void upgradeProxy() {
    	Intent i = new Intent();
		i.setClass(getApplicationContext(), UpgradeService.class);
		i.setAction(UpgradeService.ACTION);
		startService(i);
    }
    
    private boolean checkInput(String username, String password) {
        return true;
    }
    
    private boolean checkUsername(String username) {
        if(username == null) return false;
        return true;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        username = mEditUserName.getText().toString();
        password = mEditPassword.getText().toString();
//        resetViewComponents();
    }
    
    private String postLoginRequest() {
    	Config config = Config.getInstance(getApplicationContext());
    	mRequest.action = Request.ACTION_GET;
    	mRequest.items = "USERINFO";
    	mRequest.versionId = "-1";
    	mRequest.body = "<?xml version='1.0' encoding='utf-8'?><OBJECTS><OBJECT>"
    		+ "<PWD>" + Utils.Encrypt(mEditPassword.getText().toString()) + "</PWD>" 
    		+ "<DEVICEPIM>" + config.getFlatId() + "</DEVICEPIM>"
    		+ "<COMMPIM>" + DeviceInfo.getInstance(getApplicationContext()).getIMSI() + "</COMMPIM>"
    		+ "</OBJECT></OBJECTS>";
    	return mHandler.handleRequest(username, config.getFlatId(), null, mRequest);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LOGD("[[onActivityResult]] resultCode = " + resultCode);
        setResult(resultCode);
        finish();
    }
    
    @Override
    public void onUserLeaveHint() {
        Log.d(TAG, "[[onUserLeaveHint]]");
    }
    
    private void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		mTipTextView = (TextView)getLayoutInflater().inflate(R.layout.dialog_view, null);
		mTipTextView.setBackgroundDrawable(null);
		dialog.setView(mTipTextView);
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		// TODO Auto-generated method stub
		super.onPrepareDialog(id, dialog);
		switch(id) {
		case DIALOG_NETWORK_DISABLE:
			dialog.getWindow().setBackgroundDrawableResource(R.drawable.network_disable);
			dialog.setCanceledOnTouchOutside(true);
			mTipTextView.setText(getString(R.string.txt_netword_disable));
			break;
		case DIALOG_LOGIN_FAULT:
			dialog.getWindow().setBackgroundDrawableResource(R.drawable.login_fault);
			dialog.setCanceledOnTouchOutside(true);
			mTipTextView.setText(mXMLResponse.errorDescription);
			break;
		}
	}
}
