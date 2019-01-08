package com.teamo.app.unilife;


import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private BlogRecyclerAdapter blogRecyclerAdapter;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    public HomeFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blog_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null) {

            blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
            blog_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
            blog_list_view.setAdapter(blogRecyclerAdapter);
            blog_list_view.setHasFixedSize(true);

            firebaseFirestore = FirebaseFirestore.getInstance();
            int filter = 0;

            try{
                filter = Integer.parseInt(getArguments().getString("filter", "0"));
            }catch (Exception e){

            }

            if(filter==0){
                new DataPlacement().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else{
                loadMorePost(filter);
            }

        }



        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_categories_btn:
                Intent toCategory = new Intent(getContext(), CategoryActivity.class);
                startActivity(toCategory);
                break;
        }

        return true;
    }

    public void loadMorePost(int filter){

        if(firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = null;

            if(filter == 0){
                nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);
            }else if(filter == 1){ //Second hand stuff
                blog_list.clear();
                nextQuery = firebaseFirestore.collection("Posts")
                        .whereEqualTo("category", "Second Hand Stuffs");
            }else if(filter == 2){
                blog_list.clear();
                nextQuery = firebaseFirestore.collection("Posts")
                        .whereEqualTo("category", "Search Restaurants");
            }else{
                blog_list.clear();
                nextQuery = firebaseFirestore.collection("Posts")
                        .whereEqualTo("category", "Search Home");
            }


            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                blog_list.add(blogPost);

                                blogRecyclerAdapter.notifyDataSetChanged();
                            }

                        }
                    }

                }
            });

        }

    }

    private class DataPlacement extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            if(firebaseAuth.getCurrentUser() != null) {

                blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                        if (reachedBottom) {

                            loadMorePost(0);

                        }

                    }
                });

                Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
                firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {

                            if (isFirstPageFirstLoad) {

                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                blog_list.clear();

                            }

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                    if (isFirstPageFirstLoad) {

                                        blog_list.add(blogPost);

                                    } else {

                                        blog_list.add(0, blogPost);

                                    }


                                    blogRecyclerAdapter.notifyDataSetChanged();

                                }
                            }

                            isFirstPageFirstLoad = false;

                        }

                    }

                });
            }

            return null;
        }
    }
}
