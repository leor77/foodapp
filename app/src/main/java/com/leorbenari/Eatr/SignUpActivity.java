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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth.AuthStateListener mAuthListener;

    private Button registerButton;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignIn;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);


        registerButton = (Button) findViewById(R.id.registerButton);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        textViewSignIn = (TextView)findViewById(R.id.textViewSignIn);
        progressDialog = new ProgressDialog(this);

        registerButton.setOnClickListener(this);
        textViewSignIn.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

//        if (mAuth.getCurrentUser() != null) {
//            finish();
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
//        }

    }

    private void registerUser(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering User...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, "Registered Successfully",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));

                        } else {
                            Toast.makeText(SignUpActivity.this, "Could not Register...Please try again.",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }
                    }
                });

    }

    @Override
    public void onClick(View view) {
        if (view == registerButton){
            registerUser();
        }
        if (view == textViewSignIn){
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

}
