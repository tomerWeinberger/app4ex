package com.example.user.app4;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class LoginExist extends AsyncTask<Void, Void, JSONObject> {
    private final String name;
    private final String pass;
    private Activity parent;

    LoginExist(String name, String pass, Activity parent) {
        this.name = name;
        this.pass = pass;
        this.parent = parent;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        try {
            URL url = new URL("http://172.18.13.47:8080/Server/MyLogin");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setReadTimeout(100000);
            urlConnection.setConnectTimeout(150000);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("userName", name);
            urlConnection.setRequestProperty("password", pass);
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
                parent.startActivity(new Intent(parent, Chat.class));
            } else {
                parent.startActivity(new Intent(parent, Login.class));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCancelled() {


    }
}