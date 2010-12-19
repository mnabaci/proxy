package com.android.testapp;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ViewWarnActivity extends ListActivity {
    
    private WarnAdapter mWarnAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mWarnAdapter = new WarnAdapter();
        setContentView(R.layout.view);
        getListView().setAdapter(mWarnAdapter);
    }
    
    private void retrieveAllWarns() {
        
    }
    
    private class WarnAdapter extends BaseAdapter {

        public int getCount() {
            // TODO Auto-generated method stub
            return 3;
        }

        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.viewlist_item, null);
            }
            TextView textView = (TextView)convertView;
            textView.setText("text");
            return textView;
        }
        
    }

}
