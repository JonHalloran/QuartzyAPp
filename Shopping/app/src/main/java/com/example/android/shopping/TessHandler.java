package com.example.android.shopping;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Jonathan on 7/9/2017.
 */

public class TessHandler extends AppCompatActivity{
    private static final String LOG_TAG = "TessHandler";
    Bitmap bitmap;
    TessBaseAPI tessBaseAPI;
    String dataLocation;
    String result;
    Context context = this;
    String language = "eng";

    public TessHandler() {
    }
/*
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Tess Handler constructor starting");
        /*
        tessBaseAPI.init(dataLocation, language);
        tessBaseAPI.setImage(bitmap);
        result = tessBaseAPI.getUTF8Text();
        Log.d(LOG_TAG, result);
        */

    public TessHandler (Context context){
        this.context = context;
    }
    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = dataLocation+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = dataLocation + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String analyzeImage(){
        Log.d(LOG_TAG, "analyzeImage");
        bitmap = BitmapFactory.decodeFile( Environment.getExternalStorageDirectory()+ "/cropped.jpg");
        Log.d(LOG_TAG, "Test1");

        dataLocation = context.getFilesDir() + "/tesseract/";
        //dataLocation = "/data/user/0/com.example.android.shopping/files/tesseract/";
        Log.d(LOG_TAG, dataLocation);
        Log.d(LOG_TAG, "Test3");

        checkFile(new File(dataLocation + "tessdata/"));
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(dataLocation, language);
        tessBaseAPI.setImage(bitmap);
        result = tessBaseAPI.getUTF8Text();;
        Log.d(LOG_TAG, "tess results:  "  + result);
        return result;

    }
}
