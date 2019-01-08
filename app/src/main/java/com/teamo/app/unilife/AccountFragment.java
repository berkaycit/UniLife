package com.teamo.app.unilife;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private RecyclerView user_list_view;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private CircleImageView profileImage;
    private TextView profileName, profileBio;

    private Context context;

    public AccountFragment() {
    }

    private void init(View view){
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //user_list_view = view.findViewById(R.id.profile_list_view);
        profileImage = view.findViewById(R.id.profileImagePhoto);
        profileName = view.findViewById(R.id.nProfileName);
        profileBio = view.findViewById(R.id.nProfileBio);

    }

    private void handle(){

        firebaseAuth = FirebaseAuth.getInstance();
        String user_id = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful() && task.getResult().exists()){

                    String name = task.getResult().getString("name");
                    String bio = task.getResult().getString("bio");
                    String image = task.getResult().getString("image");

                    profileName.setText(name);
                    profileBio.setText(bio);

                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.placeholder(R.drawable.profilephoto);

                    Glide.with(context).setDefaultRequestOptions(requestOptions).load(image).into(profileImage);
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_account, container, false);
        context = view.getContext();

        init(view);
        handle();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.account_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_edit_btn:
                Intent editprofileIntent = new Intent(getActivity(),  SetupActivity.class);
                startActivity(editprofileIntent);
                return true;

            default:
                return false;
        }
    }
}
