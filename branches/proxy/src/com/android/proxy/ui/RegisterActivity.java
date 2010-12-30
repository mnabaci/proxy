package com.android.proxy.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.proxy.R;

public class RegisterActivity extends Activity {
    private static final String TAG = "RegisterActivity";
    private static final boolean DEBUG = false;
    
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
    private EditText mEditUserName;
    private EditText mEditPassword;
    private EditText mEditPassword2;
    private EditText mEditPhone;
    private Spinner mSpinner;
    
    private String mUserName;
    private String mPassword;
    private String mPassword2;
    private String mPhoneNumber;
    private String mEmailBox;
    private Toast mToast;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(this, R.string.txt_illegal_username_password, Toast.LENGTH_LONG);
        resetViewComponents();
    }
    
    private void resetViewComponents() {
        setContentView(R.layout.register);
        
        mEditUserName = (EditText)findViewById(R.id.reg_username);
        mEditPassword = (EditText)findViewById(R.id.reg_password);
        mEditPassword2 = (EditText)findViewById(R.id.reg_password2);
        mEditPhone = (EditText)findViewById(R.id.reg_phone);
        mEditUserName.setText(mUserName);
        mEditPassword.setText(mPassword);
        mEditPassword2.setText(mPassword2);
        mEditPhone.setText(mPhoneNumber);
        mEditUserName.requestFocus();
        
        Button btnRegister = (Button)findViewById(R.id.btn_register);
        Button btnCancel = (Button)findViewById(R.id.btn_cancel);
        btnRegister.setOnClickListener(new OnClickListener(){

            public void onClick(View arg0) {
                mUserName = mEditUserName.getText().toString();
                mPassword = mEditPassword.getText().toString();
                mPassword2 = mEditPassword2.getText().toString();
                mPhoneNumber = mEditPhone.getText().toString();
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
                }
                
            }   
        }); 
        btnCancel.setOnClickListener(new OnClickListener(){
            public void onClick(View arg0) {
                finish();
            }
            
        });
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
        resetViewComponents();
    }
    
    private void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }
}

