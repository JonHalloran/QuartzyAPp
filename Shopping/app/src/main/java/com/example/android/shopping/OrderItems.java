package com.example.android.shopping;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Jonathan on 6/21/2017.
 */

public class OrderItems extends AsyncTask<String, Integer, Integer>{
    String LOG_TAG = "OrderItems";

    public String testConnection() {
        String stream = "Didn't work";
        Log.v(LOG_TAG, "testConnection is running");
        Log.v(LOG_TAG, "Check if connecting to server" + Boolean.toString(isConnectedToServer("http://www.google.com", 1000)));
        try {
            URL url = new URL("http://www.google.com");
            Log.v(LOG_TAG, "1");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(1000);
            Log.v(LOG_TAG, "2");
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            Log.v(LOG_TAG, "3");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String string = null;
            Log.v(LOG_TAG, "4");
            while(bufferedReader != null) {
                string = string + bufferedReader.read();
            }
            Log.v(LOG_TAG, string);

        } catch (MalformedURLException ex) {
            Log.v(LOG_TAG, "Constructor, Malformed URL Exception");
        } catch (IOException ex) {
            Log.v(LOG_TAG, "Constructor, IOException");
        } finally {
            return stream;
        }
    }
    public boolean isConnectedToServer(String url, int timeout) {
        try{
            Log.v(LOG_TAG, "isConnectedToServer is running");
            URL myUrl = new URL(url);
            Log.v(LOG_TAG, "1");
            URLConnection connection = myUrl.openConnection();
            Log.v(LOG_TAG, "2");
            connection.setConnectTimeout(timeout);
            Log.v(LOG_TAG, "3");
            connection.connect();
            Log.v(LOG_TAG, "4");
            return true;
        } catch (Exception e) {
            // Handle your exceptions
            return false;
        }
    }
    public Integer doInBackground(String ... strings){
        // TODO: 6/29/2017
        testConnection();
        return Integer.valueOf(42);
    }
}