package com.example.user.app4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class Welcome extends AppCompatActivity {
    private TextView msg;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        msg = (TextView) findViewById(R.id.msg);
        startMsg();
    }

    /*
    name startmsg
    desc:start msgs in new threads
     */
    private void startMsg() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> msgArr = new ArrayList<String>();
                msgArr.add(getString(R.string.first_msg));
                msgArr.add(getString(R.string.sec_msg));
                msgArr.add(getString(R.string.third_msg));
                msgArr.add(getString(R.string.fourth_msg));
                for (String str : msgArr) {
                    text = str;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            msg.setText(text);
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                directTo();
            }
        });
        t.start();
    }

    /*
    name directTo
    desc: according to our SharedPreferences the func directs us
        to our desired page
     */
    private void directTo() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        Intent intent;
        //according to your SharedPreferences we know to which activity to transfer you
        if (settings.getString("firstTime", "Yes").equals("Yes")) {
            editor.putString("firstTime", "No");
            editor.commit();
            intent = new Intent(Welcome.this, Explenation.class);
        } else if (settings.getString("username", "").equals("")) {
            intent = new Intent(Welcome.this, Login.class);
        } else {
            new LoginExist(settings.getString("username", ""), settings.getString("password", ""), Welcome.this).execute();
            return;
        }
        startActivity(intent);
        finish();
    }
}