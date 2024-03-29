package com.android.proxy.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.proxy.Config;
import com.android.proxy.ProxyService;
import com.android.proxy.R;
import com.android.proxy.cache.Request;
import com.android.proxy.internet.RequestHandler;
import com.android.proxy.internet.XMLResponse;
import com.android.proxy.utils.DeviceInfo;
import com.android.proxy.utils.Utils;

public class RegisterActivity extends Activity {
    private static final String TAG = "RegisterActivity";
    private static final boolean DEBUG = true;
    
    private static final int DIALOG_NETWORK_DISABLE = 1;
    private static final int DIALOG_REGISTER_FAULT = 2;
    
    private DialogInterface.OnClickListener mNULLClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
        }
    };
    
    private static final int DIALOG_SHOW = 0;
    private static final int REGISTER_FINISH = 1;
    private static final int INIT_DIALOG = 2;
    private static final int DISMISS_DIALOG = 6;
    private static final int SHOW_WARNING_DIALOG = 7;
    
    private static final int CHECK_INPUT_SUCCESS = 3;
    private static final int CHECK_DIFF_PASSWORD = 4;
    private static final int CHECK_ILLEGAL_INPUT = 5;
    
    private AlertDialog mTipsDialog;
    private TextView mTipTextView;
    private EditText mEditUserName;
    private EditText mEditPassword;
    private EditText mEditPassword2;
    private EditText mEditPhone;
    private Spinner mSpinner;
