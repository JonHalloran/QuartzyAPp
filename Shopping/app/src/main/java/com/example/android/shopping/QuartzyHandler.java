package com.example.android.shopping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
 * Handles all interection with Quartzy.
 */
// TODO: 7/9/2017 confirmation of success toasts
// TODO: 7/11/2017 fix how handling cookies, don't think I'm using cookie handler correctly
// TODO: 7/11/2017 have class check before relogging
class QuartzyHandler extends AsyncTask<JSONObject, Integer, String> {
    private static final String LOG_TAG = "QuartzyHandler";
    private CookieManager cookieManager = new CookieManager(null,CookiePolicy.ACCEPT_ALL);
    private static final String COOKIES_HEADER = "Set-Cookie";
    private String person_id  = null;
    private String authorization = null;
    private String inputType;
    private String group_id = null;
    private String quartzySearchString = null;
    private String quartzySearchPage = null;


    Context context;

    QuartzyHandler (Context context){
        this.context= context;

    }
    protected String doInBackground(JSONObject... jsonObjects){
        Log.v(LOG_TAG, "doInBackground running");
        CookieHandler.setDefault(cookieManager);
        cookieManager.getCookieStore().removeAll();
        login();
        getToken();
        String resultsString = null;

        for(JSONObject jsonObject : jsonObjects) {
            //This is all to handle different requests types in same async task.  Probably a better way to do this but not sure what it is.
            try {
                inputType = jsonObject.getString("request_type");
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch (inputType) {
                case "login":
                    getIDs();
                    break;
                case "search":
                    checkIDs();
                    resultsString = searchQuartzy(jsonObject);
                    break;
                case "order":
                    checkIDs();
                    orderItem(jsonObject);
                    break;
                case "requestssearch":
                    checkIDs();
                    resultsString = requestsSearch(jsonObject);
                    break;
                case "signin":
                    checkIDs();
                    signIn(jsonObject);
                    break;
            }
        }
        return resultsString;
    }
    private void login() {
        // using login info to get "frontend:session" which will be needed
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
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) addr.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            conn.setRequestMethod("POST");
        }catch (Exception e){
            e.printStackTrace();
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
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os));
            writer.write(post_data, 0, post_data.length());
            writer.flush();
            writer.close();
            os.close();

        }catch (Exception e){
            e.printStackTrace();
        }
        set_cookie(conn);
    }
    private void getToken() {
        //kinda curious what the client id actually is
        // this method gets the authorization using the frontend:session cookie
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
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) token_server_url.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            conn.setRequestMethod("POST");
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        set_cookie(conn);
        try {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String string = readStream(in);
            jsonConverter(string);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    private void getIDs() {
        // Gets the person_ID/user_ID from quartzy so that it shows up as the correct person doing things.11

        // TODO: 7/11/2017 switch thsi method to be done after login is first put in and then not run again.  Also add getting the group ID as that shouldn't be hardcoded.
        {
            String person_ID_server = "https://io.quartzy.com/users/39647";
            String before_person = "{\"type\":\"person\",\"id\":\"";
            String after_person = "\"}},\"avatar\":";

            Log.v(LOG_TAG, "getIDs starting");

            URL token_server_url = null;
            CookieHandler.setDefault(cookieManager);

            try {
                token_server_url = new URL(person_ID_server);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) token_server_url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                conn.setRequestMethod("GET");
            } catch (Exception e) {
                e.printStackTrace();
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
                e.printStackTrace();
            }

            try {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String string = readStream(in);
                person_id = string.substring(string.indexOf(before_person)+23, string.indexOf(after_person));
                group_id = string.substring(string.indexOf("\"group\",\"id\":\"")+14,string.indexOf("\"}},\"person\"") );
                Log.v(LOG_TAG, "to get userd_id  " +person_id);

                SharedPreferences sharedPref = context.getSharedPreferences("myprefs", 0);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("person_id", person_id);
                editor.putString("group_id", group_id);
                editor.commit();

            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent(context, Login.class);
                context.startActivity(intent);
            }
        }
    }
    private void orderItem (JSONObject orderJSONObject){

        //orders items... as the name would indicate
        String item_name = null;
        String quantity = null;
        String item_id = null;
        String catalog_number = null;
        String price = null;
        String company = null;
        String type = null;
        try{
            // necessary fields to order items
            item_name = orderJSONObject.getString("item_name");
            quantity = orderJSONObject.getString("quantity");
            item_id = orderJSONObject.getString("item_id");
            catalog_number = orderJSONObject.getString("catalog_number");
            price = orderJSONObject.getString("price");
            company = orderJSONObject.getString("company");
            type = orderJSONObject.getString("type");
        }catch (Exception e){
            e.printStackTrace();
        }
        // not sure if better to make a json object or not
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


        try {
            requestURL = new URL(requestaddress);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) requestURL.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            conn.setRequestMethod("POST");
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        set_cookie(conn);

    }
    public void get_cookie(HttpURLConnection conn) {
        // TODO: 7/11/2017 fix this method up
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
    private void set_cookie(HttpURLConnection conn) {
        Map<String, List<String>> headerFields = conn.getHeaderFields();
        Set<String> keys = headerFields.keySet();
        for (String string : keys){
            Log.v(LOG_TAG, string + ":  " + headerFields.get(string).toString());

        }
        List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                Log.v(LOG_TAG, cookie);
            }
        }

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
        //Just currently used to get the authorization.
        Log.v(LOG_TAG, "jsonConverter Running to get access_token");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(string);
            String access_token = jsonObject.getString("access_token");
            authorization = "Bearer " + access_token;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private String searchQuartzy(JSONObject jsonObject){
        // Searches quartzy's inventory for specified item
        Log.v(LOG_TAG, "Search Quartzy is Running");
        String searchString = null;
        String page = null;
        try{
            searchString = jsonObject.getString("search_string");
            page = jsonObject.getString("page");
        }catch (Exception e){
            e.printStackTrace();
        }
        String longString = null;
        String searchStringp1 = "https://io.quartzy.com/groups/46170/items?limit=10&page=";
        String searchStringp2 = "&query=";
        String searchStringp3 = "&sort=-created_at";
        String searchStringFull = searchStringp1 +page + searchStringp2 + searchString + searchStringp3;
        URL searchURL = null;

        try {
            searchURL = new URL(searchStringFull);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) searchURL.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            conn.setRequestMethod("GET");
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }

        try {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String encoding = new InputStreamReader(conn.getInputStream()).getEncoding();
            Log.v(LOG_TAG, "Encoding is:  " + encoding);
            longString = readStream(in);
            Log.v(LOG_TAG, "Input:  " + longString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v(LOG_TAG, "Longstring:  " + longString);

        quartzySearchString = searchString;
        quartzySearchPage = page;
        return longString;

    }
    private String requestsSearch (JSONObject jsonObject){
        // searches request for specified string.
        Log.d(LOG_TAG, "requestSearchRunning");
        String searchString = null;
        String returnString = null;
        try{
            searchString = jsonObject.getString("search_string");
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "searchString:  "+ searchString);
        String requestSearchStringp1 = "https://io.quartzy.com/groups/46170/order-requests?group%5B%5D=46170&limit=1&query=";
        String requestSearchStringp2 = "&sort=-status_changed_at&status%5B%5D=ORDERED&status%5B%5D=BACKORDERED";
        String requestSearchString = requestSearchStringp1 + searchString + requestSearchStringp2;
        URL searchURL = null;

        try {
            searchURL = new URL(requestSearchString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) searchURL.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            conn.setRequestMethod("GET");
        } catch (Exception e) {
            e.printStackTrace();
        }

        conn.setRequestProperty("Accept", "application/vnd.api+json");
        //conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.setRequestProperty("Authorization", authorization);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Host", "io.quartzy.com");
        conn.setRequestProperty("Origin", "https://app.quartzy.com");
        //conn.setRequestProperty("Referer", "https://app.quartzy.com/groups/190045/requests/new?itemIds[]=26035030");
        conn.setRequestProperty("User-Agent", "Chrome/58.0.3029.110");
        conn.setInstanceFollowRedirects(false);

        try{
            conn.connect();
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String encoding = new InputStreamReader(conn.getInputStream()).getEncoding();
            Log.v(LOG_TAG, "Encoding is:  " + encoding);
            returnString = readStream(in);
            Log.v(LOG_TAG, "Input:  " + returnString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v(LOG_TAG, "returnString:  " + returnString);
        return returnString;

    }
    private void signIn(JSONObject jsonObject){
        Log.d(LOG_TAG, "signIn");
        String id = null;
        String quantity = null;
        String quantityreceived = null;
        URL signInURL = null;
        String payloadP1 = null;
        String payloadP2 = null;
        String payloadP3 = null;
        String payloadP4 = null;
        String payloadComplete = null;


        try{
            id = jsonObject.getString("id");
            quantity = jsonObject.getString("quantity");
            quantityreceived = jsonObject.getString("quantityreceived");
        }catch (Exception e){
            e.printStackTrace();
        }
        payloadP3 ="\"}},\"order_request\":{\"data\":{\"type\":\"order_request\",\"id\":\"";
        payloadP4 = "\"}}},\"type\":\"order_request_status_change\"}}";
        if (quantity.equals(quantityreceived)){
            payloadP1 = "{\"data\":{\"attributes\":{\"status\":\"RECEIVED\",\"message\":null,\"is_partial\":false},\"relationships\":{\"updated_by\":{\"data\":{\"type\":\"user\",\"id\":\"";
            // TODO: 7/9/2017 figure out person_id
            payloadComplete = payloadP1 + "39647" + payloadP3 +id + payloadP4;
        }else if (!quantity.equals(quantityreceived)){
            payloadP1= "{\"data\":{\"attributes\":{\"status\":\"RECEIVED\",\"message\":\"Received ";
            payloadP2= "\",\"is_partial\":true},\"relationships\":{\"updated_by\":{\"data\":{\"type\":\"user\",\"id\":\"";
            payloadComplete = payloadP1 +quantityreceived +" of "+ quantity + payloadP2 + person_id + payloadP3 +id + payloadP4;
        }

        Log.v(LOG_TAG,payloadComplete);

        try {
            signInURL = new URL("https://io.quartzy.com/order-request-status-changes");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) signInURL.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            conn.setRequestMethod("POST");
        } catch (Exception e) {
            e.printStackTrace();
        }

        conn.setRequestProperty("Accept", "application/vnd.api+json");
        //conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.setRequestProperty("Authorization", authorization);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Type", "application/vnd.api+json");
        conn.setRequestProperty("Host", "io.quartzy.com");
        conn.setRequestProperty("Origin", "https://app.quartzy.com");
        conn.setRequestProperty("Referer", "https://app.quartzy.com/groups/190045/requests?status[]=ORDERED");
        conn.setRequestProperty("User-Agent", "Chrome/58.0.3029.110");
        conn.setInstanceFollowRedirects(false);

        try {
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os));
            writer.write(payloadComplete, 0, payloadComplete.length());
            writer.flush();
            writer.close();
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        set_cookie(conn);

    }
    @Override
    protected void onPostExecute(String string) {
        Log.v(LOG_TAG, "onPostExecute running" );
        Log.v(LOG_TAG, "string:  " + string);
        super.onPostExecute(string);
        if(inputType.equals("search")) {
            Intent intent = new Intent(context, SearchResults.class);
            intent.putExtra("jsonString", string);
            // TODO: 7/16/2017 find a more elegant way of passing search string and page
            Log.d(LOG_TAG, "quartzySearchPage:  " + quartzySearchPage);
            intent.putExtra("searchString",quartzySearchString);
            intent.putExtra("searchPage", quartzySearchPage);
            context.startActivity(intent);
            context.startActivity(intent);
        }
        if (inputType.equals( "requestssearch")){

            Intent intent = new Intent(context, SignIn2.class);
            intent.putExtra("jsonString", string);

        }

    }
    private void checkIDs(){
        SharedPreferences sharedPref =  context.getSharedPreferences("myprefs", 0);
        person_id = sharedPref.getString("person_id", "");
        group_id = sharedPref.getString("group_id", "");
        if(person_id.equals("") || group_id.equals("")) {
            getIDs();
        }
    }
}