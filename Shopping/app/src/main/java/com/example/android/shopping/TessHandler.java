package com.example.android.shopping;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Jonathan on 7/9/2017.
 */

class TessHandler extends AppCompatActivity{

    private static final String LOG_TAG = "TessHandler";
    Bitmap bitmap = null;
    TessBaseAPI tessBaseAPI;
    String datapath;
    String result;
    Context context = this;
    String language = "eng";

    TessHandler (Context context) {
        this.context = context;

        //init image
        bitmap = BitmapFactory.decodeFile( Environment.getExternalStorageDirectory()+ "/cropped.jpg");
        Log.v(LOG_TAG, "bitmap isn't null:  " + Boolean.toString(bitmap != null));

        //initialize Tesseract API
        String language = "eng";
        datapath = context.getFilesDir()+ "/tesseract/";
        Log.d("1", datapath);
        tessBaseAPI = new TessBaseAPI();

        checkFile(new File(datapath + "tessdata/"));
        Log.d("datapath", datapath);
        tessBaseAPI.init(datapath, language);
        Log.v(LOG_TAG, "post init");
    }

    String analyzeImage(){
        String OCRresult = null;
        tessBaseAPI.setImage(bitmap);
        OCRresult = tessBaseAPI.getUTF8Text();
        return OCRresult;
    }

    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = context.getAssets();

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
