package com.mis.givewayapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements LocationListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int RESULT_OK = 1;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView default_email, default_user, userRole;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private String fullName = "";
    private View headerView, root;

    private CircleImageView profileImageView,dpImageView;
    private Button profileChangeBtn, saveButton, updateProfile;

    //private DatabaseReference databaseReference;

    private Uri imageUri;
    private String myUri="";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicRef;

    private DocumentReference documentReference;
    private TextInputLayout firstname, lastname, mobile, email, password, address, city, postcode
            , country,state;

    private LocationManager locationManager;

    private boolean valid = false;
    private boolean validMobile = false;
    private boolean validPassword = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root =  inflater.inflate(R.layout.fragment_profile, container, false);

        firstname = root.findViewById(R.id.firstname);
        lastname = root.findViewById(R.id.lastname);
        mobile = root.findViewById(R.id.mobile);
        email = root.findViewById(R.id.email);
        password = root.findViewById(R.id.password);
        userRole = root.findViewById(R.id.userRole);
        address = root.findViewById(R.id.address);
        city = root.findViewById(R.id.city);
        postcode = root.findViewById(R.id.postcode);
        country = root.findViewById(R.id.country);
        state = root.findViewById(R.id.state);
        password = root.findViewById(R.id.password);

        updateProfile = root.findViewById(R.id.update_profile);

        //https://www.youtube.com/watch?v=TnYXQHvuPIw
        //Location
        grantPermission();
        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationEnabled();
        getLocation();


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();



        storageProfilePicRef = FirebaseStorage.getInstance().getReference().child("ProfilePic");

        documentReference = firebaseFirestore.collection(
                "Users").document(firebaseAuth.getCurrentUser().getUid());

        profileImageView = root.findViewById(R.id.profile_image);

