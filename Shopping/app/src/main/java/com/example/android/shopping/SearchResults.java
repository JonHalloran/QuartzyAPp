package com.example.android.shopping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Jonathan on 7/4/2017.
 */

public class SearchResults extends AppCompatActivity{
    private JSONObject jsonObject = null;
    private  static  final String LOG_TAG = "SearchResults";
    private List<String> item_names = new LinkedList<String>();
    private List<String> item_id = new LinkedList<String>();
    private List<String> catalog_number = new LinkedList<String>();
    private List<String> prices = new LinkedList<String>();
    private List<String> companies = new LinkedList<String>();
    private List<String> types = new LinkedList<String>();
    Context context = this;

    int arrayLength = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(LOG_TAG, "created");
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String jsonString = intent.getExtras().getString("jsonString");
        arrayLength = 0;

        try {
            jsonObject = new JSONObject(jsonString);
            arrayLength = jsonObject.getJSONArray("data").length();
        } catch (Exception e) {
            Log.v(LOG_TAG, e.toString());
        }
        int i = 0;
        while (i < arrayLength){
            JSONParser(i);
            i++;
        }
        setContentView(R.layout.search_results);

        Log.v(LOG_TAG, "item array: " + item_names.toArray(new String[arrayLength]) );

        ListView listView = (ListView) findViewById(R.id.lv_searchResults);

        String[] itemIdArray = new String[arrayLength];
        Log.v(LOG_TAG, "item list: " + item_id.toString());





        Log.v(LOG_TAG, "Rigght beforeAdapter");
        final SearchResultsAdapter adapter = new SearchResultsAdapter(item_names, item_id, catalog_number, prices, companies, types, context);
        listView.setAdapter(adapter);
    }

    public SearchResults() {


    }
    private void JSONParser (int i){
        try{
            JSONObject newJSONObject = jsonObject.getJSONArray("data").getJSONObject(i);
            item_id.add(newJSONObject.getString("id"));
            JSONObject attributes = newJSONObject.getJSONObject("attributes");
            item_names.add(attributes.getString("name"));
            catalog_number.add(attributes.getString("catalog_number"));
            prices.add(attributes.getString("price"));
            JSONObject relationships = newJSONObject.getJSONObject("relationships");
            companies.add(relationships.getJSONObject("company").getJSONObject("data").getString("id"));
            types.add(relationships.getJSONObject("type").getJSONObject("data").getString("id"));
        }catch (Exception e){
            Log.v(LOG_TAG,e.toString());
        }
    }


}
