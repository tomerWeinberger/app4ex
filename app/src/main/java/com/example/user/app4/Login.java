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
import android.widget.TextView;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    private EditText nameText;
    private EditText passwordText;
    private Button loginButton;
    private Button regButton;

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


        String email = nameText.getText().toString();
        String password = passwordText.getText().toString();
        onLoginSuccess();


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
        Intent in = new Intent(Login.this,Chat.class);
        in.putExtra("name", nameText.getText().toString());
        startActivity(in);
        finish();
    }
}
