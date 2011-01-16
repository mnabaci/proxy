package com.android.proxy.ui;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.proxy.R;
import com.android.proxy.cache.RequestProvider;
import com.android.proxy.cache.ResponseProvider;

public class ResponseActivity extends ListActivity {
	
	private ResponseAdapter mResponseAdapter;
    private String[] projection = {"_id", ResponseProvider.OWNER, ResponseProvider.BODY, 
    		ResponseProvider.REQUEST_ID, ResponseProvider.TIME};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view);
		Cursor cursor = managedQuery(ResponseProvider.CONTENT_URI, projection, 
				null, null, null);
		mResponseAdapter = new ResponseAdapter(getApplicationContext(), cursor);
		getListView().setAdapter(mResponseAdapter);
	}
	
	private class ResponseAdapter extends CursorAdapter {

        public ResponseAdapter(Context context, Cursor c) {
			super(context, c);
		}

		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			TextView textView = (TextView)view;
			textView.setText(cursor.getInt(0) + ":" + cursor.getString(1) + 
					":request_id:" + cursor.getInt(3));
			textView.setTag(cursor.getInt(0));
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = getLayoutInflater().inflate(R.layout.viewlist_item, null);
			return v;
		}
        
    }


}
