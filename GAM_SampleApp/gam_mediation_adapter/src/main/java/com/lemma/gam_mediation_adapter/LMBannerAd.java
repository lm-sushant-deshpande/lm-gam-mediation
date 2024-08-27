package com.lemma.gam_mediation_adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import lemma.lemmavideosdk.banner.LMBannerView;
import lemma.lemmavideosdk.banner.LMBannerView.BannerViewListener;
import lemma.lemmavideosdk.banner.LMBannerView.LMAdSize;

public class LMBannerAd implements MediationBannerAd {

    private final LMBannerView lmBannerView;

    @NonNull
    private final MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> adLoadCallback;

    @Nullable
    private MediationBannerAdCallback bannerAdCallback;

    public LMBannerAd(@NonNull MediationBannerAdConfiguration mediationBannerAdConfiguration,
                      @NonNull MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback) {
        this.adLoadCallback = callback;

        // Extract the JSON string from the Bundle
        String serverParameter = mediationBannerAdConfiguration.getServerParameters().getString("parameter");

        // Parse server parameters from JSONObject
        String pubId = "";
        String adUnitId = "";
        String adServerUrl = "";

        try {
            JSONObject jsonObject = new JSONObject(serverParameter);
            pubId = jsonObject.optString("pubId", "");
            adUnitId = jsonObject.optString("adUnitId", "");
            adServerUrl = jsonObject.optString("adServerUrl", "");

        } catch (JSONException error) {
            AdError adError = new AdError(
                    error.hashCode(),
                    Objects.requireNonNull(error.getMessage()),
                    "com.lemma.gam_mediation_adapter"
            );
            adLoadCallback.onFailure(adError);
        }

        // Get the ad size from the configuration
        int width = mediationBannerAdConfiguration.getAdSize().getWidth();
        int height = mediationBannerAdConfiguration.getAdSize().getHeight();
        LMAdSize adSize = new LMAdSize(width, height);

        // Initialize LMBannerView
        this.lmBannerView = new LMBannerView(mediationBannerAdConfiguration.getContext(), pubId, adUnitId, adSize, adServerUrl);
        this.lmBannerView.setBannerViewListener(new BannerViewListener() {
            @Override
            public void onAdReceived() {
                bannerAdCallback = adLoadCallback.onSuccess(LMBannerAd.this);
                bannerAdCallback.reportAdImpression();
            }

            @Override
            public void onAdError(Error error) {
                // Create an AdError object
                AdError adError = new AdError(
                        error.hashCode(),
                        Objects.requireNonNull(error.getMessage()),
                        "com.lemma.gam_mediation_adapter"
                );

                // Notify the callback with the error
                adLoadCallback.onFailure(adError);
            }
        });
    }

    public void loadAd() {
        // Load the ad into the banner view
        lmBannerView.loadAd();
    }

    @NonNull
    @Override
    public View getView() {
        return lmBannerView;
    }
}
