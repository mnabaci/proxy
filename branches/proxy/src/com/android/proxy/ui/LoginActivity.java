package com.android.proxy.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.proxy.Config;
import com.android.proxy.ProxyService;
import com.android.proxy.R;
import com.android.proxy.cache.Request;
import com.android.proxy.internet.RequestHandler;
import com.android.proxy.internet.XMLResponse;
import com.android.proxy.utils.DeviceInfo;
import com.android.proxy.utils.Utils;

public class LoginActivity extends Activity{
    private static final String TAG = "LoginActivity";
    private static final boolean DEBUG = true;
        
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
    private String username;
    private String password;
    
    private EditText mEditUserName;
    private EditText mEditPassword;
    
    private RequestHandler mHandler;
    private Request mRequest;
    
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
    }
    
    private void processLogin() {
        username = mEditUserName.getText().toString();
        password = mEditPassword.getText().toString();
        if(!checkInput(username, password)) {
            return;
        }
        String result = postLoginRequest();
        LOGD("login result:" + result);
        XMLResponse xmlInfo = RequestHandler.parseXMLResult(getApplicationContext(), result);
        if (xmlInfo.resultCode.equals(RequestHandler.SUCCESSFUL_RESULT_CODE)) {
        	Config.getInstance(getApplicationContext()).setSessionId(xmlInfo.sessionId);
        	Config.getInstance(getApplicationContext()).setUserId(username);
        	postRequestInCache();
    		finish();
        } else {
        	Toast.makeText(getApplicationContext(), 
    				xmlInfo.errorDescription , Toast.LENGTH_LONG).show();
        }
    }
    
    private void postRequestInCache() {
    	Intent i = new Intent();
		i.setClass(getApplicationContext(), ProxyService.class);
		i.putExtra(ProxyService.PROXY_INTENT_TYPE, ProxyService.PROXY_INTENT_TYPE_POSTREQUESTS);
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
}
