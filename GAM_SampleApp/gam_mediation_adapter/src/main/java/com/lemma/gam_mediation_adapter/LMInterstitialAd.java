package com.lemma.gam_mediation_adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;

import org.json.JSONException;
import org.json.JSONObject;

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

        // Extract the JSON string from the Bundle
        String serverParameter = mediationInterstitialAdConfiguration.getServerParameters().getString("parameter");

        // Parse server parameters from JSONObject
        String pubId = "";
        String adUnitId = "";
        String adServerUrl = "";

        try {
            JSONObject jsonObject = new JSONObject(serverParameter);
            pubId = jsonObject.optString("pubId", "");
            adUnitId = jsonObject.optString("adUnitId", "");
            adServerUrl = jsonObject.optString("adServerUrl", "");

        } catch (JSONException e) {
            AdError adError = new AdError(
                    e.hashCode(),
                    Objects.requireNonNull(e.getMessage()),
                    "com.lemma.gam_mediation_adapter"
            );
            adLoadCallback.onFailure(adError);
        }


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
