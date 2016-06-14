package com.example.user.app4;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class Register extends AppCompatActivity {

    private EditText usernameIn;
    private EditText emailIn;
    private EditText passwordIn;
    private EditText nameIn;
    private Button signupButton;
    private TextView loginLink;
    private int iconNumber;
    private RadioGroup raGroup;

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
        if (id == R.id.btn_icon1) {
            iconNumber = 1;
        }
        if (id == R.id.btn_icon2) {
            iconNumber = 2;
        }
        if (id == R.id.btn_icon3) {
            iconNumber = 3;
        }

    onSignupSuccess();

}


    public void onSignupSuccess() {
        signupButton.setEnabled(true);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", "Yes");
        editor.commit();
        Intent in = new Intent(Register.this, Chat.class);
        in.putExtra("name", usernameIn.getText().toString());
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
}


