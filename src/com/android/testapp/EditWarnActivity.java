package com.android.testapp;

import java.io.UTFDataFormatException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

public class EditWarnActivity extends Activity {
    
    public static final String WARN_ID = "warn_id";
    
    private DatePicker mTriggerDatePicker;
    private TimePicker mTriggerTimePicker;
    private DatePicker mFinishDatePicker;
    private TimePicker mFinishTimePicker;
    private Spinner mRepeatTypeSpinner;
    private EditText mRepeatIntervalEdit;
    private EditText mMessageEdit;
    private Spinner mShowTypeSpinner;
    private Button mCommitButton;
    private Button mCancelButton;
    private static final String[] PROJECTION = {"_id", "owner", "trigger", "repeat", "interval", "finish", "message",
        "vibrate", "sound", "show_type", "intent_target", "intent_action", "intent_data", "checked"};
    
    private int mWarnID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        mTriggerDatePicker = (DatePicker)findViewById(R.id.trigger_date);
        mTriggerTimePicker = (TimePicker)findViewById(R.id.trigger_time);
        mFinishDatePicker = (DatePicker)findViewById(R.id.finish_date);
        mFinishTimePicker = (TimePicker)findViewById(R.id.finish_time);
        mRepeatTypeSpinner = (Spinner)findViewById(R.id.repeat_type);
        mRepeatIntervalEdit = (EditText)findViewById(R.id.interval_time);
        mMessageEdit = (EditText)findViewById(R.id.message);
        mShowTypeSpinner = (Spinner)findViewById(R.id.show_type);
        mCommitButton = (Button)findViewById(R.id.commit);
        mCancelButton = (Button)findViewById(R.id.cancel);
        
        if (getIntent().getAction().equals(Intent.ACTION_EDIT)) {
            mWarnID = getIntent().getIntExtra(WARN_ID, -1);
            if (mWarnID != -1) {
                Uri uri = ContentUris.withAppendedId(Uri.parse("content://com.android.proxy.warn/warns"), mWarnID);
                Cursor cursor = getContentResolver().query(uri, PROJECTION, null, null, null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(cursor.getLong(2));
                    mTriggerDatePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 
                            calendar.get(Calendar.DAY_OF_MONTH), null);
                    mTriggerTimePicker.setIs24HourView(false);
                    mTriggerTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
                    mTriggerTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
                    mRepeatTypeSpinner.setSelection(cursor.getInt(3));
                    mRepeatIntervalEdit.setText(String.valueOf(cursor.getInt(4)));
                    calendar.setTimeInMillis(cursor.getLong(5));
                    mFinishDatePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 
                    		calendar.get(Calendar.DAY_OF_MONTH), null);
                    mFinishTimePicker.setIs24HourView(false);
                    mFinishTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
                    mFinishTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
                    mMessageEdit.setText(cursor.getString(6));
                    mShowTypeSpinner.setSelection(cursor.getInt(9));
                }
                cursor.close();
            }
        }
        
        mCommitButton.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                insertRecord();
                finish();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    private void insertRecord() {
        ContentValues values = new ContentValues();
        values.put("owner", getPackageName());
        Calendar calendar = new GregorianCalendar(mTriggerDatePicker.getYear(), mTriggerDatePicker.getMonth()
                , mTriggerDatePicker.getDayOfMonth(), mTriggerTimePicker.getCurrentHour(), 
                mTriggerTimePicker.getCurrentMinute(), 0);
        values.put("trigger", calendar.getTime().getTime());
        values.put("repeat", mRepeatTypeSpinner.getSelectedItemPosition());
        values.put("interval", Integer.parseInt(mRepeatIntervalEdit.getText().toString()));
        calendar.set(mFinishDatePicker.getYear(), mFinishDatePicker.getMonth(), mFinishDatePicker.getDayOfMonth(), 
                mFinishTimePicker.getCurrentHour(), mFinishTimePicker.getCurrentMinute(), 0);
        values.put("finish", calendar.getTime().getTime());
        values.put("message", mMessageEdit.getText().toString());
        values.put("vibrate", false);
        values.put("sound", false);
        values.put("show_type", mShowTypeSpinner.getSelectedItemPosition());
        values.put("intent_target", "com.android.mms/.ui.ConversationList");
        values.put("intent_action", Intent.ACTION_VIEW);
        values.put("intent_data", "smsto:");
        values.put("checked", false);
        Uri uri = Uri.parse("content://com.android.proxy.warn/warns");
        if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
        	getContentResolver().insert(uri, values);
        } else if (getIntent().getAction().equals(Intent.ACTION_EDIT)) {
        	Uri queryUri = ContentUris.withAppendedId(uri, mWarnID);
        	getContentResolver().update(queryUri, values, null, null);
        }
    }

}
