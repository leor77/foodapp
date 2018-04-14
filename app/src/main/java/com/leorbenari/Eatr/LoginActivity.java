package com.leorbenari.Eatr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button signInButton;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignUp;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton = (Button) findViewById(R.id.signInButton);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textViewSignUp = (TextView) findViewById(R.id.textViewSignUp);
        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        signInButton.setOnClickListener(this);
        textViewSignUp.setOnClickListener(this);
    }

    private void signInUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Signed in Successfully",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));

                        } else {
                            Toast.makeText(LoginActivity.this, "Could not Sign in...Please try again.",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }
                    }
                });
    }

    @Override
    public void onClick (View view){
        if (view == signInButton) {
            signInUser();
        }
        if (view == textViewSignUp) {
            startActivity(new Intent(this, SignUpActivity.class));
        }
    }
}

