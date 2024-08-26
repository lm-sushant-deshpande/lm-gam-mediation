package com.lemma.gam_mediation_adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;

import lemma.lemmavideosdk.banner.LMBannerView;
import lemma.lemmavideosdk.banner.LMBannerView.BannerViewListener;
import lemma.lemmavideosdk.banner.LMBannerView.LMAdSize;

public class LMBannerAd implements MediationBannerAd {

    private final LMBannerView banner;
    private final MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> adLoadCallback;
    private MediationBannerAdCallback adCallback;

    public LMBannerAd(@NonNull MediationBannerAdConfiguration mediationBannerAdConfiguration,
                      @NonNull MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback) {
        this.adLoadCallback = callback;

        // Extract necessary parameters from mediationBannerAdConfiguration
        String pubId = mediationBannerAdConfiguration.getServerParameters().getString("pubId");
        String adUnitId = mediationBannerAdConfiguration.getServerParameters().getString("adUnitId");
        String adServerUrl = mediationBannerAdConfiguration.getServerParameters().getString("adServerUrl");

        // Get the ad size from the configuration
        int width = mediationBannerAdConfiguration.getAdSize().getWidth();
        int height = mediationBannerAdConfiguration.getAdSize().getHeight();
        LMAdSize adSize = new LMAdSize(width, height);

        // Initialize LMBannerView
        this.banner = new LMBannerView(mediationBannerAdConfiguration.getContext(), pubId, adUnitId, adSize, adServerUrl);
        this.banner.setBannerViewListener(new BannerViewListener() {
            @Override
            public void onAdReceived() {
                adCallback = adLoadCallback.onSuccess(LMBannerAd.this);
                adCallback.reportAdImpression();
            }

            @Override
            public void onAdError(Error error) {
                adLoadCallback.onFailure(error.getMessage());
            }
        });
    }

    public void loadAd() {
        // Load the ad into the banner view
        banner.loadAd();
    }

    @NonNull
    @Override
    public View getView() {
        return banner;
    }
}
