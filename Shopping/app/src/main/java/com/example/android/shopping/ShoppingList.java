package com.example.android.shopping;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class ShoppingList extends AppCompatActivity {
    String LOG_TAG = "ShoppingList";

    ArrayList<String> selectedItems = new ArrayList<>(); // to be used to populate ordering list
    String [] shopping_items = {"pippette", "lb", "media", "turttles"};  //just used to initialize it
    Context context = this;

    @Override

    //// TODO: 6/17/2017  fix this shit
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseHelper mDBHelper = new DatabaseHelper(this);
        SQLiteDatabase sqLiteDatabase = mDBHelper.getWritableDatabase();
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
           SQLiteDatabase mDb = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
        try {
            shopping_items = mDBHelper.getFirstColumn();
        }catch (SQLException ex){
        }
        // layout stuff
        setContentView(R.layout.shopping_list);
        ListView listView = (ListView) findViewById(R.id.shopping_list);
        final ShoppingListAdapter adapter = new ShoppingListAdapter(shopping_items, context);
        listView.setAdapter(adapter);
        Button bt_orderItems = (Button) findViewById(R.id.bt_orderSelectedItems);
        Log.v(LOG_TAG, "button Created");
        bt_orderItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                databaseHelper.orderItems();
                String orderList= " ";
                String [] itemList = adapter.getItemList();
                int[] orderAmounts = adapter.getOrderAmount();
                Log.v(LOG_TAG, itemList[1]);
                for(int i = 0; i<itemList.length;i++ ){
                    if (orderAmounts[i]>0){
                        orderList =orderList + databaseHelper.getURLFromItemname(itemList[i]) + " x " + Integer.toString(orderAmounts[i]);
                    }
                }
                Log.v(LOG_TAG, orderList);
                Toast.makeText(context, orderList, Toast.LENGTH_LONG).show();

            }
        });
        Button bt_clearList = (Button) findViewById(R.id.bt_clearList);
        bt_clearList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                databaseHelper.clearList();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
    public void orderSelectedItems(View view){
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        String selItems="";
        for(String item:selectedItems){
            if(selItems=="")
                selItems=databaseHelper.getURLFromItemname(item);
            else
                selItems+="/"+databaseHelper.getURLFromItemname(item);
        }
        Toast.makeText(this, selItems, Toast.LENGTH_LONG).show();
        new OrderItems().execute("turtle");
    }
}
