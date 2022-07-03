package com.mis.givewayapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private TextInputLayout email;
    private Button resetPassword, login;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        email = findViewById(R.id.email);
        resetPassword = findViewById(R.id.reset_password);
        login = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar);
        firebaseAuth = FirebaseAuth.getInstance();

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetForgotPassword();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });
    }

    private void resetForgotPassword() {
        ModalClass modalClass = new ModalClass();
        if(modalClass.checkFieldEmpty(email))
        {
            if(modalClass.emailValidator(email))
            {
                String emailAddress = email.getEditText().getText().toString().trim();
                progressBar.setVisibility(View.VISIBLE);

                firebaseAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.INVISIBLE);

                        if(task.isSuccessful())
                        {
                            Toast.makeText(ForgotPassword.this,"Check your email to reset the password!",
                                    Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            Toast.makeText(ForgotPassword.this,"Sorry, Try again!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ForgotPassword.this,e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }


        }

    }

}