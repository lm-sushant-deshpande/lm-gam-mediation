package com.example.gam_sampleapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.lemma.gam_mediation_adapter.LemmaCustomEvent;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/** Main Activity. Inflates main activity xml. */
@SuppressLint("SetTextI18n")
public class InterstitialActivity extends AppCompatActivity {

    public static final String TEST_DEVICE_HASHED_ID = "5EDC3D41206D55C7AF372A3BBE9A4D71";
    private static final long GAME_LENGTH_MILLISECONDS = 3000;
    private static final String AD_UNIT_ID = "/22192417927/HB Video Test";
    private static final String TAG = "MyActivity";

    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdManagerInterstitialAd interstitialAd;
    private CountDownTimer countDownTimer;
    private Button loadAd;
    private Button showAd;
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

        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        googleMobileAdsConsentManager.gatherConsent(
                this,
                consentError -> {
                    if (consentError != null) {
                        // Consent not obtained in current session.
                        Log.w(TAG, String.format("%s: %s", consentError.getErrorCode(), consentError.getMessage()));
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

        // Initialize the buttons
        loadAd = findViewById(R.id.load_ad);
        showAd = findViewById(R.id.show_ad);

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

        countDownTimer = new CountDownTimer(milliseconds, 50) {
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

        // Build the ad request.
        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        // Add any additional configuration you might be using
        // For example, you could add test devices, keywords, etc.

        AdManagerAdRequest adRequest = adRequestBuilder.build();

        // Log the ad request details.
        Log.d(TAG, "Ad Request Details: " +
                "Test Devices: " + MobileAds.getRequestConfiguration().getTestDeviceIds() +
                ", Ad Request: " + adRequest.toString() // AdManagerAdRequest does not have a toString() method, use specific details.
        );

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
                        showAd.setEnabled(true);

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
                                        showAd.setEnabled(false);
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        Log.d(TAG, "The ad failed to show.");
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        InterstitialActivity.this.interstitialAd = null;
                                        showAd.setEnabled(false);
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
                        String errorMessage = loadAdError.getMessage();
                        String errorDetails = String.format(
                                java.util.Locale.US,
                                "domain: %s, code: %d, message: %s",
                                loadAdError.getDomain(),
                                loadAdError.getCode(),
                                loadAdError.getMessage()
                        );

                        Log.e(TAG, "Ad failed to load. Error: " + errorDetails);
                        Toast.makeText(
                                InterstitialActivity.this,
                                "Failed to load ad: " + errorMessage,
                                Toast.LENGTH_SHORT
                        ).show();

                        interstitialAd = null;
                        adIsLoading = false;
                        showAd.setEnabled(false);
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
                })
                .start();
    }
}