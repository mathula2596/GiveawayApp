package com.mis.givewayapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class Dashboard extends AppCompatActivity {

    //    https://www.youtube.com/watch?v=lt6xbth-yQo&list=PL5jb9EteFAOD8dlG1Il3fCiaVNPD_P7gh&index=4
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView default_email, default_user;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String fullName = "";
    private View headerView;
    private CircleImageView profileImageView;
    private DatabaseReference databaseReference;
    private DocumentReference documentReference;
    private Uri imageUri;
    private String myUri="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        //set user email and name to header

//        navigationView = findViewById(R.id.navigation_view);
//        headerView = navigationView.getHeaderView(0);
//
//        default_user = headerView.findViewById(R.id.default_user);
//        default_email = headerView.findViewById(R.id.default_email);
//
//        firebaseAuth = FirebaseAuth.getInstance();
//        firebaseFirestore = FirebaseFirestore.getInstance();
//        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
//
//        drawerLayout = findViewById(R.id.drawer_layout);
//
//        toolbar = findViewById(R.id.toolbar);
//
//        setSupportActionBar(toolbar);
//
//        navigationView.bringToFront();
//        toolbar.bringToFront();
//
//        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout
//                ,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
//        drawerLayout.addDrawerListener(actionBarDrawerToggle);
//        actionBarDrawerToggle.syncState();
//
//        navigationView.setNavigationItemSelectedListener(this);
//
//        navigationView.setCheckedItem(R.id.home);
//
//        profileImageView = headerView.findViewById(R.id.dp);
//
//        documentReference =
//                firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid());
//
//        getUserInfo();

    }

//    protected void getUserFullName(Callback callback) {
//
//        documentReference.get().addOnSuccessListener(documentSnapshot -> {
//
//            fullName = documentSnapshot.getString("firstname")
//                    + " " + documentSnapshot.getString("lastname");
//            callback.onCallback(fullName);
//
//
//
//        }).addOnFailureListener(e -> fullName = "");
//    }
//
//    @Override
//    public void onBackPressed() {
//        if(drawerLayout.isDrawerOpen(GravityCompat.START))
//        {
//            drawerLayout.closeDrawer(GravityCompat.START);
//        }
//        else
//        {
//            super.onBackPressed();
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
//            Toast.makeText(getApplicationContext(),"Error, Try again!",Toast.LENGTH_LONG).show();
//        }
//
//    }
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//    }
//    private void replaceFragment(Fragment fragment)
//    {
//        FragmentManager fragmentManager =getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.frameLayout,fragment);
//        fragmentTransaction.commit();
//    }
//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//
//        switch (item.getItemId())
//        {
//            case R.id.home:
//                startActivity(new Intent(getApplicationContext(),Dashboard.class));
//                break;
//            case R.id.new_product:
//                startActivity(new Intent(getApplicationContext(),NewProduct.class));
//                break;
//            case R.id.list_product:
//                break;
//            case R.id.sales_list:
//                break;
//            case R.id.profile_menu:
//               replaceFragment(new ProfileFragment());
//               // startActivity(new Intent(getApplicationContext(),Profile.class));
//                break;
//            case R.id.logout:
//                firebaseAuth.signOut();
//                Toast.makeText(this,"Logout Successful",Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(getApplicationContext(),MainActivity.class));
//                finish();
//
//                break;
//        }
//        drawerLayout.closeDrawer(GravityCompat.START);
//        return true;
//    }
//
//    public interface Callback {
//        void onCallback(String value);
//    }
//
//    private void getUserInfo()
//    {
//        if(!firebaseAuth.getCurrentUser().getUid().isEmpty()){
//            getUserFullName(value -> default_user.setText(value));
//        }
//        default_email.setText(firebaseAuth.getCurrentUser().getEmail());
//
//        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                if(documentSnapshot.getString("image") != null)
//                {
//                    Picasso.get().load(documentSnapshot.getString("image")).into(profileImageView);
//                }
//
//            }
//        });
//
//
//    }
}