//        dpImageView = headerView.findViewById(R.id.dp);

        profileChangeBtn = root.findViewById(R.id.change_profile_btn);


        saveButton=root.findViewById(R.id.btnSave);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadProfileImage();
            }
        });
        profileChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent inte =  CropImage.activity().setAspectRatio(1,1).getIntent(getContext());
                startActivityForResult(inte,CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);

            }




        });




        getUserInfo();

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isValidMobile(mobile) && checkField(firstname) && checkField(lastname) && checkFieldEmpty(address)&& checkField(city)&& checkField(state)&& checkFieldEmpty(postcode)&& checkField(country)){
                    documentReference.update("firstname",firstname.getEditText().getText().toString());
                    documentReference.update("lastname",lastname.getEditText().getText().toString());
                    documentReference.update("mobile",mobile.getEditText().getText().toString());
                    documentReference.update("address",address.getEditText().getText().toString());
                    documentReference.update("city",city.getEditText().getText().toString());
                    documentReference.update("state",state.getEditText().getText().toString());
                    documentReference.update("postcode",postcode.getEditText().getText().toString());
                    documentReference.update("country",country.getEditText().getText().toString());
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Profile Update Successful!",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mobile.setError("Please enter the correct mobile number");
                }


                if(!password.getEditText().getText().toString().isEmpty())
                {
                    validPassword = isValidPassword(password);
                    if(validPassword)
                    {
                        firebaseAuth.getCurrentUser().updatePassword(password.getEditText().getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getActivity().getApplicationContext(),"Password Changed Successful!",
                                        Toast.LENGTH_SHORT).show();
                                firebaseAuth.signOut();
                                startActivity(new Intent(getActivity().getApplicationContext(),MainActivity.class));
                                getActivity().finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity().getApplicationContext(),"Login again to reset the password"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                firebaseAuth.signOut();
                                startActivity(new Intent(getActivity().getApplicationContext(),MainActivity.class));
                                getActivity().finish();
                            }
                        });
                    }
                    else
                    {
                        password.setError("Password should be more than 8 characters and contain Uppercase, Symbol and Number");

                    }



                }


            }
        });


        return root;
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d("TAG", "onActivityResult: "+requestCode);
//        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null)
//        {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            imageUri = result.getUri();
//
//            profileImageView.setImageURI(imageUri);
//        }
//        else
//        {
//            Toast.makeText(getActivity().getApplicationContext(),"Error, Try again!",Toast.LENGTH_LONG).show();
//        }
//    }


    protected boolean isValidPassword(TextInputLayout textField) {
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

    protected boolean isValidMobile(TextInputLayout textField){
        validMobile = false;
        String mobile = textField.getEditText().getText().toString();
        mobile = "+44"+mobile;
//        Log.d("TAG", "isValidMobile: mobile"+mobile);
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
    protected boolean checkField(TextInputLayout textField){
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
    protected boolean checkFieldEmpty(TextInputLayout textField){
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

    private void locationEnabled() {

        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(getActivity().getApplicationContext())
                    .setTitle("Enable GPS Service")
                    .setMessage("We need your GPS location to show your address.")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
    void getLocation() {
        try {
            locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, (LocationListener) this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private void grantPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }





    private void uploadProfileImage() {

        /*final ProgressDialog progressDialog =
                new ProgressDialog(getActivity().getApplicationContext());
        progressDialog.setTitle("Set your profile");
        progressDialog.setMessage("Please wait, while we are setting your data");

        progressDialog.show();*/

        if(imageUri !=null)
        {
            final StorageReference fileRef =
                    storageProfilePicRef.child(firebaseAuth.getCurrentUser().getUid() + ".jpg");
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        Uri downloadUrl =  task.getResult();
                        myUri = downloadUrl.toString();

                        documentReference.update("image",myUri);

                        //progressDialog.dismiss();
                        getUserInfo();
                    }
                }
            });
        }
        else
        {
           // progressDialog.dismiss();
            Toast.makeText(getActivity().getApplicationContext(),"Image not selected",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        getSetLocation(location);
    }

    protected void getSetLocation(Location location)
    {
        try {
            Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //address
                    try{
                        if(!documentSnapshot.getString("address").isEmpty())
                        {
                            address.getEditText().setText(documentSnapshot.getString("address"));
                        }
                    }
                    catch (Exception e)
                    {
                        address.getEditText().setText(addresses.get(0).getAddressLine(0));
                    }
                    //city
                    try{
                        if(!documentSnapshot.getString("city").isEmpty())
                        {
                            city.getEditText().setText(documentSnapshot.getString("city"));
                        }
                    }
                    catch (Exception e)
                    {
                        city.getEditText().setText(addresses.get(0).getLocality());
                    }
                    //state
                    try{
                        if(!documentSnapshot.getString("state").isEmpty())
                        {
                            state.getEditText().setText(documentSnapshot.getString("state"));
                        }
                    }
                    catch (Exception e)
                    {
                        state.getEditText().setText(addresses.get(0).getAdminArea());
                    }
                    //country
                    try{
                        if(!documentSnapshot.getString("country").isEmpty())
                        {
                            country.getEditText().setText(documentSnapshot.getString("country"));
                        }
                    }
                    catch (Exception e)
                    {
                        country.getEditText().setText(addresses.get(0).getCountryName());
                    }
                    //postcode
                    try{
                        if(!documentSnapshot.getString("postcode").isEmpty())
                        {
                            postcode.getEditText().setText(documentSnapshot.getString("postcode"));
                        }
                    }
                    catch (Exception e)
                    {
                        postcode.getEditText().setText(addresses.get(0).getPostalCode());
                    }


                }
            });

        }
        catch (Exception e) {

        }
    }

//    public interface Callback {
//        void onCallback(String value);
//    }
//
//    protected void getUserFullName(Profile.Callback callback) {
//        documentReference.get().addOnSuccessListener(documentSnapshot -> {
//
//            fullName = documentSnapshot.getString("firstname")
//                    + " " + documentSnapshot.getString("lastname");
//            callback.onCallback(fullName);
//
//        }).addOnFailureListener(e -> fullName = "");
//    }
    private void getUserInfo()
    {
//        if(!firebaseAuth.getCurrentUser().getUid().isEmpty()){
//            getUserFullName(value -> default_user.setText(value));
//        }
//        default_email.setText(firebaseAuth.getCurrentUser().getEmail());

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                firstname.getEditText().setText(documentSnapshot.getString("firstname"));
                lastname.getEditText().setText(documentSnapshot.getString("lastname"));
                email.getEditText().setText(documentSnapshot.getString("email"));
                mobile.getEditText().setText(documentSnapshot.getString("mobile"));
                userRole.setText(documentSnapshot.getString("userRole"));
                try{
                    if(!documentSnapshot.getString("address").isEmpty())
                    {
                        address.getEditText().setText(documentSnapshot.getString("address"));
                    }
                }
                catch (Exception e)
                {

                }
                //city
                try{
                    if(!documentSnapshot.getString("city").isEmpty())
                    {
                        city.getEditText().setText(documentSnapshot.getString("city"));
                    }
                }
                catch (Exception e)
                {
                }
                //state
                try{
                    if(!documentSnapshot.getString("state").isEmpty())
                    {
                        state.getEditText().setText(documentSnapshot.getString("state"));
                    }
                }
                catch (Exception e)
                {
                }
                //country
                try{
                    if(!documentSnapshot.getString("country").isEmpty())
                    {
                        country.getEditText().setText(documentSnapshot.getString("country"));
                    }
                }
                catch (Exception e)
                {
                }
                //postcode
                try{
                    if(!documentSnapshot.getString("postcode").isEmpty())
                    {
                        postcode.getEditText().setText(documentSnapshot.getString("postcode"));
                    }
                }
                catch (Exception e)
                {
                }

                if(documentSnapshot.getString("image") != null)
                {
                   // Picasso.get().load(documentSnapshot.getString("image")).into(dpImageView);
                    Picasso.get().load(documentSnapshot.getString("image")).into(profileImageView);


                }

            }
        });

    }

}