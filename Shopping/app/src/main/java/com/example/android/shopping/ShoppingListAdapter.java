package com.example.android.shopping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by Jonathan on 6/28/2017.
 */

public class ShoppingListAdapter extends BaseAdapter {

    private String[] itemList;
    private int[] orderAmount;
    Context context;

    public int getCount(){
        int countlength = itemList.length;
        return countlength;
    }
    public View getView(final int i, View view, ViewGroup viewGroup){
        final DatabaseHelper databaseHelper = new DatabaseHelper(context);
        orderAmount[i]= databaseHelper.getOrderAmount(itemList[i]);
        if (view == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.inventory_item_mark2, null);
        }
        TextView tv_item = (TextView)view.findViewById(R.id.tv_item);
        tv_item.setText(itemList[i]);

        final TextView tv_orderCount = (TextView)view.findViewById(R.id.tv_orderCount);
        tv_orderCount.setText(Integer.toString(orderAmount[i]));

        Button bt_orderMore = (Button) view.findViewById(R.id.bt_orderMore);
        Button bt_orderFewer = (Button) view.findViewById(R.id.bt_orderFewer);

        bt_orderMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderAmount[i] ++;
                tv_orderCount.setText(Integer.toString(orderAmount[i]));
                databaseHelper.setOrderAmount(itemList[i], orderAmount [i]);
            }
        });
        bt_orderFewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderAmount[i]> 0){
                    orderAmount[i] --;
                    tv_orderCount.setText(Integer.toString(orderAmount[i]));
                    databaseHelper.setOrderAmount(itemList[i], orderAmount [i]);
                }else{
                    Toast.makeText(context, ".... You cant order fewer than 0 of something", Toast.LENGTH_SHORT);
                }
            }
        });


        return view;
    }
    public long getItemId(int i){
        // TODO: 6/28/2017
        long l = (long) i;
        return l;
    }
    public String getItem(int i){
        // TODO: 6/28/2017
        return itemList[i];
    }
    public ShoppingListAdapter(String[] itemList , Context context){
        this.context = context;
        this.itemList = itemList;
        orderAmount = new int[itemList.length];
    }
    private String[] getItemList(){
        return itemList;
    }
    private int[] getOrderAmount(){
        return orderAmount;
    }
}
