package com.example.gam_sampleapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the banner link TextView
        TextView bannerLink = findViewById(R.id.banner_link);
        TextView interstitialLink = findViewById(R.id.interstitial_link);


        // Set up the click listener to start MyActivity
        bannerLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BannerActivity.class);
            startActivity(intent);
        });

        interstitialLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,InterstitialActivity.class);
            startActivity(intent);
        });

    }
}
