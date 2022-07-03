package com.mis.givewayapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    private Button loginBtn,registerBtn;
    private TextInputLayout email, password;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private TextView forgotPassword;

    private String [] PERMISSIONS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        https://www.youtube.com/watch?v=y0gX4FD3nxk
        PERMISSIONS = new String[]{
            Manifest.permission.CALL_PHONE,
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if(!hasPermission(MainActivity.this,PERMISSIONS))
        {
            ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS,1);
        }
        ModalClass modalClass = new ModalClass();

        loginBtn = findViewById(R.id.login);
        registerBtn = findViewById(R.id.register);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        forgotPassword = findViewById(R.id.forgot_password);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToRegisterScreen();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(modalClass.checkFieldEmpty(email))
                {
                    modalClass.emailValidator(email);

                }
                modalClass.checkFieldEmpty(password);

                if(modalClass.valid && modalClass.validEmail)
                {
                    firebaseAuth.signInWithEmailAndPassword(email.getEditText().getText().toString(),password.getEditText().getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(MainActivity.this,"Login Successful!",
                                    Toast.LENGTH_LONG).show();
                            checkUserAccessRole(authResult.getUser().getUid());
                        }


                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetForgotPassword();
            }
        });

    }

    private void resetForgotPassword() {
        startActivity(new Intent(getApplicationContext(),ForgotPassword.class));
    }

    protected void checkUserAccessRole(String uid) {

        DocumentReference documentReference =
                firebaseFirestore.collection("Users").document(uid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.getString("userRole").equals("Buyer"))
                {
                    moveToBuyerScreen();
                }
                else if(documentSnapshot.getString("userRole").equals("Seller"))
                {
                    int fieldCount = documentSnapshot.getData().size();
                    if(fieldCount<11)
                    {
                        Toast.makeText(MainActivity.this,"Please update your profile to display your products with location",
                                Toast.LENGTH_LONG).show();
                    }
                    moveToSellerScreen();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onSuccess: Success"+e.getMessage());

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseAuth.getCurrentUser() != null)
        {
            checkUserAccessRole(firebaseAuth.getCurrentUser().getUid());
        }
    }

    private boolean hasPermission(Context context, String...PERMISSIONS)
    {
        if (context != null && PERMISSIONS != null)
        {
            for(String permission: PERMISSIONS)
            {
                if(ActivityCompat.checkSelfPermission(context,permission)!=PackageManager.PERMISSION_GRANTED)
                {
                    return  false;
                }
            }

        }
        return true;
    }

    protected void moveToRegisterScreen()
    {
        startActivity(new Intent(getApplicationContext(),Register.class));
        finish();
    }
    protected void moveToBuyerScreen()
    {
        startActivity(new Intent(getApplicationContext(),BuyerDashboard.class));
        finish();
    }

    protected void moveToSellerScreen()
    {
        startActivity(new Intent(getApplicationContext(),SellerDashboard.class));
        finish();
    }

}