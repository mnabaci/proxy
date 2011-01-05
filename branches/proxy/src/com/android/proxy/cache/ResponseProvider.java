package com.android.proxy.cache;

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

public class ResponseProvider extends ContentProvider {
	
	private static final String TAG = "ResponseProvider";
    private static final boolean DEBUG = true;
    
    public static final String PROVIDER_NAME = "com.android.proxy.cache.response";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/responses");
    
    public static final String _ID = "_id";
    public static final String OWNER = "owner";
    public static final String BODY = "body";
    public static final String TIME = "time";
    public static final String REQUEST_ID = "request_id";
    
    private static final int RESPONSES = 1;
    private static final int RESPONSE_ID = 2;
    private static UriMatcher uriMatcher;
    
    static{  
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);  
        uriMatcher.addURI(PROVIDER_NAME, "responses", RESPONSES);  
        uriMatcher.addURI(PROVIDER_NAME, "responses/#", RESPONSE_ID);  
    }   
    
    private SQLiteDatabase mResponseDB;
    private static final String DATABASE_NAME = "ResponseDB";
    private static final String DATABASE_TABLE = "responses";
    private static final int DATABASE_VERSION = 1;
    
    private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + _ID 
    								+ " integer primary key autoincrement, " + OWNER + " text, "
    								+ BODY + " text, " + TIME + " long," + REQUEST_ID + " integer);";
    
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
           case RESPONSES:
              count = mResponseDB.delete(DATABASE_TABLE, selection, selectionArgs);
              break;

           case RESPONSE_ID:
              String id = uri.getPathSegments().get(1);
              count = mResponseDB.delete(DATABASE_TABLE, _ID + " = " + id 
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
        case RESPONSES:
           return "vnd.android.cursor.dir/response";
        case RESPONSE_ID:
           return "vnd.android.cursor.item/response";
        default:
           throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowID = mResponseDB.insert(DATABASE_TABLE, "", values);           

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
        mResponseDB = dbHelper.getWritableDatabase();  
        return (mResponseDB == null)? false:true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(DATABASE_TABLE);
        if (uriMatcher.match(uri) == RESPONSE_ID) {
            sqlBuilder.appendWhere(_ID + " = " + uri.getPathSegments().get(1));
        }
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = TIME;
        }
        Cursor c = sqlBuilder.query(mResponseDB, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
        switch (uriMatcher.match(uri)){
           case RESPONSES:
              count = mResponseDB.update(DATABASE_TABLE, values, selection, selectionArgs);
              break;

           case RESPONSE_ID: 
              count = mResponseDB.update(DATABASE_TABLE, values, _ID + " = " + uri.getPathSegments().get(1) 
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
