package com.mis.givewayapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewProduct extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE = 1;
    private String [] conditionItems = {"New","Used"};
    private String [] negotiableItems = {"Yes","No"};
    private String [] statusItems = {"Open to give","Sold out"};
    private String [] categoryItems = {"Electronic","Furniture","Stationary","Grocery","Kitchen " +
            "Appliances","Food","Clothes","Home Appliances","Toys","Laundry Appliances"};
    private String [] freeItems = {"Yes","No"};

    private String currentState = "empty";
    private FusedLocationProviderClient fusedLocationProviderClient;

    private AutoCompleteTextView autoCompleteCondition, autoCompleteNegotiable, autoCompleteStatus,
            autoCompleteCategory,autoCompleteFree;

    private ArrayAdapter<String> arrayAdapter;

    private TextInputLayout price,title,description;

    private static final int IMAGE_CODE = 1;
    private Button selectImage,publish;


    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView default_email, default_user, userRole, image_error;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private String fullName = "";
    private View headerView;

    private CircleImageView profileImageView;
    private DatabaseReference databaseReference;
    private DocumentReference documentReference,documentReferenceUser;

    private StorageReference storageReference;
    private FirebaseUser user;
    private ProgressDialog progressDialog;
    private int uploadCount = 0;
    private String docId;
    private String userState = "United Kingdom";


    private ArrayList<Uri> imageList = new ArrayList<>();
    private ArrayList<String> imageListName = new ArrayList<>();
    private ArrayList<SlideModel> sliderModels = new ArrayList<>();


    private ImageSlider image_slider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        autoCompleteCondition = findViewById(R.id.condition);
        arrayAdapter = new ArrayAdapter<>(this,R.layout.list_item,conditionItems);
        autoCompleteCondition.setAdapter(arrayAdapter);

        autoCompleteNegotiable = findViewById(R.id.negotiable);
        arrayAdapter = new ArrayAdapter<>(this,R.layout.list_item,negotiableItems);
        autoCompleteNegotiable.setAdapter(arrayAdapter);

        autoCompleteStatus = findViewById(R.id.status);
        arrayAdapter = new ArrayAdapter<>(this,R.layout.list_item,statusItems);
        autoCompleteStatus.setAdapter(arrayAdapter);

        autoCompleteCategory = findViewById(R.id.category);
        arrayAdapter = new ArrayAdapter<>(this,R.layout.list_item,categoryItems);
        autoCompleteCategory.setAdapter(arrayAdapter);

        autoCompleteFree = findViewById(R.id.free);
        arrayAdapter = new ArrayAdapter<>(this,R.layout.list_item,freeItems);
        autoCompleteFree.setAdapter(arrayAdapter);

        price = findViewById(R.id.price);
        selectImage = findViewById(R.id.select_image);
        publish = findViewById(R.id.publish);

        title = findViewById(R.id.title);
        description = findViewById(R.id.description);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel("notification","App" +
                    " Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this,"notification").setSmallIcon(R.drawable.logo).setContentTitle("New Product Added").setContentText("New Product Added for you");

        autoCompleteFree.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String item = adapterView.getItemAtPosition(position).toString();
                if(item.equals("Yes"))
                {
                    price.getEditText().setText("0.00");
                    autoCompleteNegotiable.setText(arrayAdapter.getItem(1).toString());
                }
                else
                {
                    autoCompleteNegotiable.setText("");
                    autoCompleteNegotiable.setAdapter(arrayAdapter);
                    price.getEditText().setText("");
                }
            }
        });


        storageReference = FirebaseStorage.getInstance().getReference();
        image_slider = findViewById(R.id.image_slider);


