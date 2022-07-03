package com.mis.givewayapp;

import android.util.Patterns;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModalClass {

    public FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    public DocumentReference documentReference;
    public FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    public String fullName,userRoleText = "";
    public boolean valid,validEmail,validPassword,validMobile =false;



    public void getUserInfo(TextView default_user, TextView default_email, ImageView profileImageView)
    {

        documentReference = firebaseFirestore.collection(
                "Users").document(firebaseAuth.getCurrentUser().getUid());
        if(!firebaseAuth.getCurrentUser().getUid().isEmpty()){
            getUserFullName(value -> default_user.setText(value));

        }

        default_email.setText(firebaseAuth.getCurrentUser().getEmail());

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.getString("image") != null)
                {
                    Picasso.get().load(documentSnapshot.getString("image")).into(profileImageView);
                }

            }
        });
    }

    public void getUserFullName(Callback callback) {
        DocumentReference documentReference = firebaseFirestore.collection(
                "Users").document(firebaseAuth.getCurrentUser().getUid());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                fullName = documentSnapshot.getString("firstname")
                        + " " + documentSnapshot.getString("lastname");
                callback.onCallback(fullName);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                fullName = "";
            }
        });
    }
    public interface Callback {
        void onCallback(String value);
    }

    public void getUserRole(Callback callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection(
                "Users").document(firebaseAuth.getCurrentUser().getUid());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                userRoleText = documentSnapshot.getString("userRole");
                callback.onCallback(userRoleText);
            }
        });
    }

    public boolean checkFieldEmpty(TextInputLayout textField){
        valid = false;
        if(!textField.getEditText().getText().toString().isEmpty())
        {
            textField.setError(null);
            valid = true;
        }
        else
        {
            textField.setError("Please fill the value");
            valid = false;
        }
        return valid;
    }
    public boolean isValidPassword(TextInputLayout textField) {
        validPassword = false;
        String password = textField.getEditText().getText().toString();
        Pattern pattern;
        Matcher matcher;
        final String PasswordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        pattern = Pattern.compile(PasswordPattern);
        matcher = pattern.matcher(password);

        if(matcher.matches()){
            textField.setError(null);
            validPassword = true;

        }else{
            textField.setError("Password should be more than 8 characters and contain Uppercase, Symbol and Number");
            validPassword = false;

        }
        return validPassword;

    }

    public boolean isValidMobile(TextInputLayout textField){
        validMobile = false;
        String mobile = textField.getEditText().getText().toString();
        mobile = "+44"+mobile;
        Pattern pattern;
        Matcher matcher;
        final String MobilePattern = "^\\+[0-9]{10,13}$";
        pattern = Pattern.compile(MobilePattern);
        matcher = pattern.matcher(mobile);

        if(matcher.matches()){
            textField.setError(null);
            validMobile = true;

        }else{
            textField.setError("Mobile number is not valid");
            validMobile = false;

        }
        return validMobile;
    }

    public boolean emailValidator(TextInputLayout textField){
        validEmail = false;
        String email = textField.getEditText().getText().toString();
        if(Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            textField.setError(null);
            validEmail = true;
        }
        else
        {
            textField.setError("Please fill the correct value");
            validEmail = false;
        }
        return validEmail;
    }

    public boolean checkField(TextInputLayout textField){
        valid = false;
        if(!textField.getEditText().getText().toString().isEmpty())
        {
            Pattern pattern;
            Matcher matcher;
            final String CharacterPattern = "[a-zA-Z ]+";
            pattern = Pattern.compile(CharacterPattern);
            matcher = pattern.matcher(textField.getEditText().getText().toString());

            if(matcher.matches()){
                textField.setError(null);
                valid = true;

            }else{
                textField.setError("Please enter the valid characters");
                validMobile = false;

            }

        }
        else
        {
            textField.setError("Please fill the value");
            valid = false;
        }
        return valid;
    }

    public boolean checkDropDownEmpty(AutoCompleteTextView autoCompleteTextView){
        valid = false;
        if(!autoCompleteTextView.getText().toString().isEmpty())
        {
            autoCompleteTextView.setError(null);
            valid = true;
        }
        else
        {
            autoCompleteTextView.setError("Please select the value");
            valid = false;
        }
        return valid;
    }



}
