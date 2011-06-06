package com.android.proxy.cache;

import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MessageProvider extends ContentProvider {
	
	private static final String TAG = "PushMessageProvider";
    private static final boolean DEBUG = true;
    
    public static final String PROVIDER_NAME = "com.android.proxy.cache.message";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/messages");
    
    public static final String _ID = "_id";
    public static final String MSG_ID = "msg_id";
    public static final String SENDER = "sender";
	public static final String SENDTIME = "sendtime";
	public static final String OVERTIME = "overtime";
	public static final String STARTTIME = "starttime";
	public static final String SUBJECT = "subject";
	public static final String CONTENT = "content";
	public static final String TYPE = "type";
	public static final String CUTETYPE = "cutetype";
	public static final String REMINDTYPE = "remindtype";
	public static final String SUMMARY = "summary";
	public static final String REPEAT = "repeat";
	public static final String INTERVAL = "interval";
	
	private static final int MESSAGES = 1;
    private static final int MESSAGE_ID = 2;
    private static UriMatcher uriMatcher;
    
    static{  
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);  
        uriMatcher.addURI(PROVIDER_NAME, "messages", MESSAGES);  
        uriMatcher.addURI(PROVIDER_NAME, "messages/#", MESSAGE_ID);  
    } 
    
    private SQLiteDatabase mMessagesDB;
    private static final String DATABASE_NAME = "MessageDB";
    private static final String DATABASE_TABLE = "messages";
    private static final int DATABASE_VERSION = 1;
    
    private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + _ID 
    								+ " integer primary key autoincrement, " + MSG_ID + " text, "
    								+ SENDER + " text, " + SENDTIME + " text, " + OVERTIME + " text, "
    								+ STARTTIME + " text, " + SUBJECT + " text," + CONTENT + " text, "
    								+ SUMMARY + " text, " + REPEAT + " text," + INTERVAL + " text, "
    								+ TYPE + " integer, " + CUTETYPE + " integer, " 
    								+ REMINDTYPE + " text);";
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name,
                CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
        
        DatabaseHelper(Context context) {  
            super(context, DATABASE_NAME, null, DATABASE_VERSION);  
        }  

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + DATABASE_TABLE);
            onCreate(db);
        }
        
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count=0;
        switch (uriMatcher.match(uri)){
           case MESSAGES:
              count = mMessagesDB.delete(DATABASE_TABLE, selection, selectionArgs);
              break;

           case MESSAGE_ID:
              String id = uri.getPathSegments().get(1);
              count = mMessagesDB.delete(DATABASE_TABLE, _ID + " = " + id 
                      + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
              break;

           default: throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)){
        case MESSAGES:
           return "vnd.android.cursor.dir/message";
        case MESSAGE_ID:
           return "vnd.android.cursor.item/message";
        default:
           throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowID = mMessagesDB.insert(DATABASE_TABLE, "", values);           

        //---if added successfully---
        if (rowID>0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri,null);
            return _uri;
        }

        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		LOGD("onCreate");
        Context context = getContext();  
        DatabaseHelper dbHelper = new DatabaseHelper(context);  
        mMessagesDB = dbHelper.getWritableDatabase();
        try {
			Runtime.getRuntime().exec("chmod 664 /data/data/com.android.proxy/databases/MessageDB");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return (mMessagesDB == null)? false:true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(DATABASE_TABLE);
        if (uriMatcher.match(uri) == MESSAGE_ID) {
            sqlBuilder.appendWhere(_ID + " = " + uri.getPathSegments().get(1));
        }
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = null;
        }
        Cursor c = sqlBuilder.query(mMessagesDB, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
        switch (uriMatcher.match(uri)){
           case MESSAGES:
              count = mMessagesDB.update(DATABASE_TABLE, values, selection, selectionArgs);
              break;

           case MESSAGE_ID: 
              count = mMessagesDB.update(DATABASE_TABLE, values, _ID + " = " + uri.getPathSegments().get(1) 
                      + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
              break;
           default: throw new IllegalArgumentException("Unknown URI " + uri); 

        } 
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	
	private static void LOGD(String text) {
        if(DEBUG) {
            Log.d(TAG, text);
        }
    }

}
