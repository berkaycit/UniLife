package com.teamo.app.unilife;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class CategoryActivity extends AppCompatActivity {

    TextView searchHomeMate, searchRestaurant, secondHandStuff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar categoryToolbar = findViewById(R.id.category_toolbar);
        setSupportActionBar(categoryToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Category</font>"));

        searchHomeMate = findViewById(R.id.txtSearchingHomeMate); //3 -> filter id
        searchRestaurant = findViewById(R.id.txtSearchingRestaurants); //2
        secondHandStuff = findViewById(R.id.txtSecondHandStuff); //1

        secondHandStuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent secondHandtoMain = new Intent(CategoryActivity.this, MainActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString("filter", "1");
                HomeFragment homeFragment = new HomeFragment();
                homeFragment.setArguments(bundle);

                startActivity(secondHandtoMain);
            }
        });

        searchRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resttoMain = new Intent(CategoryActivity.this, MainActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString("filter", "2");
                HomeFragment homeFragment = new HomeFragment();
                homeFragment.setArguments(bundle);

                startActivity(resttoMain);
            }
        });

        searchHomeMate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homematetoMain = new Intent(CategoryActivity.this, MainActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString("filter", "3");
                HomeFragment homeFragment = new HomeFragment();
                homeFragment.setArguments(bundle);

                startActivity(homematetoMain);
            }
        });

    }
}
