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

public class RequestProvider extends ContentProvider {
	
	private static final String TAG = "RequestProvider";
    private static final boolean DEBUG = true;
    
    public static final String PROVIDER_NAME = "com.android.proxy.cache.request";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/requests");
    
    public static final String _ID = "_id";
    public static final String OWNER = "owner";
    public static final String ACTION = "action";
    public static final String TYPE = "type";
    public static final String USER = "user";
    public static final String OBJECT = "object";
    public static final String BODY = "body";
    public static final String TIME = "time";
    public static final String RETURNED = "return";
    
    private static final int REQUESTS = 1;
    private static final int REQUEST_ID = 2;
    private static UriMatcher uriMatcher;
    
    static{  
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);  
        uriMatcher.addURI(PROVIDER_NAME, "requests", REQUESTS);  
        uriMatcher.addURI(PROVIDER_NAME, "requests/#", REQUEST_ID);  
    }   
    
    private SQLiteDatabase mRequestsDB;
    private static final String DATABASE_NAME = "RequestDB";
    private static final String DATABASE_TABLE = "requests";
    private static final int DATABASE_VERSION = 1;
    
    private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + _ID 
    								+ " integer primary key autoincrement, " + OWNER + " text, "
    								+ ACTION + " integer, " + TYPE + " text, " + USER + " text, "
    								+ OBJECT + " text, " + BODY + " text, " + TIME + " long,"
    								+ RETURNED + " boolean);";
    
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
           case REQUESTS:
              count = mRequestsDB.delete(DATABASE_TABLE, selection, selectionArgs);
              break;

           case REQUEST_ID:
              String id = uri.getPathSegments().get(1);
              count = mRequestsDB.delete(DATABASE_TABLE, _ID + " = " + id 
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
        case REQUESTS:
           return "vnd.android.cursor.dir/request";
        case REQUEST_ID:
           return "vnd.android.cursor.item/request";
        default:
           throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowID = mRequestsDB.insert(DATABASE_TABLE, "", values);           

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
        mRequestsDB = dbHelper.getWritableDatabase();  
        return (mRequestsDB == null)? false:true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(DATABASE_TABLE);
        if (uriMatcher.match(uri) == REQUEST_ID) {
            sqlBuilder.appendWhere(_ID + " = " + uri.getPathSegments().get(1));
        }
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = TIME;
        }
        Cursor c = sqlBuilder.query(mRequestsDB, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
        switch (uriMatcher.match(uri)){
           case REQUESTS:
              count = mRequestsDB.update(DATABASE_TABLE, values, selection, selectionArgs);
              break;

           case REQUEST_ID: 
              count = mRequestsDB.update(DATABASE_TABLE, values, _ID + " = " + uri.getPathSegments().get(1) 
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
