package com.example.user.app4;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class Login extends AppCompatActivity {

    private EditText nameText;
    private EditText passwordText;
    private Button loginButton;
    private Button regButton;
    private UserLoginTask mAuthTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set page vars
        setContentView(R.layout.activity_login);
        nameText = (EditText) findViewById(R.id.input_name);
        loginButton = (Button) findViewById(R.id.btn_login);
        regButton = (Button) findViewById(R.id.btn_Reg);
        passwordText = (EditText) findViewById(R.id.input_password);
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
        regButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /*
    the func takes input and check if you can lg in
     */
    private void login() {
        //if var are not valid-reject
        if (!validate()) {
            onLoginFailed();
            return;
        }
        loginButton.setEnabled(false);
        //ask for login permission from DB
        mAuthTask = new UserLoginTask(nameText.getText().toString(),passwordText.getText().toString());
        mAuthTask.execute();
    }

    /*
    the func validate all fields in the register activity
     */
    private boolean validate() {
        boolean valid = true;
        String name = nameText.getText().toString();
        String password = passwordText.getText().toString();
        //check if name id valid
        if (name.isEmpty()) {
            nameText.setError("enter a valid name");
            valid = false;
        } else {
            nameText.setError(null);
        }

        //check if pass id valid
        if (password.isEmpty()) {
            passwordText.setError("enter a valid password");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        //return if it valid or not
        return valid;
    }

    /*
    describe actions when log in faild
     */
    public void onLoginFailed() {
        //make a toast
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    /*
    dfines operation if login went good
     */
    public void onLoginSuccess() {
        //edit SharedPreferences
        String name = nameText.getText().toString();
        String password = passwordText.getText().toString();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", name);
        editor.putString("password", password);
        editor.commit();
        loginButton.setEnabled(true);
        //move to next activity
        Intent in = new Intent(Login.this, Chat.class);
        startActivity(in);
        finish();
    }

    /*
    this class is in charch on the communication woth the server
     */
    public class UserLoginTask extends AsyncTask<Void, Void, JSONObject> {
        private HashMapParser map;
        UserLoginTask(String name, String pass) {
            this.map = new HashMapParser();
            map.put("userName",name);
            map.put("password",pass);
        }

        /*
        namedoInBackground
        desc:send the post request!!and return the json
        */
        @Override
        protected JSONObject doInBackground(Void... params) {
            PostMsg pm = new PostMsg(User.address+"MyLogin",this.map);
            return pm.sendPostMsg();
        }

        /*
        name onPostExecute
        desc: take the json and act according to json answer
         */
        @Override
        protected void onPostExecute(final JSONObject json) {
          try {
              //if login was good
              if (json.getString("login_result").equals("success")) {
                  onLoginSuccess();
              } else {
                  onLoginFailed();
              }
          }catch (JSONException e){
              e.printStackTrace();
          }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;

        }
    }


}
