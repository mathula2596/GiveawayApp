package com.mis.givewayapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class BuyerDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE = 1;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView default_email, default_user;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;


    private View headerView;

    private CircleImageView profileImageView;
    private DocumentReference documentReference;

    private RecyclerView recyclerView;
    private CardRecycleViewAdaptor adaptor;
    private ArrayList<CardView> cardViewArrayList;

    private CardRecycleViewAdaptor.RecyclerViewClickListener listener;

    private String currentState = "empty";
    private FusedLocationProviderClient fusedLocationProviderClient;

    private FloatingActionButton search,clear;
    private TextInputLayout search_state,autoCompleteTextView;
    String searchValue="";
    Task<QuerySnapshot> query;

    ModalClass modalClass;

    private String [] categoryItems = {"Electronic","Furniture","Stationary","Grocery","Kitchen " +
            "Appliances","Food","Clothes","Home Appliances","Toys","Laundry Appliances"};

    private AutoCompleteTextView autoCompleteCategory;
    private RadioButton radioState, radioCategory;
    private ArrayAdapter<String> arrayAdapter;

    private String newState ="";
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private Float dbPrice;
    private String priceFormatted;

    private final String[] urlLinks = {""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_dashboard);
        //assigning the element to the particular view format//
        navigationView = findViewById(R.id.navigation_view);
        headerView = navigationView.getHeaderView(0);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        //assigning the title to toolbar//
        toolbar.setTitle(getString(R.string.app_name) + " - Latest Products");
        setSupportActionBar(toolbar);
        //bring the navigation view to front of the screen//
        navigationView.bringToFront();
        //make the drawer functioning//
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout
                ,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.home);

        default_user = headerView.findViewById(R.id.default_user);
        default_email = headerView.findViewById(R.id.default_email);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        profileImageView = headerView.findViewById(R.id.dp);

        documentReference = firebaseFirestore.collection(
                "Users").document(firebaseAuth.getCurrentUser().getUid());

        modalClass = new ModalClass();
        modalClass.getUserInfo(default_user,default_email,profileImageView);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        search_state = findViewById(R.id.search_state);
        search = findViewById(R.id.search);
        clear = findViewById(R.id.clear);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
            }
        });

        autoCompleteCategory = findViewById(R.id.category);
        arrayAdapter = new ArrayAdapter<>(this,R.layout.list_item,categoryItems);
        autoCompleteCategory.setAdapter(arrayAdapter);

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        autoCompleteCategory.setVisibility(View.GONE);
        autoCompleteTextView.setVisibility(View.GONE);


        radioState = findViewById(R.id.state_radio);
        radioCategory = findViewById(R.id.category_radio);

        searchBy();
        InitializeCardView();




    }
    private void searchBy() {


        radioCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(radioCategory.isChecked())
                {
                    search_state.setVisibility(View.GONE);
                    autoCompleteCategory.setVisibility(View.VISIBLE);
                    autoCompleteTextView.setVisibility(View.VISIBLE);
                }
                else
                {
                    search_state.setVisibility(View.VISIBLE);
                    autoCompleteCategory.setVisibility(View.GONE);
                    autoCompleteTextView.setVisibility(View.GONE);
                }

                createDataForCards("");
            }
        });

        radioState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(radioState.isChecked())
                {
                    search_state.setVisibility(View.VISIBLE);
                    autoCompleteCategory.setVisibility(View.GONE);
                    autoCompleteTextView.setVisibility(View.GONE);

                }
                else
                {
                    search_state.setVisibility(View.GONE);
                    autoCompleteCategory.setVisibility(View.VISIBLE);
                    autoCompleteTextView.setVisibility(View.VISIBLE);
                }
                createDataForCards("");

            }

        });
    }

    private void clear() {

        search_state.getEditText().setText("");
        newState ="";
        autoCompleteTextView.getEditText().setText("");
        autoCompleteTextView.setHint(getString(R.string.category));
        createDataForCards("search by state");


    }

    private void search() {
        ModalClass modalClass = new ModalClass();
        if(radioState.isChecked()) {
            if (modalClass.checkFieldEmpty(search_state)) {
                searchValue = search_state.getEditText().getText().toString();


            }
        }
        if(radioCategory.isChecked()) {
            if (modalClass.checkDropDownEmpty(autoCompleteCategory)) {
                searchValue = autoCompleteCategory.getText().toString();
            }
        }
        createDataForCards(searchValue);
    }
    private void getLastLocation(ModalClass.Callback callback) {

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

        ActivityCompat.requestPermissions(BuyerDashboard.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
        ActivityCompat.requestPermissions(BuyerDashboard.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_CODE);


    }


    private void InitializeCardView() {
        setOnClickListener();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardViewArrayList = new ArrayList<>();

        adaptor = new CardRecycleViewAdaptor(this, cardViewArrayList,listener);

        createDataForCards("");
        recyclerView.setAdapter(adaptor);



    }

    //https://www.youtube.com/watch?v=vBxNDtyE_Co
    private void setOnClickListener() {
        listener = new CardRecycleViewAdaptor.RecyclerViewClickListener() {
            @Override
            public void onCLick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(),SingleProduct.class);
                intent.putExtra("documentId",cardViewArrayList.get(position).getDocumentId());
                startActivity(intent);
            }
        };
    }




    @Override
    protected void onStart() {
        super.onStart();
    }

    //https://www.youtube.com/watch?v=64jZNbnkS7c
    private void createDataForCards(String state) {

        if(state !="search by state")
        {
            newState = state;
        }


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();



        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy");
        String currentDate = date.format(new Date());

        if(radioState.isChecked() || state.equals("search by state"))
        {

            Log.d("radio1", "createDataForCards: "+"radioState");
            getLastLocation(value -> {

                if(newState!="")
                {
                    query =
                            db.collection("Products").whereEqualTo("status", "Open to give").whereEqualTo("userState",newState.trim()).orderBy("dateCreated", Query.Direction.DESCENDING).get();
                }
                else
                {

                    query =
                            db.collection("Products").whereEqualTo("status", "Open to give").whereEqualTo(
                                    "userState",value.trim()).orderBy("dateCreated",
                                    Query.Direction.DESCENDING).get();

                    queryRunner(query);

                    query = db.collection("Products").whereEqualTo("status", "Open to give").whereNotEqualTo(
                            "userState",value.trim()).orderBy("userState").orderBy("dateCreated",
                            Query.Direction.DESCENDING).get();
                }

                queryRunner(query);

            });
        }
        else if(radioCategory.isChecked()) {

            String selectedCategory = autoCompleteCategory.getText().toString();

            query = db.collection("Products").whereEqualTo("status", "Open to give").whereEqualTo("category", selectedCategory).orderBy("dateCreated", Query.Direction.DESCENDING).get();

            queryRunner(query);

        }
    }

    private void queryRunner(Task<QuerySnapshot> query)
    {
        query.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    cardViewArrayList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        documentReference = firebaseFirestore.collection(
                                "Users").document(document.get(
                                "userId").toString());


                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(document.get("imageLink").toString() != "")
                                {
                                    urlLinks[0] = document.get("imageLink").toString();
                                    String [] imageUrl =
                                            urlLinks[0] .replace("[", "").replace("]", "").split(", ");

                                    dbPrice = Float.parseFloat(document.getString("price"));
                                    decimalFormat.setMaximumFractionDigits(2);
                                    priceFormatted = decimalFormat.format(dbPrice);

                                    CardView cardView = new CardView(document.getString(
                                            "title")
                                            ,document.getString("description"),
                                            "£ "+priceFormatted,
                                            documentSnapshot.getString("firstname"),
                                            document.getString("dateCreated"),
                                            imageUrl[0],document.getId(),
                                            document.getString("userState"));

                                    cardViewArrayList.add(cardView);
                                    adaptor.notifyDataSetChanged();
                                }

                            }
                        });

                    }
                } else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
            }
        });
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
                startActivity(new Intent(getApplicationContext(),BuyerDashboard.class));
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