package com.teamo.app.unilife;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firebaseFirestore;

    private String current_user_id;

    private FloatingActionButton addPostBtn;

    private BottomNavigationView mainbottomNav;

    private HomeFragment homeFragment;
    private AccountFragment accountFragment;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle drawerToggle;

    private TextView navUserName, navUserMail;
    private CircleImageView userProfileImage;

    private void init(){
        mDrawerLayout = findViewById(R.id.anaDrawerLayout);
        mNavigationView = findViewById(R.id.anaNavView);
        mainToolbar = findViewById(R.id.main_toolbar);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();


    }

    private void handler(){

        //çekmece butonu
        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mainToolbar, R.string.opened, R.string.closed);
        //layout ile senkronize
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        //tıklanma olayları için navigation view a listener veriyoruz
        mNavigationView.setNavigationItemSelectedListener(this);

        if(currentUser != null){
            current_user_id = mAuth.getCurrentUser().getUid();
            updateNavBar();
        }

    }




    private void updateNavBar() {

        View header=mNavigationView.getHeaderView(0);
        navUserName = header.findViewById(R.id.navUsername);
        navUserMail = header.findViewById(R.id.navUsermail);
        userProfileImage = header.findViewById(R.id.navProfilePhoto);

        navUserMail.setText(currentUser.getEmail());

        firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    setNavData(userName, userImage);

                }

            }
        });


    }

    private void setNavData(String userName, String userImage) {
        navUserName.setText(userName);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.profilephoto);

        if(userProfileImage != null)
            Glide.with(this).applyDefaultRequestOptions(requestOptions).load(userImage).into(userProfileImage);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        handler();


        setSupportActionBar(mainToolbar);

        getSupportActionBar().setTitle("UniLife");

        if(mAuth.getCurrentUser() != null) {

            mainbottomNav = findViewById(R.id.mainBottomNav);

            // FRAGMENTS
            homeFragment = new HomeFragment();
            accountFragment = new AccountFragment();

            initializeFragment();

            mainbottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

                    switch (item.getItemId()) {

                        case R.id.bottom_action_home:

                            replaceFragment(homeFragment, currentFragment);
                            return true;

                        case R.id.bottom_action_account:

                            replaceFragment(accountFragment, currentFragment);
                            return true;

                        default:
                            return false;


                    }

                }
            });


            addPostBtn = findViewById(R.id.add_post_btn);
            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(newPostIntent);

                }
            });

        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){

            sendToLogin();

        } else {

            current_user_id = mAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){

                        if(!task.getResult().exists()){

                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();

                        }

                    } else {

                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                    }

                }
            });

        }

    }


    private void logOut() {


        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();

    }

    private void initializeFragment(){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container, homeFragment);
        fragmentTransaction.add(R.id.main_container, accountFragment);

        fragmentTransaction.hide(accountFragment);

        fragmentTransaction.commit();

    }

    private void replaceFragment(Fragment fragment, Fragment currentFragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == homeFragment){

            fragmentTransaction.hide(accountFragment);

        }

        if(fragment == accountFragment){

            fragmentTransaction.hide(homeFragment);

        }

        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()){
            case R.id.navExit:
                logOut();
                break;

            case R.id.navEditProfile:
                Intent toEdit = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(toEdit);
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }
}
