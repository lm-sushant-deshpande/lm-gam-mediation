package com.example.gam_sampleapp;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import lemma.lemmavideosdk.banner.LMBannerView;

/** Main Activity. Inflates main activity xml and child fragments. */
public class BannerActivity extends AppCompatActivity {

  public static final String TEST_DEVICE_HASHED_ID = "ABCDEF012345";
  static final String AD_UNIT = "/22192417927/HB Video Test";
  private static final String TAG = "MyActivity";
  private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
  private final AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);
  private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
  private AdManagerAdView adView;
  private FrameLayout adContainerView;
  private Button loadAdButton;
  private LMBannerView bannerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.banner_activity);

    bannerView = new LMBannerView(this, "895", "18618", new LMBannerView.LMAdSize(570, 162),"https://sg.ads.lemmatechnologies.com/lemma/servad");

    FrameLayout mLinerAdContainer = findViewById(R.id.ad_linear_container);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

    mLinerAdContainer.addView(bannerView, params);


    bannerView.setBannerViewListener(new LMBannerView.BannerViewListener() {
      @Override
      public void onAdReceived() {
        Log.d(TAG,"Ad Received Successfully");
      }

      @Override
      public void onAdError(Error error) {
        Log.e(TAG, "Ad failed with error - " + error.toString());
      }
    });

    Button playBtn = findViewById(R.id.start);
    playBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        bannerView.loadAd();
      }
    });

  }


  @Override
  public void onPause() {
    if (adView != null) {
      adView.pause();
    }
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (adView != null) {
      adView.resume();
    }
  }

  @Override
  public void onDestroy() {
    if (adView != null) {
      adView.destroy();
    }
    super.onDestroy();
  }

  private void loadBanner() {
    if (adView != null) {
      adContainerView.removeView(adView); // Remove old ad view if present
    }

    // Create a new ad view
    adView = new AdManagerAdView(this);
    adView.setAdUnitId(AD_UNIT);
    adView.setAdSize(getAdSize());

    // Set the AdListener to handle ad events
    adView.setAdListener(new AdListener() {
      @Override
      public void onAdLoaded() {
        super.onAdLoaded();
        // Show a toast message when the ad is loaded successfully
        Toast.makeText(BannerActivity.this, "Ad loaded successfully", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onAdFailedToLoad(LoadAdError adError) {
        super.onAdFailedToLoad(adError);
        // Log or handle the ad load failure here
        Log.e(TAG, "Ad failed to load: " + adError.getMessage());
      }
    });

    // Start loading the ad in the background
    AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
    adView.loadAd(adRequest);

    // Replace ad container with new ad view
    adContainerView.removeAllViews();
    adContainerView.addView(adView);
  }

  private void initializeMobileAdsSdk() {
    if (isMobileAdsInitializeCalled.getAndSet(true)) {
      return;
    }

    // Set your test devices
    MobileAds.setRequestConfiguration(
            new RequestConfiguration.Builder()
                    .setTestDeviceIds(Arrays.asList(TEST_DEVICE_HASHED_ID))
                    .build());

    new Thread(
            () -> {
              // Initialize the Google Mobile Ads SDK on a background thread
              MobileAds.initialize(this, initializationStatus -> {});

              // No need to load the ad here; it's done when the button is clicked
            })
            .start();
  }

  public AdSize getAdSize() {
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    int adWidthPixels = displayMetrics.widthPixels;

    if (VERSION.SDK_INT >= VERSION_CODES.R) {
      WindowMetrics windowMetrics = this.getWindowManager().getCurrentWindowMetrics();
      adWidthPixels = windowMetrics.getBounds().width();
    }

    float density = displayMetrics.density;
    int adWidth = (int) (adWidthPixels / density);
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
  }
}
