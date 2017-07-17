package com.example.android.shopping;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

/**
 * Created by Jonathan on 7/1/2017.
 */
class Login extends AppCompatActivity {
    String LOG_TAG = "Login";
    Context context = this;
    String login;
    String password;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //view stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        // button to save login
        Button bt_saveLogin = (Button) findViewById(R.id.bt_saveLogin);
        bt_saveLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // getting pw and login
                EditText et_login = (EditText) findViewById(R.id.et_login);
                login = et_login.getText().toString();

                EditText et_password = (EditText) findViewById(R.id.et_password);
                password = et_password.getText().toString();
                //making sure that valid strings are input and going back to main
                if(!login.matches("") && !password.matches("")){
                    SharedPreferences sharedPref = getSharedPreferences("myprefs", 0);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("Login", login);
                    editor.putString("Password", password);
                    editor.commit();
                    checkLogin();

                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                }// in case they don't put something in both
                else{
                    Toast.makeText(context, "Please enter a valid Login and Password", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    void checkLogin(){
        JSONObject loginObject = new JSONObject();
        try{
            loginObject.put("request_type", "login");
        }catch (Exception e){
            e.printStackTrace();
        }
        new QuartzyHandler(context).execute(loginObject);
    }
}