//    private EditText mEditLastName;
//    private EditText mEditFirstName;
    
    private String mUserName;
    private String mPassword;
    private String mPassword2;
    private String mPhoneNumber;
    private String mEmailBox;
    private Toast mToast;
    private String mLastName;
    private String mFirstName;
    
    private RequestHandler mHandler;
    private Request mRequest;
    private XMLResponse mXMLResponse;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(this, R.string.txt_illegal_username_password, Toast.LENGTH_LONG);
        resetViewComponents();
        mHandler = new RequestHandler(Config.getInstance(getApplicationContext()).getCloudUrl());
        mRequest = new Request(); 
    }
    
    private void resetViewComponents() {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	getWindow().setBackgroundDrawableResource(R.drawable.bg);
        setContentView(R.layout.register);
        
        mEditUserName = (EditText)findViewById(R.id.reg_username);
        mEditPassword = (EditText)findViewById(R.id.reg_password);
        mEditPassword2 = (EditText)findViewById(R.id.reg_password2);
        mEditPhone = (EditText)findViewById(R.id.reg_phone);
//        mEditLastNamse = (EditText)findViewById(R.id.last_name);
//        mEditFirstName = (EditText)findViewById(R.id.first_name);
        mEditUserName.setText(mUserName);
        mEditPassword.setText(mPassword);
        mEditPassword2.setText(mPassword2);
        mEditPhone.setText(mPhoneNumber);
        mEditUserName.requestFocus();
//        mEditLastName.setText(mLastName);
//        mEditFirstName.setText(mFirstName);
        
        Button btnRegister = (Button)findViewById(R.id.btn_register);
        Button btnCancel = (Button)findViewById(R.id.btn_cancel);
        btnRegister.setOnClickListener(new OnClickListener(){

            public void onClick(View arg0) {
                mUserName = mEditUserName.getText().toString();
                mPassword = mEditPassword.getText().toString();
                mPassword2 = mEditPassword2.getText().toString();
                mPhoneNumber = mEditPhone.getText().toString();
//                mLastName = mEditLastName.getText().toString();
//                mFirstName = mEditFirstName.getText().toString();
                int checkResult = checkInput(mUserName, mPassword, mPassword2);
                if(checkResult == CHECK_ILLEGAL_INPUT) {
                    mToast.cancel();
                    mToast.setText(R.string.txt_illegal_username_password);
                    mToast.show();
                    return;
                } else if (checkResult == CHECK_DIFF_PASSWORD) {
                    mToast.cancel();
                    mToast.setText(R.string.txt_diff_password);
                    mToast.show();
                    return;
                } else {
                	String result = postRegisterRequest();
                	LOGD("result:" + result);
                	if (TextUtils.isEmpty(result)) {
                    	showDialog(DIALOG_NETWORK_DISABLE);
                    	return;
                    }
                	XMLResponse xmlInfo = RequestHandler.parseXMLResult(getApplicationContext(),result);
                	mXMLResponse = xmlInfo;
                	if (xmlInfo.resultCode.equals(RequestHandler.SUCCESSFUL_RESULT_CODE)) {
                		Config.getInstance(getApplicationContext()).setSessionId(xmlInfo.sessionId);
                		Config.getInstance(getApplicationContext()).setUserId(mUserName);
                		postRequestInCache();
                		finish();
                	} else {
                		showDialog(DIALOG_REGISTER_FAULT);
                		Toast.makeText(getApplicationContext(), 
                				xmlInfo.errorDescription , Toast.LENGTH_LONG).show();
                	}
                }
            }   
        }); 
        btnCancel.setOnClickListener(new OnClickListener(){
            public void onClick(View arg0) {
                finish();
            }
            
        });
    }
    
    private void postRequestInCache() {
    	Intent i = new Intent();
		i.setClass(getApplicationContext(), ProxyService.class);
		i.putExtra(ProxyService.PROXY_INTENT_TYPE, ProxyService.PROXY_INTENT_TYPE_POSTREQUESTS);
		startService(i);
    }
    
    private String postRegisterRequest() {
    	Config config = Config.getInstance(getApplicationContext());
    	mRequest.action = Request.ACTION_POST;
    	mRequest.items = "USERINFO";
    	mRequest.versionId = "-1";
    	mRequest.body = "<?xml version='1.0' encoding='utf-8'?><OBJECTS><OBJECT>"
    		+ "<PWD>" + mEditPassword.getText().toString() + "</PWD>" 
    		+ "<DEVICEPIM>" + config.getFlatId() + "</DEVICEPIM>"
    		+ "<COMMPIM>" + DeviceInfo.getInstance(getApplicationContext()).getIMSI() + "</COMMPIM>"
//    		+ "<USERLNAME>" + mEditLastName.getText().toString() + "</USERLNAME>"
//    		+ "<USERFNAME>" + mEditFirstName.getText().toString() + "</USERFNAME>"
    		+ "<MPHONE>" + mEditPhone.getText().toString() + "</MPHONE>"
    		+ "</OBJECT></OBJECTS>";
    	LOGD("request body:" + mRequest.body);
    	return mHandler.handleRequest(mUserName, config.getFlatId(), null, mRequest);
    }
    
    private int checkInput(String username, String password, String password2) {
        if(username == null || username.equals("") || username.trim().equals("")) {
            return CHECK_ILLEGAL_INPUT;
        }
        if(password == null || password.equals("") || password.trim().equals("")) {
            return CHECK_ILLEGAL_INPUT;
        }
        if(password2 == null || password.equals("") || password.trim().equals("")) {
            return CHECK_ILLEGAL_INPUT;
        }
        if(!password.equals(password2)) {
            return CHECK_DIFF_PASSWORD;
        }
        if(!checkUsername(username)) {
            return CHECK_ILLEGAL_INPUT;
        }
        if(!checkPassword(password)) {
            return CHECK_ILLEGAL_INPUT;
        }
        return CHECK_INPUT_SUCCESS;
    }
    
    private boolean checkUsername(String username) {
        if(username == null) return false;
        if(username.length() < 4 || username.length() > 16) return false;
        for(int i = 0; i < username.length(); i++) {
            if(i == 0 && !Character.isLowerCase(username.charAt(i))) {
                return false;
            }
            if(!Character.isLowerCase(username.charAt(i)) && !Character.isDigit(username.charAt(i)) && 
                    (username.charAt(i) != '_') && (username.charAt(i) != '-' &&
                    (username.charAt(i) != '.' ))) {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkPassword(String password) {
        if (password == null) {
            return false;
        }
        if (password.length() < 6 || password.length() > 16) {
            return false;
        }
        for(int i = 0; i < password.length(); i++) {
            if (!Character.isLetterOrDigit(password.codePointAt(i)) && password.charAt(i) != '-' &&
                    password.charAt(i) != '_') {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LOGD("onConfigurationChanged");
        mUserName = mEditUserName.getText().toString();
        mPassword = mEditPassword.getText().toString();
        mPassword2 = mEditPassword2.getText().toString();
        mPhoneNumber = mEditPhone.getText().toString();
//        mLastName = mEditLastName.getText().toString();
//        mFirstName = mEditFirstName.getText().toString();
//        resetViewComponents();
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
		case DIALOG_REGISTER_FAULT:
			dialog.getWindow().setBackgroundDrawableResource(R.drawable.login_fault);
			dialog.setCanceledOnTouchOutside(true);
			mTipTextView.setText(mXMLResponse.errorDescription);
			break;
		}
	}
    
    private void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }
}

