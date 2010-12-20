package com.android.testapp;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ViewWarnActivity extends ListActivity {
    
	private static final String TAG = "ViewWarnActivity";
	
	
    private WarnAdapter mWarnAdapter;
    private String[] projection = {"_id", "owner", "trigger", "message", "vibrate"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Cursor cursor = managedQuery(Uri.parse("content://com.android.proxy.warn/warns"), 
    			projection, "owner = '" + getPackageName() + "'", null, null);
        mWarnAdapter = new WarnAdapter(getApplicationContext(), cursor);
        setContentView(R.layout.view);
        getListView().setAdapter(mWarnAdapter);
    }
    
    private class WarnAdapter extends CursorAdapter {

        public WarnAdapter(Context context, Cursor c) {
			super(context, c);
		}

		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			TextView textView = (TextView)view;
			textView.setText(cursor.getInt(0) + ":" + cursor.getString(3) 
					+ "&" + Boolean.valueOf(cursor.getString(4)));
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = getLayoutInflater().inflate(R.layout.viewlist_item, null);
			return v;
		}
        
    }

}
