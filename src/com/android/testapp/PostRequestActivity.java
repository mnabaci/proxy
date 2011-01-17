package com.android.testapp;

import com.android.proxy.cache.Request;
import com.android.proxy.cache.Response;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class PostRequestActivity extends Activity {

	private Spinner mSpinner;
	private EditText mIDEdit;
	private EditText mMobileEdit;
	private Button mConfirmButton;
	private Button mCancelButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.postrequest);
		mSpinner = (Spinner)findViewById(R.id.action_type);
		mIDEdit = (EditText)findViewById(R.id.contact_id);
		mMobileEdit = (EditText)findViewById(R.id.mobile);
		mConfirmButton = (Button)findViewById(R.id.btn_confirm);
		mCancelButton = (Button)findViewById(R.id.btn_cancel);
		if (mConfirmButton != null) {
			mConfirmButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					postRequest();
				}
			});
		}
		if (mCancelButton != null) {
			mCancelButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			});
		}
	}
	
	private void postRequest() {
		int action = mSpinner.getSelectedItemPosition();
		switch (action) {
		case 0:
			queryRecord();
			break;
		case 1:
			postRecord();
			break;
		case 2:
			deleteRecord();
			break;
		case 3:
			updateRecord();
			break;
		case 4:
			getResponse();
			break;
		}
	}
	
	private void getResponse() {
		int id = 1;
		try {
			id = Integer.parseInt(mIDEdit.getText().toString());
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "id must be integer", Toast.LENGTH_LONG).show();
			return;
		}
		try {
			Response response = TestActivity.mService.getResponse(id, getPackageName());
			showDialog(response.body);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void postRecord() {
    	Request request = new Request();
    	request.action = Request.ACTION_POST;
    	request.items = "CONTACES";
    	request.versionId = "-1";
    	request.packageName = getPackageName();
    	request.body = "<?xml version='1.0' encoding='utf-8'?><OBJECTS><OBJECT><CONTACESID>" 
    		+ mIDEdit.getText().toString() +
    		"</CONTACESID><WHETHEREXTEND>1</WHETHEREXTEND><FAMILYNAME>江</FAMILYNAME><USERNAME>test" +
    		"</USERNAME><NICKNAME>jenny</NICKNAME><HEADPORTRAIT>pic1.jpg</HEADPORTRAIT><COMPANYNAME>" +
    		"company</COMPANYNAME><DEPARTMENT>department</DEPARTMENT><ADDRESS>address</ADDRESS>" +
    		"<TELEPHONEEXCHANGE>85711780</TELEPHONEEXCHANGE><EXTENSION>8301</EXTENSION><FAX></FAX>" +
    		"<MOBILEPHONE1>" + mMobileEdit.getText().toString() + "</MOBILEPHONE1><MOBILEPHONE2></MOBILEPHONE2><TELEPHONE>" +
    		"0739-8811332</TELEPHONE><EMAIL>xiaoliang.jiang@eaglelink.cn</EMAIL>" +
    		"</OBJECT></OBJECTS>";
    	if (TestActivity.mIsBinding && TestActivity.mService != null) {
    		try {
				Response response = TestActivity.mService.postRequest(request);
//				Toast.makeText(getApplicationContext(), response.body, Toast.LENGTH_LONG).show();
				showDialog(response.body);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    private void deleteRecord() {
    	Request request = new Request();
    	request.action = Request.ACTION_DELETE;
    	request.items = "CONTACES";
    	request.versionId = "-1";
    	request.packageName = getPackageName();
    	request.body = "<?xml version='1.0' encoding='utf-8'?><OBJECTS><OBJECT><CONTACESID>"+ mIDEdit.getText().toString() +
    		"</CONTACESID></OBJECT></OBJECTS>";
    	if (TestActivity.mIsBinding && TestActivity.mService != null) {
    		try {
				Response response = TestActivity.mService.postRequest(request);
//				Toast.makeText(getApplicationContext(), response.body, Toast.LENGTH_LONG).show();
				showDialog(response.body);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    private void queryRecord() {
    	Request request = new Request();
    	request.action = Request.ACTION_GET;
    	request.items = "CONTACES";
    	request.versionId = "-1";
    	request.packageName = getPackageName();
    	request.body = "<?xml version='1.0' encoding='utf-8'?><OBJECTS><OBJECT><CONTACESID>"
    		+ mIDEdit.getText().toString() +"</CONTACESID></OBJECT></OBJECTS>";
    	if (TestActivity.mIsBinding && TestActivity.mService != null) {
    		try {
				Response response = TestActivity.mService.postRequest(request);
//				Toast.makeText(getApplicationContext(), response.body, Toast.LENGTH_LONG).show();
				showDialog(response.body);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    private void updateRecord() {
    	Request request = new Request();
    	request.action = Request.ACTION_PUT;
    	request.items = "CONTACES";
    	request.versionId = "-1";
    	request.packageName = getPackageName();
    	request.body = "<?xml version='1.0' encoding='utf-8'?><OBJECTS><OBJECT><CONTACESID>" 
    		+ mIDEdit.getText().toString() +
		"</CONTACESID><WHETHEREXTEND>1</WHETHEREXTEND><FAMILYNAME>Fang</FAMILYNAME><USERNAME>test" +
		"</USERNAME><NICKNAME>kevin</NICKNAME><HEADPORTRAIT>pic1.jpg</HEADPORTRAIT><COMPANYNAME>" +
		"company</COMPANYNAME><DEPARTMENT>department</DEPARTMENT><ADDRESS>address</ADDRESS>" +
		"<TELEPHONEEXCHANGE>85711780</TELEPHONEEXCHANGE><EXTENSION>8301</EXTENSION><FAX></FAX>" +
		"<MOBILEPHONE1>" + mMobileEdit.getText().toString() + "</MOBILEPHONE1><MOBILEPHONE2></MOBILEPHONE2><TELEPHONE>" +
		"0739-8811332</TELEPHONE><EMAIL>xiaoliang.jiang@eaglelink.cn</EMAIL>" +
		"</OBJECT></OBJECTS>";
    	if (TestActivity.mIsBinding && TestActivity.mService != null) {
    		try {
				Response response = TestActivity.mService.postRequest(request);
//				Toast.makeText(getApplicationContext(), response.body, Toast.LENGTH_LONG).show();
				showDialog(response.body);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    private void showDialog(String message) {
    	new AlertDialog.Builder(this)
    	.setMessage(message)
    	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		})
		.create().show();
    }

}
