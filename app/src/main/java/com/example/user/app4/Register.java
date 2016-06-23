package com.example.user.app4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity {

    private EditText usernameIn;
    private EditText emailIn;
    private EditText passwordIn;
    private EditText nameIn;
    private Button signupButton;
    private TextView loginLink;
    private RadioGroup raGroup;
    private UserRegisterTask mAuthTask;
    /*
    name oncreate
    desc the func initialize all needed vars
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initiate activity vars
        setContentView(R.layout.activity_register);
        usernameIn = (EditText) findViewById(R.id.input_username);
        passwordIn = (EditText) findViewById(R.id.input_password);
        nameIn = (EditText) findViewById(R.id.input_namer);
        emailIn = (EditText) findViewById(R.id.input_email);
        raGroup = (RadioGroup) findViewById(R.id.radioGroup);
        signupButton = (Button) findViewById(R.id.btn_signup);
        loginLink = (TextView) findViewById(R.id.link_login);
        //set signup btn
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

    /*
    the func tries to sign you up
     */
    public void signup() {
        //if input was wrong
        if (!validate()) {
            onSignupFailed();
            return;
        }
        //else sign up!
        signupButton.setEnabled(false);
        //get vars
        String name = usernameIn.getText().toString();
        String email = emailIn.getText().toString();
        String password = passwordIn.getText().toString();
        String pvtName = nameIn.getText().toString();
        int id = raGroup.getCheckedRadioButtonId();
        String iconNumber = "";
        if (id == R.id.btn_icon1) {
            iconNumber = "1";
        }else if (id == R.id.btn_icon2) {
            iconNumber = "2";
        }else if (id == R.id.btn_icon3) {
            iconNumber = "3";
        }
        //try to sign up with the server
        mAuthTask = new UserRegisterTask(name,password,email,pvtName,iconNumber);
        mAuthTask.execute();
    }


    /*
    the func transfer you on if the sign up was successful
     */
    public void onSignupSuccess() {
        String name = usernameIn.getText().toString();
        String password = passwordIn.getText().toString();
        //save it to SharedPreferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", name);
        editor.putString("password", password);
        editor.commit();
        signupButton.setEnabled(true);
        //set new activity
        Intent in = new Intent(Register.this, Chat.class);
        startActivity(in);
        finish();
    }

    /*
    the func notifies user if sign up was wrong
     */
    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), R.string.regFailed, Toast.LENGTH_LONG).show();
        signupButton.setEnabled(true);
    }

    /*
    the func validate all fields in activity
     */
    public boolean validate() {
        boolean valid = true;
        String name = usernameIn.getText().toString();
        String email = emailIn.getText().toString();
        String password = passwordIn.getText().toString();
        String namePvt = nameIn.getText().toString();
        //for each field-if it is empty set an err msg
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

    /*
    this class is in charch on the communication woth the server
     */
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

        /*
         namedoInBackground
         desc:send the post request!!and return the json
        */
        @Override
        protected JSONObject doInBackground(Void... params) {
            PostMsg pm = new PostMsg(User.address+"Register",this.map);
            return pm.sendPostMsg();
        }

        /*
         name onPostExecute
         desc: take the json and act according to json answer
         */
        @Override
        protected void onPostExecute(final JSONObject json) {
            try {
                //get answer and determine if sign up was good or not
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


