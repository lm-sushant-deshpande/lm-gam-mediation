/*
 * Copyright (C) 2013 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.gam_sampleapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/** Main Activity. Inflates main activity xml. */
@SuppressLint("SetTextI18n")
public class InterstitialActivity extends AppCompatActivity {

  // Check your logcat output for the test device hashed ID e.g.
  // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
  // to get test ads on this device" or
  // "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF012345") to set this as
  // a debug device".
  public static final String TEST_DEVICE_HASHED_ID = "ABCDEF012345";

  private static final long GAME_LENGTH_MILLISECONDS = 3000;
  private static final String AD_UNIT_ID = "/21775744923/example/interstitial";
  private static final String TAG = "MyActivity";

  private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
  private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
  private AdManagerInterstitialAd interstitialAd;
  private CountDownTimer countDownTimer;
  private Button loadAd;
  private boolean gamePaused;
  private boolean gameOver;
  private boolean adIsLoading;
  private long timerMilliseconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interstitial_activity);

        // Log the Mobile Ads SDK version.
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion());

        googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        googleMobileAdsConsentManager.gatherConsent(
                this,
                consentError -> {
                    if (consentError != null) {
                        // Consent not obtained in current session.
                        Log.w(
                                TAG,
                                String.format("%s: %s", consentError.getErrorCode(), consentError.getMessage()));
                    }

                    startGame();

                    if (googleMobileAdsConsentManager.canRequestAds()) {
                        initializeMobileAdsSdk();
                    }

                    if (googleMobileAdsConsentManager.isPrivacyOptionsRequired()) {
                        // Regenerate the options menu to include a privacy setting.
                        invalidateOptionsMenu();
                    }
                });

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk();
        }

        loadAd = findViewById(R.id.load_ad);
        Button showAd = findViewById(R.id.show_ad);

        loadAd.setOnClickListener(view -> {
            loadAd();
        });

        showAd.setOnClickListener(view -> {
            showInterstitial();
        });

        // Initially disable the "Show Ad" button.
        showAd.setEnabled(false);
    }

  private void createTimer(final long milliseconds) {
    // Create the game timer, which counts down to the end of the level
    // and shows the "retry" button.
    if (countDownTimer != null) {
      countDownTimer.cancel();
    }

    countDownTimer =
        new CountDownTimer(milliseconds, 50) {
          @Override
          public void onTick(long millisUnitFinished) {
            timerMilliseconds = millisUnitFinished;
          }

          @Override
          public void onFinish() {
            gameOver = true;
              loadAd.setVisibility(View.VISIBLE);
          }
        };

    countDownTimer.start();
  }

  @Override
  public void onResume() {
    // Start or resume the game.
    super.onResume();
    resumeGame();
  }

  @Override
  public void onPause() {
    super.onPause();
    pauseGame();
  }


    private void loadAd() {
        // Request a new ad if one isn't already loaded.
        if (adIsLoading || interstitialAd != null) {
            return;
        }
        adIsLoading = true;
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        AdManagerInterstitialAd.load(
                this,
                AD_UNIT_ID,
                adRequest,
                new AdManagerInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        InterstitialActivity.this.interstitialAd = interstitialAd;
                        adIsLoading = false;
                        Log.i(TAG, "onAdLoaded");
                        Toast.makeText(InterstitialActivity.this, "Ad loaded successfully!", Toast.LENGTH_SHORT).show();
                        // Enable the "Show Ad" button after the ad is loaded.
                        findViewById(R.id.show_ad).setEnabled(true);

                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        Log.d(TAG, "The ad was dismissed.");
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        InterstitialActivity.this.interstitialAd = null;
                                        // Optionally disable the "Show Ad" button if desired.
                                        findViewById(R.id.show_ad).setEnabled(false);
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        Log.d(TAG, "The ad failed to show.");
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        InterstitialActivity.this.interstitialAd = null;
                                        findViewById(R.id.show_ad).setEnabled(false);
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d(TAG, "The ad was shown.");
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        interstitialAd = null;
                        adIsLoading = false;
                        String error =
                                String.format(
                                        java.util.Locale.US,
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(),
                                        loadAdError.getCode(),
                                        loadAdError.getMessage());
                        Toast.makeText(
                                        InterstitialActivity.this, "onAdFailedToLoad() with error: " + error, Toast.LENGTH_SHORT)
                                .show();
                        // Optionally disable the "Show Ad" button if needed.
                        findViewById(R.id.show_ad).setEnabled(false);
                    }
                });
    }


    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise restart the game.
        if (interstitialAd != null) {
            interstitialAd.show(this);
        } else {
            startGame();
            if (googleMobileAdsConsentManager.canRequestAds()) {
                loadAd();
            }
        }
    }

  private void startGame() {
    // Hide the button, and kick off the timer.loadAd.setVisibility(View.INVISIBLE);
    createTimer(GAME_LENGTH_MILLISECONDS);
    gamePaused = false;
    gameOver = false;
  }

  private void resumeGame() {
    if (gameOver || !gamePaused) {
      return;
    }
    // Create a new timer for the correct length.
    gamePaused = false;
    createTimer(timerMilliseconds);
  }

  private void pauseGame() {
    if (gameOver || gamePaused) {
      return;
    }
    countDownTimer.cancel();
    gamePaused = true;
  }

  private void initializeMobileAdsSdk() {
    if (isMobileAdsInitializeCalled.getAndSet(true)) {
      return;
    }

    // Set your test devices.
    MobileAds.setRequestConfiguration(
        new RequestConfiguration.Builder()
            .setTestDeviceIds(Arrays.asList(TEST_DEVICE_HASHED_ID))
            .build());

    new Thread(
            () -> {
              // Initialize the Google Mobile Ads SDK on a background thread.
              MobileAds.initialize(this, initializationStatus -> {});

              // Load an ad on the main thread.
              runOnUiThread(() -> loadAd());
            })
        .start();
  }
}
