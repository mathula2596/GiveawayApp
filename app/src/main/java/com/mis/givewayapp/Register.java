package com.mis.givewayapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class Register extends AppCompatActivity {

    private TextInputLayout firstname, lastname, mobile, email, password;
    private Button login, register;

    private RadioButton seller,buyer;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firstname = findViewById(R.id.firstname);
        lastname = findViewById(R.id.lastname);
        mobile = findViewById(R.id.mobile);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        seller = findViewById(R.id.seller);
        buyer = findViewById(R.id.buyer);

        register = findViewById(R.id.register);
        login = findViewById(R.id.login);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        ModalClass modalClass = new ModalClass();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modalClass.checkField(firstname);
                modalClass.checkField(lastname);

                if(modalClass.checkFieldEmpty(email))
                {
                    modalClass.emailValidator(email);
                }
                if(modalClass.checkFieldEmpty(mobile))
                {
                    modalClass.isValidMobile(mobile);
                }
                if(modalClass.checkFieldEmpty(password))
                {
                    modalClass.isValidPassword(password);
                }

                if(modalClass.valid && modalClass.validPassword && modalClass.validEmail && modalClass.validMobile)
                {
                    firebaseAuth.createUserWithEmailAndPassword(email.getEditText().getText().toString(),password.getEditText().getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            DocumentReference documentReference = firebaseFirestore.collection(
                                    "Users").document(user.getUid());
                            Map<String, Object> userDetails = new HashMap<>();
                            userDetails.put("firstname", firstname.getEditText().getText().toString());
                            userDetails.put("lastname", lastname.getEditText().getText().toString());
                            userDetails.put("mobile", mobile.getEditText().getText().toString());
                            userDetails.put("email", email.getEditText().getText().toString());
                            String userRole = "Buyer";
                            if(seller.isChecked()) {
                                userRole = "Seller";
                            } else if(buyer.isChecked()) {
                                userRole = "Buyer";
                            }
                            userDetails.put("userRole", userRole);

                            documentReference.set(userDetails);
                            Toast.makeText(Register.this,"Registered Successfully",
                                    Toast.LENGTH_SHORT).show();
                            moveToLoginScreen();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Register.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

                }
                else
                {
                    Toast.makeText(Register.this,"Please correct the fields",Toast.LENGTH_SHORT).show();
                }

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToLoginScreen();
            }
        });


    }

    protected void moveToLoginScreen()
    {
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }


}