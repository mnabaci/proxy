package com.android.proxy.ui;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.proxy.R;
import com.android.proxy.cache.MessageProvider;

public class MessageActivity extends ListActivity {
	
	private MessageAdapter mMessageAdapter;
    private String[] projection = {"_id", MessageProvider.MSG_ID, MessageProvider.STARTTIME, MessageProvider.SUBJECT};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view);
		Cursor cursor = managedQuery(MessageProvider.CONTENT_URI, projection, 
				null, null, null);
		mMessageAdapter = new MessageAdapter(getApplicationContext(), cursor);
		getListView().setAdapter(mMessageAdapter);
	}
	
	private class MessageAdapter extends CursorAdapter {

        public MessageAdapter(Context context, Cursor c) {
			super(context, c);
		}

		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			TextView textView = (TextView)view;
			textView.setText(cursor.getInt(0) + ":" + cursor.getString(1) + ":" + cursor.getString(2));
			textView.setTag(cursor.getInt(0));
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = getLayoutInflater().inflate(R.layout.viewlist_item, null);
			return v;
		}
        
    }

}
