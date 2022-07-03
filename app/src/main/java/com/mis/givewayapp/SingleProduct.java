package com.mis.givewayapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SingleProduct extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView default_email, default_user, product_title,seller_name,price,date,
            description,status,condition,email,call,product_delete,product_edit,location,category,negotiable;
    private LinearLayout edit_button,contact_button,edit_product;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private String fullName = "";
    private View headerView;

    private CircleImageView profileImageView;
    private DocumentReference documentReference;
    private ImageSlider imageSlider;
    private Object SlideModel;

    private AutoCompleteTextView autoCompleteStatus;
    private String [] statusItems = {"Open to give","Sold out"};
    private ArrayAdapter<String> arrayAdapter;

    private Button update_product;

    private TextInputLayout edit_status_text;
    private String userRoleText;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);

        navigationView = findViewById(R.id.navigation_view);
        headerView = navigationView.getHeaderView(0);

        default_user = headerView.findViewById(R.id.default_user);
        default_email = headerView.findViewById(R.id.default_email);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        profileImageView = headerView.findViewById(R.id.dp);

        drawerLayout = findViewById(R.id.drawer_layout);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name) + " - Product Details");
        setSupportActionBar(toolbar);

        navigationView.bringToFront();

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout
                ,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.list_product);

        documentReference = firebaseFirestore.collection(
                "Users").document(firebaseAuth.getCurrentUser().getUid());


        //https://www.youtube.com/watch?v=unjG9xR-O9k

        // getUserInfo();
        imageSlider = findViewById(R.id.image_slider);
        String documentId = "";

        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            documentId = extras.getString("documentId");
        }

        getProductDetails(documentId);

        product_title = findViewById(R.id.product_title);
        seller_name = findViewById(R.id.seller_name);
        price = findViewById(R.id.price);
        date = findViewById(R.id.date);
        description = findViewById(R.id.description);
        status = findViewById(R.id.status);
        condition = findViewById(R.id.condition);
        email = findViewById(R.id.contact_seller_email);
        call = findViewById(R.id.contact_seller_call);
        location = findViewById(R.id.location);
        category = findViewById(R.id.category);
        negotiable = findViewById(R.id.negotiable);

        edit_button = findViewById(R.id.edit_button);
        contact_button = findViewById(R.id.contact_button);

        product_delete = findViewById(R.id.product_delete);
        product_edit = findViewById(R.id.product_edit);

        autoCompleteStatus = findViewById(R.id.edit_status);
        arrayAdapter = new ArrayAdapter<>(this,R.layout.list_item,statusItems);
        autoCompleteStatus.setAdapter(arrayAdapter);

        edit_status_text = findViewById(R.id.edit_status_text);

        update_product = findViewById(R.id.update_product);
        edit_product = findViewById(R.id.edit_product);

        edit_product.setVisibility(View.GONE);

        String finalDocumentId = documentId;
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSellerEmail(finalDocumentId);
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSellerCall(finalDocumentId);
            }
        });

        validateButton(documentId);

        String finalDocumentId1 = documentId;
        product_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProduct(finalDocumentId1);
            }
        });

        update_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                documentReference = firebaseFirestore.collection(
                        "Products").document(finalDocumentId1);
                documentReference.update("status",autoCompleteStatus.getText().toString());

                Toast toast = Toast.makeText(getApplicationContext(),"Status Updated " +
                                "Successful!",
                        Toast.LENGTH_SHORT);
                //toast.setMargin(50,50);
                toast.show();
                edit_product.setVisibility(View.GONE);
                refresh();

            }
        });

        product_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct(finalDocumentId1);
            }
        });

        ModalClass modalClass = new ModalClass();
        modalClass.getUserInfo(default_user,default_email,profileImageView);

        validateUserRole();
    }

    private void validateUserRole() {
        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.getString("userRole").equals("Buyer"))
                {
                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.buyer_menu);
                    navigationView.setCheckedItem(R.id.profile_menu);
                }

            }
        });

    }

    private void deleteProduct(String documentId)
    {
        firebaseFirestore.collection(
                "Products").document(documentId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(),"Deleted Successful!",
                        Toast.LENGTH_SHORT).show();
                refresh();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });


        edit_product.setVisibility(View.GONE);
    }

    private void editProduct(String documentId)
    {
        edit_product.setVisibility(View.VISIBLE);
        arrayAdapter = new ArrayAdapter<>(this,R.layout.list_item,statusItems);
        autoCompleteStatus.setAdapter(arrayAdapter);

        firebaseFirestore.collection("Products").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(statusItems[0].equals(documentSnapshot.getString("status")))
                {
                    autoCompleteStatus.setText(arrayAdapter.getItem(0),false);

                }
                else
                {
                    autoCompleteStatus.setText(arrayAdapter.getItem(1),false);
                }

            }
        });

    }
    private void validateButton(String documentId)
    {
        ModalClass modalClass = new ModalClass();
        modalClass.getUserRole(value -> {
            userRoleText=value;
            if(userRoleText.equals("Seller"))
            {
                final String[] userId = {""};

                firebaseFirestore.collection("Products").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        userId[0] = documentSnapshot.getString("userId");

                        if(firebaseAuth.getCurrentUser().getUid().toString().equals(userId[0]))
                        {
                            contact_button.setVisibility(View.GONE);
                            if(!documentSnapshot.getString("status").equals("Sold out"))
                            {
                                edit_button.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                edit_button.setVisibility(View.GONE);
                            }

                        }
                        else
                        {
                            if(!documentSnapshot.getString("status").equals("Sold out"))
                            {
                                contact_button.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                contact_button.setVisibility(View.GONE);
                            }

                            edit_button.setVisibility(View.GONE);

                        }



                    }
                });
            }
            else
            {
                final String[] userId = {""};

                firebaseFirestore.collection("Products").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if(!documentSnapshot.getString("status").equals("Sold out"))
                        {
                            contact_button.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            contact_button.setVisibility(View.GONE);
                        }

                        edit_button.setVisibility(View.GONE);


                    }
                });
            }
        });


    }
    private void getProductDetails(String documentId) {
        final String[] urlLinks = {""};
        firebaseFirestore.collection("Products").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.get("imageLink").toString() != "") {
                    urlLinks[0] = documentSnapshot.get("imageLink").toString();
                    String[] imageUrl =
                            urlLinks[0].replace("[", "").replace("]", "").split(", ");

                    ArrayList<SlideModel> sliderModels = new ArrayList<>();
                    for (int i = 0; i < imageUrl.length; i++) {
                        sliderModels.add(new SlideModel(imageUrl[i]));
                    }
                    imageSlider.setImageList(sliderModels, true);
                }


                firebaseFirestore.collection("Users").document(documentSnapshot.getString("userId")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        seller_name.setText(documentSnapshot.getString("firstname") +" " + documentSnapshot.getString("lastname"));


                    }
                });
                product_title.setText(documentSnapshot.getString("title"));
                Float dbPrice = Float.parseFloat(documentSnapshot.getString("price"));
                decimalFormat.setMaximumFractionDigits(2);
                String priceFormatted = decimalFormat.format(dbPrice);
                price.setText("£ "+priceFormatted);
                date.setText(documentSnapshot.getString("dateCreated"));
                description.setText(documentSnapshot.getString("description"));
                location.setText(documentSnapshot.getString("userState"));
                category.setText(documentSnapshot.getString("category"));

                if(documentSnapshot.getString("negotiable").equals("No"))
                {
                    negotiable.setText("Fixed Price");
                }
                else
                {
                    negotiable.setText("Negotiable");
                }

                if(documentSnapshot.getString("status").equals("Sold out"))
                {
                    status.setBackgroundColor(Color.RED);
                    status.setTextColor(Color.WHITE);

                    call.setEnabled(false);
                    email.setEnabled(false);

                }
                status.setText(documentSnapshot.getString("status"));
                condition.setText(documentSnapshot.getString("condition"));




            }
        });

    }

    private void getSellerEmail(String documentId) {

        //get the seller email address from firestore and set that to the button click
        firebaseFirestore.collection("Products").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                firebaseFirestore.collection("Users").document(documentSnapshot.getString("userId")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshotUser) {

                        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                        String emailList[] = { documentSnapshotUser.getString("email") };
                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, emailList);
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                                "Enquiry on " + documentSnapshot.getString("title"));
                        emailIntent.setType("text/html");
                        emailIntent.setPackage("com.google.android.gm");
                        startActivity(emailIntent);

                    }
                });
            }
        });
    }

    private void getSellerCall(String documentId) {

        //get the seller mobile number from firestore and set that to the button click
        firebaseFirestore.collection("Products").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                firebaseFirestore.collection("Users").document(documentSnapshot.getString("userId")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshotUser) {

                        String number = "+44"+documentSnapshotUser.getString("mobile") ;
                        Uri mobilenumber = Uri.parse("tel:"+number);
                        Intent intent = new Intent(Intent.ACTION_DIAL,mobilenumber);
                        startActivity(intent);
                    }
                });

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

    private void refresh()
    {
        Intent refresh = new Intent(getApplicationContext(), ListProducts.class);
        startActivity(refresh);//Start the same Activity
        finish();
    }
}