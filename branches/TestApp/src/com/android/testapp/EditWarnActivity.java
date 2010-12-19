package com.android.testapp;

import java.io.UTFDataFormatException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

public class EditWarnActivity extends Activity {
    
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
        values.put("intent_action", Intent.ACTION_VIEW);
        values.put("intent_data", "smsto:");
        getContentResolver().insert(Uri.parse("content://com.android.proxy.warn/warns"), values);
    }

}
