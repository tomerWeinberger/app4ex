package com.example.user.app4;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Register extends AppCompatActivity {

    private EditText usernameIn;
    private EditText emailIn;
    private EditText passwordIn;
    private EditText nameIn;
    private Button signupButton;
    private TextView loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        usernameIn = (EditText) findViewById(R.id.input_username);
        passwordIn = (EditText) findViewById(R.id.input_password);
        nameIn = (EditText) findViewById(R.id.input_namer);
        emailIn = (EditText) findViewById(R.id.input_email);

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
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }

        signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(Register.this,
                R.style.AppTheme_PopupOverlay);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = usernameIn.getText().toString();
        String email = emailIn.getText().toString();
        String password = passwordIn.getText().toString();
        String pvtName = nameIn.getText().toString();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    public void onSignupSuccess() {
        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
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


