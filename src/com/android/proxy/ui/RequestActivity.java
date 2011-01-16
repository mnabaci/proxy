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

public class RequestActivity extends ListActivity {
	
	private RequestAdapter mRequestAdapter;
    private String[] projection = {"_id", RequestProvider.OWNER, RequestProvider.ACTION, 
    		RequestProvider.ITEMS, RequestProvider.BODY, RequestProvider.RESPONSE};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view);
		Cursor cursor = managedQuery(RequestProvider.CONTENT_URI, projection, 
				null, null, null);
		mRequestAdapter = new RequestAdapter(getApplicationContext(), cursor);
		getListView().setAdapter(mRequestAdapter);
	}
	
	private class RequestAdapter extends CursorAdapter {

        public RequestAdapter(Context context, Cursor c) {
			super(context, c);
		}

		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			TextView textView = (TextView)view;
			textView.setText(cursor.getInt(0) + ":" + cursor.getInt(2) + ":" + cursor.getString(3) + 
					":response:" + cursor.getInt(5));
			textView.setTag(cursor.getInt(0));
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = getLayoutInflater().inflate(R.layout.viewlist_item, null);
			return v;
		}
        
    }


}
