package com.mis.givewayapp;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.dhaval2404.imagepicker.ImagePicker;
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

import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener  {

    //initailaizing the location manager class
    private LocationManager locationManager;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView default_email, default_user, userRole;

    // firebase auth//
    private FirebaseAuth firebaseAuth;
    // firebase firestore implementation//
    private FirebaseFirestore firebaseFirestore;


    // image view for the profile and the navigation drawer image//
    private CircleImageView profileImageView,dpImageView;
    private Button profileChangeBtn, saveButton, updateProfile;


    private String fullName = "";
    private View headerView;

    private Uri imageUri;
    private String myUri="";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicRef;

    private DocumentReference documentReference;
    private TextInputLayout firstname, lastname, mobile, email, password, address, city, postcode
            , country,state;



    private boolean validPassword = false;
    private String userRoleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ModalClass modalClass = new ModalClass();

        navigationView = findViewById(R.id.navigation_view);
        headerView = navigationView.getHeaderView(0);

        //https://www.youtube.com/watch?v=TnYXQHvuPIw
        //Location
        grantPermission();
        //declaring the location manager class
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationEnabled();
        getLocation();

        // assigning the view using the id//
        profileImageView = findViewById(R.id.profile_image);
        dpImageView = headerView.findViewById(R.id.dp);

        firstname = findViewById(R.id.firstname);
        lastname = findViewById(R.id.lastname);
        mobile = findViewById(R.id.mobile);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        userRole = findViewById(R.id.userRole);
        address = findViewById(R.id.address);
        city = findViewById(R.id.city);
        postcode = findViewById(R.id.postcode);
        country = findViewById(R.id.country);
        state = findViewById(R.id.state);
        password = findViewById(R.id.password);

        updateProfile = findViewById(R.id.update_profile);

        default_user = headerView.findViewById(R.id.default_user);
        default_email = headerView.findViewById(R.id.default_email);

        // declaring the firestore auth and firestore//
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // accessing the users table using the current user id//
        documentReference = firebaseFirestore.collection(
                "Users").document(firebaseAuth.getCurrentUser().getUid());

        drawerLayout = findViewById(R.id.drawer_layout);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name) + " - Profile");
        setSupportActionBar(toolbar);

        navigationView.bringToFront();

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout
                ,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.profile_menu);

        storageProfilePicRef = FirebaseStorage.getInstance().getReference().child("ProfilePic");

        profileChangeBtn = findViewById(R.id.change_profile_btn);

        saveButton=findViewById(R.id.btnSave);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadProfileImage();
            }
        });
        profileChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //select the image using the camera or gallery
                ImagePicker.with(Profile.this)
                        .crop()
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });

        getUserInfo();
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check validation for the field need to be filled and with correct format//
                if(modalClass.isValidMobile(mobile) && modalClass.checkField(firstname) && modalClass.checkField(lastname) && modalClass.checkFieldEmpty(address)&& modalClass.checkField(city)&& modalClass.checkField(state)&& modalClass.checkFieldEmpty(postcode)&& modalClass.checkField(country)){
                    // update the firestore database//
                    documentReference.update("firstname",firstname.getEditText().getText().toString());
                    documentReference.update("lastname",lastname.getEditText().getText().toString());
                    documentReference.update("mobile",mobile.getEditText().getText().toString());
                    documentReference.update("address",address.getEditText().getText().toString());
                    documentReference.update("city",city.getEditText().getText().toString());
                    documentReference.update("state",state.getEditText().getText().toString());
                    documentReference.update("postcode",postcode.getEditText().getText().toString());
                    documentReference.update("country",country.getEditText().getText().toString());
                    Toast.makeText(Profile.this,
                            "Profile Update Successful!",
                            Toast.LENGTH_SHORT).show();
                }
                // update the password on firestore database//
                if(!password.getEditText().getText().toString().isEmpty())
                {
                    validPassword = modalClass.isValidPassword(password);
                    if(validPassword)
                    {
                        firebaseAuth.getCurrentUser().updatePassword(password.getEditText().getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Profile.this,"Password Changed Successful!",
                                        Toast.LENGTH_SHORT).show();
                                firebaseAuth.signOut();
                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Profile.this,"Login again to reset the password"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                firebaseAuth.signOut();
                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                finish();
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
    }


    private void locationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
            new AlertDialog.Builder(Profile.this).setTitle("Enable GPS Service").setMessage("We need your GPS location to show your address.").setCancelable(false)
                    .setPositiveButton("Enable", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton("Cancel", null).show();
        }
    }
    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, (LocationListener) this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    //requesting the location permission from the users
    private void grantPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }


//    https://www.youtube.com/watch?v=kWeeWOlzEKM&t=90s
//    https://github.com/Dhaval2404/ImagePicker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //used to display the selected image on the image view//
        if (resultCode == Activity.RESULT_OK) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadProfileImage() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Set your profile");
        progressDialog.setMessage("Please wait, while we are setting your data");

        progressDialog.show();

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

                        progressDialog.dismiss();
                        getUserInfo();
                    }
                }
            });
        }
        else
        {
            progressDialog.dismiss();
            Toast.makeText(this,"Image not selected",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        ModalClass modalClass = new ModalClass();
        switch (item.getItemId())
        {
            case R.id.home:
                modalClass.getUserRole(value -> {
                    userRoleText=value;
                    if(userRoleText.equals("Seller"))
                    {
                        startActivity(new Intent(getApplicationContext(),SellerDashboard.class));
                    }
                    else
                    {
                        startActivity(new Intent(getApplicationContext(),BuyerDashboard.class));
                    }
                });
                break;
            case R.id.new_product:
                startActivity(new Intent(getApplicationContext(),NewProduct.class));
                break;
            case R.id.list_product:
                startActivity(new Intent(getApplicationContext(),ListProducts.class));
                break;
            case R.id.sales_list:
                startActivity(new Intent(getApplicationContext(),SalesList.class));
                break;
            case R.id.profile_menu:
                startActivity(new Intent(getApplicationContext(),Profile.class));
                break;
            case R.id.logout:
                firebaseAuth.signOut();
                Toast.makeText(this,"Logout Successful",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();

                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        getSetLocation(location);
    }

    protected void getSetLocation(Location location)
    {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
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
                        state.getEditText().setText(addresses.get(0).getSubAdminArea());
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

    private void getUserInfo()
    {
        ModalClass modalClass = new ModalClass();
        if(!firebaseAuth.getCurrentUser().getUid().isEmpty()){
            modalClass.getUserFullName(value -> default_user.setText(value));
        }
        default_email.setText(firebaseAuth.getCurrentUser().getEmail());

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                firstname.getEditText().setText(documentSnapshot.getString("firstname"));
                lastname.getEditText().setText(documentSnapshot.getString("lastname"));
                email.getEditText().setText(documentSnapshot.getString("email"));
                mobile.getEditText().setText(documentSnapshot.getString("mobile"));
                userRole.setText(documentSnapshot.getString("userRole"));
                if(documentSnapshot.getString("userRole").equals("Buyer"))
                {
                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.buyer_menu);
                    navigationView.setCheckedItem(R.id.profile_menu);
                }
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
                    //load the image from the picker to the view//
                    Picasso.get().load(documentSnapshot.getString("image")).into(dpImageView);
                    Picasso.get().load(documentSnapshot.getString("image")).into(profileImageView);


                }

            }
        });

    }
}