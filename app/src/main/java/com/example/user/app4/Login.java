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

    private void login() {
        if (!validate()) {
            onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);
        mAuthTask = new UserLoginTask(nameText.getText().toString(),passwordText.getText().toString());
        mAuthTask.execute();

    }

    private boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String password = passwordText.getText().toString();

        if (name.isEmpty()) {
            nameText.setError("enter a valid name");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (password.isEmpty()) {
            passwordText.setError("enter a valid password");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        loginButton.setEnabled(true);
    }

    public void onLoginSuccess() {
        String name = nameText.getText().toString();
        String password = passwordText.getText().toString();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", name);
        editor.putString("password", password);
        editor.commit();
        loginButton.setEnabled(true);
        Intent in = new Intent(Login.this, Chat.class);
        startActivity(in);
        finish();
    }

    public class UserLoginTask extends AsyncTask<Void, Void, JSONObject> {
        private HashMapParser map;
        UserLoginTask(String name, String pass) {
            this.map = new HashMapParser();
            map.put("userName",name);
            map.put("password",pass);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            PostMsg pm = new PostMsg(User.address+"MyLogin",this.map);
            return pm.sendPostMsg();
        }

        @Override
        protected void onPostExecute(final JSONObject json) {
          try {
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
