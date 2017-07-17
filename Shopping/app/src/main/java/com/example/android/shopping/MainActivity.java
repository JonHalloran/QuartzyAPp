package com.example.android.shopping;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by Jonathan on 7/1/2017.
 */
// TODO: 7/9/2017 Favorites
    //general class for interacting with Quartzy
class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // check if login exists
        SharedPreferences sharedPref =  getSharedPreferences("myprefs", 0);
        String login = sharedPref.getString("Login", "");
        String password = sharedPref.getString("Password", "");
        Log.v(LOG_TAG, login + password);
        //forced to login if not given
        if (login.equals("") || password.equals("")) {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt_goShoppingList = (Button) findViewById(R.id.bt_goShoppingList);
        bt_goShoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShoppingList.class);
                startActivity(intent);
            }
        });

        Button bt_goLogin = (Button) findViewById(R.id.bt_goLogin);
        bt_goLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Login.class);
                startActivity(intent);
            }
        });

        //this button was supposed to be for favorites but as that isn't implemented yet got switched over
        Button bt_goMyFavs = (Button) findViewById(R.id.bt_goMyFavs);
        bt_goMyFavs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SignIn.class);
                startActivity(intent);
            }
        });
        Button bt_goFindItem = (Button) findViewById(R.id.bt_goFindItem);
        bt_goFindItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, QuartzySearch.class);
                startActivity(intent);

            }
        });
    }
}
