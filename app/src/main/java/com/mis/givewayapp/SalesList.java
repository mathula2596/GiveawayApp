package com.mis.givewayapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SalesList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView default_email, default_user,list_product_title;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private String fullName,userRole;
    private View headerView;


    private CircleImageView profileImageView;
    private DocumentReference documentReference;

    private RecyclerView recyclerView;
    private CardRecycleViewAdaptor adaptor;
    private ArrayList<CardView> cardViewArrayList;

    private CardRecycleViewAdaptor.RecyclerViewClickListener listener;

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private Float dbPrice;
    private String priceFormatted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_list);

        InitializeCardView();
        navigationView = findViewById(R.id.navigation_view);
        headerView = navigationView.getHeaderView(0);

        default_user = headerView.findViewById(R.id.default_user);
        default_email = headerView.findViewById(R.id.default_email);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        profileImageView = headerView.findViewById(R.id.dp);

        drawerLayout = findViewById(R.id.drawer_layout);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name) + " - Sold Out Products");
        setSupportActionBar(toolbar);

        navigationView.bringToFront();

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout
                ,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.sales_list);

        documentReference = firebaseFirestore.collection(
                "Users").document(firebaseAuth.getCurrentUser().getUid());

        ModalClass modalClass = new ModalClass();
        modalClass.getUserInfo(default_user,default_email,profileImageView);

    }

    private void InitializeCardView() {
        setOnClickListener();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardViewArrayList = new ArrayList<>();

        adaptor = new CardRecycleViewAdaptor(this, cardViewArrayList,listener);

        createDataForCards();
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
    private void createDataForCards() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        ModalClass modalClass = new ModalClass();
        modalClass.getUserRole(value -> {
            userRole = value;
            final String[] urlLinks = {""};
            Task<QuerySnapshot> query;
            if(userRole.equals("Seller"))
            {
                query = db.collection("Products").whereEqualTo("userId",
                        firebaseAuth.getCurrentUser().getUid()).whereEqualTo("status","Sold out").orderBy(
                                "dateCreated",
                    Query.Direction.DESCENDING).get();

            }
            else
            {
                //CardView cardView;
                query = db.collection("Products").whereEqualTo("status","Sold out").orderBy(
                        "dateCreated", Query.Direction.DESCENDING).get();
            }
            query.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            documentReference = firebaseFirestore.collection(
                                    "Users").document(document.get(
                                    "userId").toString());
                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (document.get("imageLink").toString() != "") {
                                        urlLinks[0] = document.get("imageLink").toString();
                                        String[] imageUrl =
                                                urlLinks[0].replace("[", "").replace("]", "").split(", ");

                                        dbPrice = Float.parseFloat(document.getString("price"));
                                        decimalFormat.setMaximumFractionDigits(2);
                                        priceFormatted = decimalFormat.format(dbPrice);

                                        CardView cardView = new CardView(document.getString(
                                                "title")
                                                , document.getString("description"),
                                                "£ " + priceFormatted,
                                                documentSnapshot.getString("firstname"),
                                                document.getString("dateCreated"),
                                                imageUrl[0], document.getId(),
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