package com.android.testapp;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ViewWarnActivity extends ListActivity {
    
	private static final String TAG = "ViewWarnActivity";
	private static final boolean DEBUG = true;
	
    private WarnAdapter mWarnAdapter;
    private String[] projection = {"_id", "owner", "trigger", "message", "vibrate"};
    private static final int CT_MENU_EDIT = 1;
    private static final int CT_MENU_DELETE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Cursor cursor = managedQuery(Uri.parse("content://com.android.proxy.warn/warns"), 
    			projection, "owner = '" + getPackageName() + "'", null, null);
        mWarnAdapter = new WarnAdapter(getApplicationContext(), cursor);
        setContentView(R.layout.view);
        getListView().setAdapter(mWarnAdapter);
        registerForContextMenu(getListView());
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
			textView.setTag(cursor.getInt(0));
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = getLayoutInflater().inflate(R.layout.viewlist_item, null);
			return v;
		}
        
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        // TODO Auto-generated method stub
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CT_MENU_EDIT, 0, "EDIT");
        menu.add(0, CT_MENU_DELETE, 0, "DELETE");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int warnID = (Integer)info.targetView.getTag();
        LOGD("onContextItemSelected:" + warnID);
        switch (item.getItemId()) {
        case CT_MENU_EDIT:
            editWarn(warnID);
            break;
        case CT_MENU_DELETE:
            deleteWarn(warnID);
            break;
        }
        return super.onContextItemSelected(item);
    }
    
    private void deleteWarn(int id) {
        Uri deleteUri = ContentUris.withAppendedId(Uri.parse("content://com.android.proxy.warn/warns"), id);
        int deleteCount = getContentResolver().delete(deleteUri, null, null);
        LOGD("delete:" + deleteCount);
        mWarnAdapter.notifyDataSetChanged();
    }
    
    private void editWarn(int id) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.putExtra(EditWarnActivity.WARN_ID, id);
        intent.setClass(getApplicationContext(), EditWarnActivity.class);
        startActivity(intent);
    }
    
    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}
