package com.example.android.shopping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

/**
 * Created by Jonathan on 7/3/2017.
 */

public class QuartzySearch extends AppCompatActivity{
    Context context = this;
    String LOG_TAG = "QuartzySearch";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quartzy_search);
        final EditText et_quartzySearchString = (EditText)findViewById(R.id.et_quartzySearchString);
        Button bt_quartzySearch = (Button) findViewById(R.id.bt_quartzySearch);
        bt_quartzySearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject searchObject = new JSONObject();
                try {
                    searchObject.put("request_type", "search");
                    searchObject.put("search_string", et_quartzySearchString.getText().toString());
                }catch (Exception e){
                    Log.v(LOG_TAG, e.toString());
                }
                new QuartzyHandler(context).execute(searchObject);
            }
        });

    }
}
