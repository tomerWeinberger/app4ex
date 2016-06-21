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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Register extends AppCompatActivity {

    private EditText usernameIn;
    private EditText emailIn;
    private EditText passwordIn;
    private EditText nameIn;
    private Button signupButton;
    private TextView loginLink;
    private RadioGroup raGroup;
    private UserRegisterTask mAuthTask;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        usernameIn = (EditText) findViewById(R.id.input_username);
        passwordIn = (EditText) findViewById(R.id.input_password);
        nameIn = (EditText) findViewById(R.id.input_namer);
        emailIn = (EditText) findViewById(R.id.input_email);
        raGroup = (RadioGroup) findViewById(R.id.radioGroup);
        signupButton = (Button) findViewById(R.id.btn_signup);
        loginLink = (TextView) findViewById(R.id.link_login);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(Register.this, Login.class);
                startActivity(in);
                finish();
            }
        });
        Button clear = (Button) findViewById(R.id.btn_clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameIn.getText().clear();
                passwordIn.getText().clear();
                nameIn.getText().clear();
                emailIn.getText().clear();

            }
        });

    }

    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }

        signupButton.setEnabled(false);

        String name = usernameIn.getText().toString();
        String email = emailIn.getText().toString();
        String password = passwordIn.getText().toString();
        String pvtName = nameIn.getText().toString();
        int id = raGroup.getCheckedRadioButtonId();
        String iconNumber = "";
        if (id == R.id.btn_icon1) {
            iconNumber = "1";
        }
        if (id == R.id.btn_icon2) {
            iconNumber = "2";
        }
        if (id == R.id.btn_icon3) {
            iconNumber = "3";
        }
        mAuthTask = new UserRegisterTask(name,password,email,pvtName,iconNumber);
        mAuthTask.execute();
    }


    public void onSignupSuccess() {
        String name = usernameIn.getText().toString();
        String password = passwordIn.getText().toString();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", name);
        editor.putString("password", password);
        editor.commit();
        signupButton.setEnabled(true);
        Intent in = new Intent(Register.this, Chat.class);
        startActivity(in);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Register failed", Toast.LENGTH_LONG).show();
        signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = usernameIn.getText().toString();
        String email = emailIn.getText().toString();
        String password = passwordIn.getText().toString();
        String namePvt = nameIn.getText().toString();

        if (name.isEmpty()) {
            usernameIn.setError("at least 1 characters");
            valid = false;
        } else {
            usernameIn.setError(null);
        }
        if (namePvt.isEmpty()) {
            nameIn.setError("at least 1 characters");
            valid = false;
        } else {
            nameIn.setError(null);
        }
        if (email.isEmpty()) {
            emailIn.setError("enter a valid email address");
            valid = false;
        } else {
            emailIn.setError(null);
        }

        if (password.isEmpty()) {
            passwordIn.setError("at least 1 characters");
            valid = false;
        } else {
            passwordIn.setError(null);
        }

        return valid;
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, JSONObject> {
        private HashMapParser map;
        UserRegisterTask(String name, String pass,String email,String pvtName, String icon) {
            this.map = new HashMapParser();
            map.put("userName",name);
            map.put("password",pass);
            this.map.put("logo", icon);
            this.map.put("mail", email);
            this.map.put("name", pvtName);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                URL url = new URL(User.address+"Register");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                try {
                    //send the POST out
                    PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                    out.print(this.map.Parse());
                    out.close();

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
                String s = json.getString("register_result");
                if (s.equals("success")) {
                    onSignupSuccess();
                } else {
                    onSignupFailed();
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


