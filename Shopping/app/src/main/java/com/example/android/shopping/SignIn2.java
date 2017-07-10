package com.example.android.shopping;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by Jonathan on 7/9/2017.
 */

public class SignIn2 extends AppCompatActivity {
    private static final String LOG_TAG = "SignIn2";
    TextView tv_ItemName;
    ImageView iv_signIn2CroppedImage;
    Context context = this;
    String jsonString = null;
    JSONObject resultObject = null;
    String itemName;
    String price;
    String id;
    String quantity;
    String quantityPrevRec;
    String catalogNumber;
    String vendor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin2);
        tv_ItemName = (TextView) findViewById(R.id.tv_sI2ItemName);
        iv_signIn2CroppedImage = (ImageView) findViewById(R.id.iv_signIn2CroppedImage);
        TextView tv_vendor = (TextView) findViewById(R.id.tv_sI2Vendor);
        TextView tv_catalogNumber = (TextView) findViewById(R.id.tv_sI2CatalogNumber);
        TextView tv_price = (TextView) findViewById(R.id.tv_sI2Price);
        Intent intent = getIntent();
        Button signIn = (Button) findViewById(R.id.bt_sI2SignIn);
        Button goBack = (Button) findViewById(R.id.bt_sI2GoBack);
        jsonString = intent.getExtras().getString("jsonString");
        // TODO: 7/9/2017 make adjustable quantity
        Log.d(LOG_TAG, "Json string in:  " + jsonString);
        parseJson();

        Bitmap croppedImage = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+ "/cropped.jpg");
        iv_signIn2CroppedImage.setImageBitmap(croppedImage);
        tv_ItemName.setText(itemName);
        tv_vendor.setText(vendor);
        tv_catalogNumber.setText(catalogNumber);
        tv_price.setText("$" + price);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quartzySignIn();
                Intent intent = new Intent(context, SignIn.class);
                startActivity(intent);
            }
        });
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SignIn.class);
                startActivity(intent);
            }
        });

    }

    private void parseJson(){
        // parses JSON string/Object from quartzy for data to show
        try {
            resultObject = new JSONObject(jsonString);
        } catch (Exception e) {
            Log.v(LOG_TAG, e.toString());
        }
        try {
            JSONObject smallerJSONObject = resultObject.getJSONArray("data").getJSONObject(0);
            id = smallerJSONObject.getString("id");
            JSONObject evenSmallerObject = smallerJSONObject.getJSONObject("attributes");
            itemName = evenSmallerObject.getString("item_name");
            quantity = evenSmallerObject.getString("quantity");
            quantityPrevRec = evenSmallerObject.getString("quantity_received");
            catalogNumber = evenSmallerObject.getString("catalog_number");
            vendor = evenSmallerObject.getString("vendor_name");
            price = evenSmallerObject.getString("price");

        }catch (Exception e) {
            Log.v(LOG_TAG, e.toString());
        }
    }

    private void quartzySignIn (){
        //sends request via QuartzyHandler to sign an ordered item in.
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("request_type", "signin");
            jsonObject.put("id", id);
            jsonObject.put("quantity", quantity);
            jsonObject.put("quantityreceived", quantity);
        }catch (Exception e){
            e.printStackTrace();
        }
        new QuartzyHandler(context).execute(jsonObject);
    }
}
