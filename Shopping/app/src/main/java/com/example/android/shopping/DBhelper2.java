package com.example.android.shopping;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jonathan on 7/1/2017.
 */

public class DBhelper2 extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FAVORITES_DB";
    public static final String TABLE_NAME = "FAVORITES_TABLE";
    // table columns
    public static final String ITEM = "item";
    public static final String URL = "URL";
    private static final String LOG_TAG = "DBhelper2";
    private static DBhelper2 sInstance;
    Context context;

    public DBhelper2(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                ITEM + " TEXT PRIMARY KEY, " +
                URL + " TEXT )";
        sqLiteDatabase.execSQL(CREATE_TABLE);

        //db.close();
    }

    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int int1, int int2) {
        // TODO: 7/1/2017
    }
    public static synchronized DBhelper2 getInstance (Context context){
        if (sInstance == null){
            sInstance = new DBhelper2(context.getApplicationContext());
        }return  sInstance;
    }
    public void addItem(String item, String itemURL){
        getInstance(context);
        SQLiteDatabase db = sInstance.getWritableDatabase();
        db.beginTransaction();
        try{
            ContentValues values = new ContentValues();
            values.put(ITEM , item);
            values.put(URL, itemURL);
            db.insertOrThrow(TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        }catch (Exception e){
            Log.d(LOG_TAG, "Failure to add to database");
        }finally {
            db.endTransaction();
        }
    }
    public String getURLFromItemname(String itemName) {
        String returnValue ;
        String item = itemName;
        String query = "SELECT * FROM "+ TABLE_NAME+ " WHERE " + ITEM + " = '" + item + "'";
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        returnValue = cursor.getString(1);
        sqLiteDatabase.close();
        cursor.close();
        return returnValue;
    }
    public String [] getFirstColumn() {
        List<String> returnValues = new LinkedList<String>();
        int counter = 0;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (counter < cursor.getCount()) {
            returnValues.add(cursor.getString(0));
            counter++;
            cursor.moveToNext();
        }
        cursor.close();
        String[] strings = returnValues.toArray(new String[returnValues.size()]);
        sqLiteDatabase.close();
        return strings;
    }
}
