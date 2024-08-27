package com.lemma.gam_mediation_adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;

import java.util.Objects;

import lemma.lemmavideosdk.interstitial.LMInterstitial;
import lemma.lemmavideosdk.interstitial.LMInterstitial.LMInterstitialListener;

public class LMInterstitialAd implements MediationInterstitialAd {

    private final LMInterstitial lmInterstitial;

    @NonNull
    private final MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> adLoadCallback;

    @Nullable
    private MediationInterstitialAdCallback interstitialAdCallback;

    // Constructor
    public LMInterstitialAd(@NonNull MediationInterstitialAdConfiguration mediationInterstitialAdConfiguration,
                            @NonNull MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> adLoadCallback) {
        this.adLoadCallback = adLoadCallback;

        String pubId = mediationInterstitialAdConfiguration.getServerParameters().getString("pubId");
        String adUnitId  = mediationInterstitialAdConfiguration.getServerParameters().getString("adUnitId");
        String adServerUrl = mediationInterstitialAdConfiguration.getServerParameters().getString("adServerUrl");


        // Initialize LMInterstitial with required parameters
        this.lmInterstitial = new LMInterstitial(mediationInterstitialAdConfiguration.getContext(), pubId, adUnitId ,adServerUrl);

        this.lmInterstitial.setListener(new LMInterstitialListener() {

            @Override
            public void onAdReceived(LMInterstitial ad) {
                // Notify that the ad has been loaded
                interstitialAdCallback = LMInterstitialAd.this.adLoadCallback.onSuccess(LMInterstitialAd.this);
            }

            @Override
            public void onAdFailed(LMInterstitial ad, Error error) {
                // Create an AdError object
                AdError adError = new AdError(
                        error.hashCode(),
                        Objects.requireNonNull(error.getMessage()),
                        "com.lemma.gam_mediation_adapter"
                );

                // Notify that the ad failed to load
                adLoadCallback.onFailure(adError);
            }

            @Override
            public void onAdOpened(LMInterstitial ad) {
                // Notify that the ad has been opened
                if (interstitialAdCallback != null) {
                    interstitialAdCallback.onAdOpened();
                }
            }

            @Override
            public void onAdClosed(LMInterstitial ad) {
                // Notify that the ad has been closed
                if (interstitialAdCallback != null) {
                    interstitialAdCallback.onAdClosed();
                }
            }
        });
    }

    public void loadAd() {
        lmInterstitial.loadAd();
    }

    @Override
    public void showAd(@NonNull Context context) {
        lmInterstitial.show();
    }
}
