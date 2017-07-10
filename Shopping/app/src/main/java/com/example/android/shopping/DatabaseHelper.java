package com.example.android.shopping;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

// TODO: 7/9/2017 merge two databases
public class DatabaseHelper extends SQLiteOpenHelper {
    // database helper for use with list of "commonly" ordered items in Dillin Lab

    private static String DB_NAME = "info.db";
    private static String DB_PATH = "";
    private static final int DB_VERSION = 11;
    private final String LOG_TAG = "DatabaseHelper";

    private SQLiteDatabase mDataBase;
    private final Context mContext;
    private boolean mNeedUpdate = false;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;

        copyDataBase();

        this.getReadableDatabase();
    }

    public void updateDataBase() throws IOException {
        if (mNeedUpdate) {
            File dbFile = new File(DB_PATH + DB_NAME);
            if (dbFile.exists())
                dbFile.delete();

            copyDataBase();

            mNeedUpdate = false;
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException {
        //InputStream mInput = mContext.getAssets().open(DB_NAME);
        InputStream mInput = mContext.getResources().openRawResource(R.raw.try_this);
        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    public boolean openDataBase() throws SQLException {
        mDataBase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            mNeedUpdate = true;
    }

    public String [] getFirstColumn() {
        List<String> returnValues = new LinkedList<String>();
        int counter = 0;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        this.checkDataBase();

        Cursor cursor = sqLiteDatabase.query("SHOPPING_LIST", null, null, null, null, null, null);
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
    public String getURLFromItemname(String itemName) {
        String returnValue ;
        String item = itemName;
        String query = "SELECT * FROM SHOPPING_LIST WHERE ITEMS = '" + item + "'";
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        returnValue = cursor.getString(3);
        sqLiteDatabase.close();
        cursor.close();
        return returnValue;
    }

    public int getOrderAmount(String item){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        int returnValue;
        String query = "SELECT * FROM SHOPPING_LIST WHERE ITEMS = '" + item + "'";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        returnValue = cursor.getInt(9);
        sqLiteDatabase.close();
        cursor.close();
        return returnValue;
    }
    public void setOrderAmount (String item, int i){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ORDER_AMOUNT", Integer.valueOf(i));
        sqLiteDatabase.update("SHOPPING_LIST", contentValues, "ITEMS = '" + item + "'", null);


    }
    public void clearList(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ORDER_AMOUNT", 0);
        sqLiteDatabase.update("SHOPPING_LIST", contentValues, null, null);


    }
    public void orderItems(){
        // method to order items.  Calls QuartzyHandler to do it.
        List<String> orderValues = new LinkedList<String>();
        int counter = 0;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        this.checkDataBase();
        JSONObject[] orderObjects = {};

        Cursor cursor = sqLiteDatabase.query("SHOPPING_LIST", null, null, null, null, null, null);
        cursor.moveToFirst();
        while (counter < cursor.getCount()) {
            if(cursor.getInt(9)>0) {
                JSONObject orderobject = new JSONObject();
                try{

                    orderobject.put  ("request_type", "order");
                    orderobject.put ("item_name", cursor.getString(5));
                    orderobject.put ("quantity", cursor.getInt(9));
                    orderobject.put ("item_id", cursor.getString(3));
                    orderobject.put ("catalog_number", cursor.getString(7));
                    orderobject.put ("price", cursor.getString(8));
                    orderobject.put ("company", cursor.getString(10));
                    orderobject.put ("type", cursor.getString(11));

                }catch (Exception e){
                    Log.v(LOG_TAG, e.toString());
                }
                orderObjects[counter]= orderobject;

            counter++;
            cursor.moveToNext();
        }
        cursor.close();
        new QuartzyHandler(mContext).execute(orderObjects);
        clearList();
        sqLiteDatabase.close();
    }

}}