package com.example.android.shopping;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Jonathan on 7/4/2017.
 */

public class SearchResultsAdapter extends BaseAdapter {

    private  static  final String LOG_TAG = "SearchResultsAdapter";
    List<String> item_name;
    List<String> item_id;
    List<String> catalog_number;
    List<String> price;
    List<String> company;
    List<String> type;
    Context context;

    @Override
    public int getCount() {
        return item_id.size();
    }

    @Override
    public Object getItem(int position) {
        return item_name.get(position);
    }

    @Override
    public long getItemId(int position) {
        long l = (long) position;
        return l;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Log.v(LOG_TAG, "getView");

        if (view == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.quartzy_search_item, null);
        }
        TextView itemName = (TextView) view.findViewById(R.id.tv_searchResultItemName);
        itemName.setText(item_name.get(position));
        final TextView catalogNumber = (TextView) view.findViewById(R.id.tv_searchCatalogNumber);
        catalogNumber.setText("Catalog # :" + catalog_number.get(position));
        TextView itemPrice = (TextView) view.findViewById(R.id.tv_searchResultItemPrice);
        itemPrice.setText("$" + price.get(position));

        Button button = (Button) view.findViewById(R.id.bt_SearchOrder);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject orderobject = new JSONObject();
                        try{
                            orderobject.put  ("request_type", "order");
                            orderobject.put ("item_name", item_name.get(position));
                            orderobject.put ("quantity", "1");
                            orderobject.put ("item_id", item_id.get(position));
                            orderobject.put ("catalog_number", catalog_number.get(position));
                            orderobject.put ("price", price.get(position));
                            orderobject.put ("company", company.get(position));
                            orderobject.put ("type", type.get(position));

                        }catch (Exception e){
                            Log.v(LOG_TAG, e.toString());
                        }

                new QuartzyHandler(context).execute(orderobject);
            }
        });

        return view;
    }

    public SearchResultsAdapter(List<String> item_name, List<String> item_id, List<String> catalog_number, List<String> price, List<String> company, List<String> type, Context context) {
        this.item_name = item_name;
        Log.v(LOG_TAG, "lets see waht they got" + item_name.toString());
        this.item_id = item_id;
        this.catalog_number = catalog_number;
        this.price = price;
        this.company = company;
        this.type = type;
        this.context = context;
        Log.v(LOG_TAG, "created");
    }
}
