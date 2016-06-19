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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", "Yes");
        editor.commit();
        loginButton.setEnabled(true);
        Intent in = new Intent(Login.this, Chat.class);
        in.putExtra("name", nameText.getText().toString());
        startActivity(in);
        finish();
    }

    public class UserLoginTask extends AsyncTask<Void, Void, JSONObject> {
        private final String name;
        private final String pass;

        UserLoginTask(String name, String pass) {
            this.name = name;
            this.pass = pass;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                URL url = new URL("http://10.0.0.1:8080/Server/MyLogin");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setReadTimeout(100000);
                urlConnection.setConnectTimeout(150000);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                //urlConnection.setRequestProperty("userName", name);
                //urlConnection.setRequestProperty("password", pass);
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();
                    User.cookie = urlConnection.getHeaderField("Set-Cookie");
                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null)
                        responseStrBuilder.append(inputStr);

                    return new JSONObject(responseStrBuilder.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(final JSONObject json) {
          try {
              if (json.getString("login_result") == "success") {
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
