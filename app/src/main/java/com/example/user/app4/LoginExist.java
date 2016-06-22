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

/*


i think this is not used




 */
public class LoginExist extends AsyncTask<Void, Void, JSONObject> {
    private final String name;
    private final String pass;
    private Activity parent;
    private HashMapParser map;

    LoginExist(String name, String pass, Activity parent) {
        this.name = name;
        this.pass = pass;
        this.parent = parent;
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
                parent.startActivity(new Intent(parent, Chat.class));
                parent.finish();
            } else {
                parent.startActivity(new Intent(parent, Login.class));
                parent.finish();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCancelled() {


    }
}