package com.android.proxy.warn;

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

public class WarnProvider extends ContentProvider {
    
    private static final String TAG = "WarnProvider";
    private static final boolean DEBUG = true;
    
    public static final String PROVIDER_NAME = "com.android.proxy.warn";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/warns");
    
    public static final String _ID = "_id";
    public static final String OWNER = "owner";
    public static final String TRIGGER_TIME = "trigger";
    public static final String REPEAT_TYPE = "repeat";
    public static final String REPEAT_INTERVAL_TIME = "interval";
    public static final String FINISH_TIME = "finish";
    public static final String MESSAGE = "message";
    public static final String VIBRATE = "vibrate";
    public static final String SOUND = "sound";
    public static final String SHOW_TYPE = "show_type";
    public static final String INTENT_TARGET = "intent_target";
    public static final String INTENT_ACTION = "intent_action";
    public static final String INTENT_DATA = "intent_data";
    public static final String CHECKED = "checked";
    
    private static final int WARNS = 1;
    private static final int WARN_ID = 2;
    private static UriMatcher uriMatcher;
    
    static{  
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);  
        uriMatcher.addURI(PROVIDER_NAME, "warns", WARNS);  
        uriMatcher.addURI(PROVIDER_NAME, "warns/#", WARN_ID);  
    }   
    
    private SQLiteDatabase mWarnsDB;
    private static final String DATABASE_NAME = "WarnDB";
    private static final String DATABASE_TABLE = "warns";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + _ID 
                                  + " integer primary key autoincrement, " + OWNER + " text, " + TRIGGER_TIME 
                                  + " long, " + REPEAT_TYPE + " integer, " + REPEAT_INTERVAL_TIME + " integer, "
                                  + FINISH_TIME + " long, " + MESSAGE + " text, " + VIBRATE + " boolean, "
                                  + SOUND + " boolean, " + SHOW_TYPE + " integer, " + INTENT_TARGET + " text, "
                                  + INTENT_ACTION + " text, " + INTENT_DATA + " text, " + CHECKED + " boolean);";
    
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
           case WARNS:
              count = mWarnsDB.delete(DATABASE_TABLE, selection, selectionArgs);
              break;

           case WARN_ID:
              String id = uri.getPathSegments().get(1);
              count = mWarnsDB.delete(DATABASE_TABLE, _ID + " = " + id 
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
        case WARNS:
           return "vnd.android.cursor.dir/warn";
        case WARN_ID:
           return "vnd.android.cursor.item/warn";
        default:
           throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = mWarnsDB.insert(DATABASE_TABLE, "", values);           

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
        mWarnsDB = dbHelper.getWritableDatabase();  
        return (mWarnsDB == null)? false:true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(DATABASE_TABLE);
        if (uriMatcher.match(uri) == WARN_ID) {
            sqlBuilder.appendWhere(_ID + " = " + uri.getPathSegments().get(1));
        }
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = OWNER;
        }
        Cursor c = sqlBuilder.query(mWarnsDB, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
           case WARNS:
              count = mWarnsDB.update(DATABASE_TABLE, values, selection, selectionArgs);
              break;

           case WARN_ID: 
              count = mWarnsDB.update(DATABASE_TABLE, values, _ID + " = " + uri.getPathSegments().get(1) 
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
