package com.example.user.app4;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginExist extends AsyncTask<Void, Void, JSONObject> {
    private Activity parent;
    private HashMapParser map;

    LoginExist(String name, String pass, Activity parent) {
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