//        https://www.youtube.com/watch?v=y1zQ4Rubh40
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image_slider.setVisibility(View.VISIBLE);
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, IMAGE_CODE);

            }
        });

        navigationView = findViewById(R.id.navigation_view);
        headerView = navigationView.getHeaderView(0);

        default_user = headerView.findViewById(R.id.default_user);
        default_email = headerView.findViewById(R.id.default_email);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        user = firebaseAuth.getCurrentUser();


        drawerLayout = findViewById(R.id.drawer_layout);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name) + " - New Products");
        setSupportActionBar(toolbar);


        navigationView.bringToFront();
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout
                ,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.new_product);

        profileImageView = headerView.findViewById(R.id.dp);

        documentReference =
                firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid());

        ModalClass modalClass = new ModalClass();
        modalClass.getUserInfo(default_user,default_email,profileImageView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait, while we are setting your data");

        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(modalClass.checkFieldEmpty(title) && modalClass.checkFieldEmpty(description)&& modalClass.checkDropDownEmpty(autoCompleteFree)&& modalClass.checkDropDownEmpty(autoCompleteNegotiable)&& modalClass.checkDropDownEmpty(autoCompleteCondition)&& modalClass.checkDropDownEmpty(autoCompleteCategory)&& modalClass.checkDropDownEmpty(autoCompleteStatus) && imageList.size()>0) {

                    SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy");
                    String currentDateandTime = date.format(new Date());

                    documentReference = firebaseFirestore.collection(
                            "Products").document();
                    docId = documentReference.getId();

                    documentReferenceUser = firebaseFirestore.collection(
                            "Users").document(user.getUid());
                    documentReferenceUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            if(documentSnapshot.getString("state") != null)
                            {
                                Map<String, Object> productDetails = new HashMap<>();

                                productDetails.put("title", title.getEditText().getText().toString());
                                productDetails.put("description", description.getEditText().getText().toString());
                                productDetails.put("isItFree", autoCompleteFree.getText().toString());
                                productDetails.put("price", price.getEditText().getText().toString());
                                productDetails.put("negotiable", autoCompleteNegotiable.getText().toString());
                                productDetails.put("condition", autoCompleteCondition.getText().toString());
                                productDetails.put("category", autoCompleteCategory.getText().toString());
                                productDetails.put("status", autoCompleteStatus.getText().toString());
                                productDetails.put("userId", user.getUid());

                                userState = documentSnapshot.getString("state");
                                productDetails.put("userState",userState);

                                productDetails.put("dateCreated", currentDateandTime);
                                productDetails.put("dateUpdated", currentDateandTime);
                                productDetails.put("imageLink", "");

                                documentReference.set(productDetails);
                            }
                            else
                            {
                                getLastLocation(value -> {

                                    Map<String, Object> productDetails = new HashMap<>();

                                    productDetails.put("title", title.getEditText().getText().toString());
                                    productDetails.put("description", description.getEditText().getText().toString());
                                    productDetails.put("isItFree", autoCompleteFree.getText().toString());
                                    productDetails.put("price", price.getEditText().getText().toString());
                                    productDetails.put("negotiable", autoCompleteNegotiable.getText().toString());
                                    productDetails.put("condition", autoCompleteCondition.getText().toString());
                                    productDetails.put("category", autoCompleteCategory.getText().toString());
                                    productDetails.put("status", autoCompleteStatus.getText().toString());
                                    productDetails.put("userId", user.getUid());

                                    userState = value;

                                    productDetails.put("userState",userState);

                                    productDetails.put("dateCreated", currentDateandTime);
                                    productDetails.put("dateUpdated", currentDateandTime);
                                    productDetails.put("imageLink", "");

                                    documentReference.set(productDetails);
                                });
                            }

                            storageReference = storageReference.child("ProductImage").child(user.getUid());


                            for (uploadCount = 0; uploadCount < imageList.size(); uploadCount++) {
                                Uri individualImage = imageList.get(uploadCount);


                                StorageReference imageName =
                                        storageReference.child("image" + individualImage.getLastPathSegment());

                                imageName.putFile(individualImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String url = String.valueOf(uri);
                                                imageListName.add(url);
                                                StoreLink(imageListName, docId);
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("TAG", "onFailure: "+e.getMessage());
                                    }
                                });
                            }

                            Toast.makeText(NewProduct.this, "Published Successfully",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    Intent refresh = new Intent(getApplicationContext(), NewProduct.class);
                    startActivity(refresh);


                }
                else
                {
                    if(imageList.size() <=0)
                    {
                        image_error = findViewById(R.id.image_error);
                        image_error.setVisibility(View.VISIBLE);
                        image_error.setText("Please select images");
                    }

                }

            }
        });


    }

    public void getLastLocation(ModalClass.Callback callback) {

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){


            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            if (location != null){
                                try {

                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                    List<Address> addresses = null;
                                    addresses = geocoder.getFromLocation(location.getLatitude(),
                                            location.getLongitude(),1);

                                    currentState = addresses.get(0).getSubAdminArea().toString();
                                    callback.onCallback(currentState);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                    });


        }else {

            askPermission();


        }

    }
    private void askPermission(){

        ActivityCompat.requestPermissions(NewProduct.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
        ActivityCompat.requestPermissions(NewProduct.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_CODE);


    }

    private void StoreLink(List <String> url, String id) {
        documentReference = firebaseFirestore.collection("Products").document(id);
        documentReference.update("imageLink", url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CODE && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {

                int totalitem = data.getClipData().getItemCount();

                for (int i = 0; i < totalitem; i++) {

                    Uri imageUri = data.getClipData().getItemAt(i).getUri();

                    imageList.add(imageUri);

                    sliderModels.add(new SlideModel(String.valueOf(imageUri)));


                }
                image_slider.setImageList(sliderModels,true);

            } else if (data.getData() != null) {
                Uri imageUri = data.getData();

                imageList.add(imageUri);
                sliderModels.add(new SlideModel(String.valueOf(imageUri)));

                image_slider.setImageList(sliderModels,true);
            }

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
        switch (item.getItemId())
        {
            case R.id.home:
                startActivity(new Intent(getApplicationContext(),SellerDashboard.class));
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

}