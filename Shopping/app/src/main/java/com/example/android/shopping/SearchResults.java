package com.example.android.shopping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jonathan on 7/4/2017.
 */

class SearchResults extends AppCompatActivity{
    
    //holds search results
    // TODO: 7/11/2017 make it so that buttons for previous and next page work. 
    private JSONObject jsonObject = null;
    private  static  final String LOG_TAG = "SearchResults";
    private List<String> item_names = new LinkedList<>();
    private List<String> item_id = new LinkedList<>();
    private List<String> catalog_number = new LinkedList<>();
    private List<String> prices = new LinkedList<>();
    private List<String> companies = new LinkedList<>();
    private List<String> types = new LinkedList<>();
    private int pageNumber;
    private String searchString = null;
    Context context = this;

    int arrayLength = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(LOG_TAG, "created");
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String jsonString = intent.getExtras().getString("jsonString");
        searchString =intent.getExtras().getString("searchString");
        String page = intent.getExtras().getString("searchPage");
        Log.d(LOG_TAG, "page: " + page);
        pageNumber = Integer.parseInt(page);

        arrayLength = 0;
        Log.v(LOG_TAG, "jsonString:  " + jsonString);

        try {
            jsonObject = new JSONObject(jsonString);
            arrayLength = jsonObject.getJSONArray("data").length();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int i = 0;
        while (i < arrayLength){
            JSONParser(i);
            i++;
        }
        if(arrayLength == 0){
            Toast.makeText(context, "No results, either go back a page or search something else", Toast.LENGTH_LONG).show();
        }
        setContentView(R.layout.search_results);
        Button nextPage = (Button) findViewById(R.id.bt_nextPage);
        Button previousPage = (Button)findViewById(R.id.bt_previousPage);

        ListView listView = (ListView) findViewById(R.id.lv_searchResults);

        Log.v(LOG_TAG, "item list: " + item_id.toString());

        Log.v(LOG_TAG, "Rigght beforeAdapter");
        final SearchResultsAdapter adapter = new SearchResultsAdapter(item_names, item_id, catalog_number, prices, companies, types, context);
        listView.setAdapter(adapter);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber ++;
                quartzySearch();
            }

        });
        previousPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pageNumber <= 1){
                    Toast.makeText(context,"... This is the first page of the results, their isn't a previous page", Toast.LENGTH_LONG).show();
                }else if (pageNumber >=1){
                    pageNumber --;
                    quartzySearch();
                }
            }
        });
    }

    SearchResults() {


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
            e.printStackTrace();
        }
    }
    void quartzySearch(){
        JSONObject searchObject = new JSONObject();
        try{
            searchObject.put("request_type", "search");
            searchObject.put("search_string", searchString);
            searchObject.put("page", String.valueOf(pageNumber));
        }catch (Exception e){
            e.printStackTrace();
        }
        new QuartzyHandler(context).execute(searchObject);
    }

}
