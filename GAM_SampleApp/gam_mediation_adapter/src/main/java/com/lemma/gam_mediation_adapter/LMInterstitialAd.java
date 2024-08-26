package com.lemma.gam_mediation_adapter;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;

import lemma.lemmavideosdk.interstitial.LMInterstitial;
import lemma.lemmavideosdk.interstitial.LMInterstitial.LMInterstitialListener;

public class LMInterstitialAd implements MediationInterstitialAd {

    private final LMInterstitial lmInterstitial;
    private final MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> adLoadCallback;
    private MediationInterstitialAdCallback interstitialAdCallback;

    // Constructor
    public LMInterstitialAd(@NonNull Context context,
                            @NonNull MediationInterstitialAdConfiguration adConfiguration,
                            @NonNull MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> adLoadCallback) {
        this.adLoadCallback = adLoadCallback;

        // Initialize LMInterstitial with required parameters
        this.lmInterstitial = new LMInterstitial(context, adConfiguration.getServerParameters().getString("pubId"),
                adConfiguration.getServerParameters().getString("adUnitId"));

        this.lmInterstitial.setListener(new LMInterstitialListener() {
            @Override
            public void onAdReceived(LMInterstitial ad) {
                // Notify that the ad has been loaded
                interstitialAdCallback = LMInterstitialAd.this.adLoadCallback.onSuccess(LMInterstitialAd.this);
            }

            @Override
            public void onAdFailed(LMInterstitial ad, Error error) {
                // Notify that the ad failed to load
                adLoadCallback.onFailure(String.valueOf(error));
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
