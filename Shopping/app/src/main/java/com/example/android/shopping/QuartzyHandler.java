package com.example.android.shopping;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by Jonathan on 7/2/2017.
 */

public class QuartzyHandler extends AsyncTask<JSONObject, Integer, String> {
    private static final String LOG_TAG = "QuartzyHandler";
    CookieManager cookieManager = new CookieManager(null,CookiePolicy.ACCEPT_ALL);
    static final String COOKIES_HEADER = "Set-Cookie";
    private String access_token = null;
    private String person_id  = null;
    private String authorization = null;
    private boolean isSearch = false;
    private JSONObject inputObject;
    private String inputType;


    Context context;

    public QuartzyHandler (Context context){
        this.context= context;

    }
    protected String doInBackground(JSONObject... jsonObjects){
        Log.v(LOG_TAG, "doInBackground running");
        CookieHandler.setDefault(cookieManager);
        cookieManager.getCookieStore().removeAll();
        login( 10000);
        getToken();
        getPersonID();
        String resultsstring = null;

        for(JSONObject jsonObject : jsonObjects) {
            try {
                inputType = jsonObject.getString("request_type");
            } catch (Exception e) {
                Log.v(LOG_TAG, e.toString());
            }
            switch (inputType) {
                case "search":
                    resultsstring = searchQuartzy(jsonObject);
                    break;
                case "order":
                    orderItem(jsonObject);
            }
        }
        return resultsstring;
    }
    public String login(int len) {
        SharedPreferences sharedPref = context.getSharedPreferences("myprefs", 0);
        String password = sharedPref.getString("Password", null);
        String login = sharedPref.getString("Login", null);
        String stupidEmail = login.replace("@", "%40");
        String post_data = "txtEmail=" + stupidEmail + "&send_to=&txtPassword=" + password + "&commit=";
        Log.v(LOG_TAG, "Login running");
        URL addr = null;
        CookieHandler.setDefault(cookieManager);
        try {
            addr = new URL("https://www.quartzy.com/auth/login");
        } catch (MalformedURLException e) {
            Log.v(LOG_TAG, "4" + e.toString());
        }
        StringBuffer data = new StringBuffer();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) addr.openConnection();
        } catch (IOException e) {
            return "Open connection error";
        }
        try {
            conn.setRequestMethod("POST");
        }catch (Exception e){
            Log.v(LOG_TAG, "5" + e.toString());
        }

        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.setRequestProperty("Cache-Control", "max-age=0");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Host", "www.quartzy.com");
        conn.setRequestProperty("Origin", "https://www.quartzy.com");
        conn.setRequestProperty("Referer", "https://www.quartzy.com/login");
        conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
        conn.setRequestProperty("User-Agent", "Chrome/58.0.3029.110");

        conn.setInstanceFollowRedirects(false);
        // conn.setRequestProperty("", "");

        conn.setDoOutput(true);
        conn.setDoInput(true);
        Log.v(LOG_TAG, "pre output:" + conn.toString());
        //conn.setInstanceFollowRedirects(true);

        try {
            Log.v(LOG_TAG, "output beginning");
            OutputStream os = conn.getOutputStream();
            Log.v(LOG_TAG, "2");
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os));
            Log.v(LOG_TAG, post_data);
            writer.write(post_data, 0, post_data.length());
            Log.v(LOG_TAG, "4" + conn.toString());
            writer.flush();
            writer.close();
            os.close();
            Log.v(LOG_TAG, "5" + conn.toString());

        }catch (Exception e){
            Log.v(LOG_TAG, "1" + e.toString());
        }
        set_cookie(conn);
        try {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String string = readStream(in);
        } catch (Exception e){
            Log.v(LOG_TAG, "2" + e.toString());
        }
        //POST data:
        String post_str = post_data;
        data.append(post_str);
        try {
            conn.connect();
        } catch (IOException e) {
            return "Connecting error";
        }
        DataOutputStream dataOS = null;
        try {
            dataOS = new DataOutputStream(conn.getOutputStream());
        } catch (IOException e2) {
            return "Out stream error";
        }
        try {
            dataOS.writeBytes(data.toString());
        } catch (IOException e) {
            return "Out stream error 1";
        }

        return "hello";
    }
    public void getToken() {
        String client_id = "QLAqCIFOysTc34xv6HuSazE5GmPAF6RGYZgtyRR8";
        String token_server = "https://io.quartzy.com/oauth/tokens";

        URL token_server_url = null;
        CookieHandler.setDefault(cookieManager);

        String refresh_token = null;
        List<HttpCookie> frontend_session = cookieManager.getCookieStore().getCookies();
        for (HttpCookie httpcookie : frontend_session) {
            String cookie = httpcookie.toString();
            if (cookie.contains("frontend:session")) {
                int index = cookie.indexOf("refresh_token%22%3A%22") + 22;
                int index2 = cookie.indexOf("%22%2C%22expires_in");
                refresh_token = cookie.substring(index, index2);
            }
        }
        String post_data = "grant_type=refresh_token&client_id=" + client_id + "&refresh_token=" + refresh_token;

        try {
            token_server_url = new URL(token_server);
        } catch (MalformedURLException e) {
            Log.v(LOG_TAG, e.toString());
        }
        StringBuffer data = new StringBuffer();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) token_server_url.openConnection();
        } catch (IOException e) {
            Log.v(LOG_TAG, e.toString());
        }
        try {
            conn.setRequestMethod("POST");
        } catch (Exception e) {
            Log.v(LOG_TAG, e.toString());
        }

        conn.setRequestProperty("Accept", "*/*");
        //conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Host", "io.quartzy.com");
        conn.setRequestProperty("Origin", "https://www.quartzy.com");
        conn.setRequestProperty("Referer", "https://www.quartzy.com");
        conn.setRequestProperty("User-Agent", "Chrome/58.0.3029.110");
        conn.setInstanceFollowRedirects(false);
        // conn.setRequestProperty("", "");

        conn.setDoOutput(true);
        conn.setDoInput(true);

        try {
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os));
            writer.write(post_data, 0, post_data.length());
            writer.flush();
            writer.close();
            os.close();

        } catch (Exception e) {
            Log.v(LOG_TAG, e.toString());
        }
        set_cookie(conn);
        try {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String encoding = new InputStreamReader(conn.getInputStream()).getEncoding();
            String string = readStream(in);
            jsonConverter(string);
        } catch (Exception e) {
            Log.v(LOG_TAG, "2" + e.toString());
        }


    }
    public void getPersonID() {
        {
            String person_ID_server = "https://io.quartzy.com/users/39647";
            String before_person = "{\"type\":\"person\",\"id\":\"";
            String after_person = "\"}},\"avatar\":";

            Log.v(LOG_TAG, "getPersonID starting");

            URL token_server_url = null;
            CookieHandler.setDefault(cookieManager);

            try {
                token_server_url = new URL(person_ID_server);
            } catch (MalformedURLException e) {
                Log.v(LOG_TAG, e.toString());
            }
            StringBuffer data = new StringBuffer();
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) token_server_url.openConnection();
            } catch (IOException e) {
                Log.v(LOG_TAG, e.toString());
            }
            try {
                conn.setRequestMethod("GET");
            } catch (Exception e) {
                Log.v(LOG_TAG, e.toString());
            }
            conn.setRequestProperty("Accept", "application/vnd.api+json");
            //conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.setRequestProperty("Authorization", authorization );
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Host", "io.quartzy.com");
            conn.setRequestProperty("Origin", "https://app.quartzy.com");
            conn.setRequestProperty("Referer", "https://app.quartzy.com");
            conn.setRequestProperty("User-Agent", "Chrome/58.0.3029.110");
            conn.setInstanceFollowRedirects(false);
            // conn.setRequestProperty("", "");
            conn.setDoInput(true);
            try{
                conn.connect();
            }catch (Exception e){
                Log.v(LOG_TAG, e.toString());
            }

            try {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String encoding = new InputStreamReader(conn.getInputStream()).getEncoding();
                Log.v(LOG_TAG, "Encoding is:  " + encoding);
                String string = readStream(in);
                Log.v(LOG_TAG, "person_id tag string:  "+ string);
                String stringiest = string.substring(string.indexOf(before_person)+23, string.indexOf(after_person));
                Log.v(LOG_TAG, "to get userd_id  " +stringiest);
                person_id = stringiest;

                Log.v(LOG_TAG, "Input Stream oauth:  " + string);
            } catch (Exception e) {
                Log.v(LOG_TAG, "2" + e.toString());
            }


        }
    }
    public void orderItem (JSONObject orderJSONObject){
        String item_name = null;
        String quantity = null;
        String item_id = null;
        String catalog_number = null;
        String price = null;
        String company = null;
        String type = null;
        try{
            item_name = orderJSONObject.getString("item_name");
            quantity = orderJSONObject.getString("quantity");
            item_id = orderJSONObject.getString("item_id");
            catalog_number = orderJSONObject.getString("catalog_number");
            price = orderJSONObject.getString("price");
            company = orderJSONObject.getString("company");
            type = orderJSONObject.getString("type");
        }catch (Exception e){
            Log.v(LOG_TAG, e.toString());
        }

        String requestaddress = "https://io.quartzy.com/groups/46170/order-requests";
        URL requestURL = null;
        String requestp1 = "{\"data\":{\"attributes\":{\"item_name\":\"";
        String requestp2= "\",\"quantity\":\"";
        String requestp3 ="\",\"quantity_received\":0,\"catalog_number\":\"";
        String requestp4 = "\",\"url\":null,\"price\":\"";
        String requestp5 = "\",\"details\":\"\",\"source\":\"\",\"shipping\":\"\",\"unit_size\":\"1\",\"bought_from\":null,\"confirmation_number\":null,\"requisition_number\":\"\",\"invoice_number\":null,\"tracking_number\":null,\"notes\":\"\",\"is_urgent\":false,\"vendor_name\":null},\"relationships\":{\"item\":{\"data\":{\"type\":\"item\",\"id\":\"";
        String requestp6 = "\"}},\"status\":{\"data\":null},\"group\":{\"data\":{\"type\":\"group\",\"id\":\"46170\"}},\"requester\":{\"data\":{\"type\":\"person\",\"id\":\"";
        String requestp7 =  "\"}},\"type\":{\"data\":{\"type\":\"type\",\"id\":\"";
        String requestp8 = "\"}},\"grant\":{\"data\":null},\"purchase_order\":{\"data\":null},\"offer\":{\"data\":null},\"company\":{\"data\":{\"type\":\"company\",\"id\":\"";
        String requestp9 = "\"}},\"order_item\":{\"data\":null},\"cart_item\":{\"data\":null},\"created_by\":{\"data\":null},\"updated_by\":{\"data\":null},\"vendor_product\":{\"data\":null}},\"type\":\"order_request\"}}";
        String request = requestp1 + item_name + requestp2 + quantity +requestp3 + catalog_number +requestp4 + price + requestp5 +item_id + requestp6 +person_id + requestp7+ type + requestp8 + company + requestp9;


        // Dillin Lab
        //request = "{\"data\":{\"attributes\":{\"item_name\":\"Autoclave Bags, Nonhazardous Waste 36 x 45 in\",\"quantity\":\"1\",\"quantity_received\":0,\"catalog_number\":\"14220-044\",\"url\":null,\"price\":\"122.63\",\"details\":\"\",\"source\":\"\",\"shipping\":\"\",\"unit_size\":\"case of 100\",\"bought_from\":null,\"confirmation_number\":null,\"requisition_number\":\"\",\"invoice_number\":null,\"tracking_number\":null,\"notes\":\"\",\"is_urgent\":false,\"vendor_name\":null},\"relationships\":{\"item\":{\"data\":{\"type\":\"item\",\"id\":\"3246454\"}},\"status\":{\"data\":null},\"group\":{\"data\":{\"type\":\"group\",\"id\":\"46170\"}},\"requester\":{\"data\":{\"type\":\"person\",\"id\":\"49232\"}},\"type\":{\"data\":{\"type\":\"type\",\"id\":\"115262\"}},\"grant\":{\"data\":null},\"purchase_order\":{\"data\":null},\"offer\":{\"data\":null},\"company\":{\"data\":{\"type\":\"company\",\"id\":\"2055\"}},\"order_item\":{\"data\":null},\"cart_item\":{\"data\":null},\"created_by\":{\"data\":null},\"updated_by\":{\"data\":null},\"vendor_product\":{\"data\":null}},\"type\":\"order_request\"}}";
        // Test_Lab

        try {
            requestURL = new URL(requestaddress);
        } catch (MalformedURLException e) {
            Log.v(LOG_TAG,e.toString());
        }
        StringBuffer data = new StringBuffer();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) requestURL.openConnection();
        } catch (IOException e) {
            Log.v(LOG_TAG, e.toString());
        }
        try {
            conn.setRequestMethod("POST");
        } catch (Exception e) {
            Log.v(LOG_TAG,  e.toString());
        }

        conn.setRequestProperty("Accept", "application/vnd.api+json");
        //conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.setRequestProperty("Authorization", authorization);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Type", "application/vnd.api+json");
        conn.setRequestProperty("Host", "io.quartzy.com");
        conn.setRequestProperty("Origin", "https://app.quartzy.com");
        conn.setRequestProperty("Referer", "https://app.quartzy.com/groups/190045/requests/new?itemIds[]=26035030");
        conn.setRequestProperty("User-Agent", "Chrome/58.0.3029.110");
        conn.setInstanceFollowRedirects(false);

        try {
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os));
            writer.write(request, 0, request.length());
            writer.flush();
            writer.close();
            os.close();

        } catch (Exception e) {
            Log.v(LOG_TAG, e.toString());
        }
        set_cookie(conn);

    }
    public void get_cookie(HttpURLConnection conn) {
        Log.v(LOG_TAG, "get_cookie running");
        SharedPreferences sh_pref_cookie = context.getSharedPreferences("cookies", Context.MODE_PRIVATE);
        String cook_new ;
        String COOKIES_HEADER;
        if (conn.getHeaderField("Set-Cookie") != null) {
            COOKIES_HEADER = "Set-Cookie";
        }
        else {
            COOKIES_HEADER = "Cookie";
        }
        cook_new = conn.getHeaderField(COOKIES_HEADER);
        if (cook_new.indexOf("remember_me", 0) >= 0) {
            SharedPreferences.Editor editor = sh_pref_cookie.edit();
            editor.putString("Cookie", cook_new);
            editor.commit();
        }
    }
    public void set_cookie(HttpURLConnection conn) {
        Log.v(LOG_TAG, "set_cookie running");
        Map<String, List<String>> headerFields = conn.getHeaderFields();
        Set<String> keys = headerFields.keySet();
        for (String string : keys){
            Log.v(LOG_TAG, string + ":  " + headerFields.get(string).toString());

        }
        List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
        if (cookiesHeader != null) {
            Log.v(LOG_TAG, "I think I should be adding cookies");
            for (String cookie : cookiesHeader) {
                cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                Log.v(LOG_TAG, cookie);
            }
        }

        /*
        SharedPreferences sh_pref_cookie = context.getSharedPreferences("cookies", Context.MODE_PRIVATE);
        String COOKIES_HEADER = "Cookie";
        String cook = sh_pref_cookie.getString(COOKIES_HEADER, "no_cookie");
        if (!cook.equals("no_cookie")) {
            conn.setRequestProperty(COOKIES_HEADER, cook);
        }
        */
    }
    private String readStream(InputStream is) throws IOException {


        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        for (String line = r.readLine(); line != null; line =r.readLine()){
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }
    private void jsonConverter (String string){
        Log.v(LOG_TAG, "jsonConverter Running to get access_token");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(string);
            Log.v(LOG_TAG, "here's hoping:  " +jsonObject.getString("access_token"));
            access_token = jsonObject.getString("access_token");
            authorization = "Bearer " + access_token;
        }catch (Exception e){
            Log.v(LOG_TAG, e.toString());
        }
    }
    public String searchQuartzy(JSONObject jsonObject){
        Log.v(LOG_TAG, "Search Quartzy is Running");
        String searchString = null;
        try{
            searchString = jsonObject.getString("search_string");
        }catch (Exception e){
            Log.v(LOG_TAG, e.toString());
        }
        String longString = null;
        String searchStringp1 = "https://io.quartzy.com/groups/46170/items?limit=10&page=1&query=";
        String searchStringp2 = "&sort=-created_at";
        String searchStringFull = searchStringp1 + searchString + searchStringp2;
        URL searchURL = null;

        try {
            searchURL = new URL(searchStringFull);
        } catch (MalformedURLException e) {
            Log.v(LOG_TAG, e.toString());
        }
        StringBuffer data = new StringBuffer();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) searchURL.openConnection();
        } catch (IOException e) {
            Log.v(LOG_TAG, e.toString());
        }
        try {
            conn.setRequestMethod("GET");
        } catch (Exception e) {
            Log.v(LOG_TAG, e.toString());
        }

        conn.setRequestProperty("Accept", "application/vnd.api+json");
        //conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.setRequestProperty("Authorization", authorization);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Host", "io.quartzy.com");
        conn.setRequestProperty("Origin", "https://app.quartzy.com");
        conn.setRequestProperty("Referer", "https://app.quartzy.com/groups/190045/requests/new?itemIds[]=26035030");
        conn.setRequestProperty("User-Agent", "Chrome/58.0.3029.110");
        conn.setInstanceFollowRedirects(false);

        try{
            conn.connect();
        }catch (Exception e){
            Log.v(LOG_TAG, e.toString());
        }

        try {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String encoding = new InputStreamReader(conn.getInputStream()).getEncoding();
            Log.v(LOG_TAG, "Encoding is:  " + encoding);
            String string = readStream(in);
            longString = string;
            Log.v(LOG_TAG, "Input:  " + longString);
        } catch (Exception e) {
            Log.v(LOG_TAG, "2" + e.toString());
        }
        Log.v(LOG_TAG, "Longstring:  " + longString.toString());
        return longString;

    }

    @Override
    protected void onPostExecute(String string) {
        Log.v(LOG_TAG, "onPostExecute running" );
        super.onPostExecute(string);
        if(inputType == "search") {
            Intent intent = new Intent(context, SearchResults.class);
            intent.putExtra("jsonString", string);
            context.startActivity(intent);
        }
    }
}