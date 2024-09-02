package com.lemma.gam_mediation_adapter;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.VersionInfo;
import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;

import java.util.List;

import lemma.lemmavideosdk.common.LemmaSDK;
import lemma.lemmavideosdk.interstitial.LMInterstitial;

public class LemmaCustomEvent extends Adapter {

    private static final String TAG = "LemmaCustomEvent";
    @NonNull
    @Override
    public VersionInfo getSDKVersionInfo() {
        Log.d(TAG, "getSDKVersionInfo");
        String version = LemmaSDK.getVersion();
        String[] version_split = version.split("\\.");
        return new VersionInfo(Integer.parseInt(version_split[0]),
                Integer.parseInt(version_split[1]),
                Integer.parseInt(version_split[2]));
    }

    @NonNull
    @Override
    public VersionInfo getVersionInfo() {
        Log.d(TAG, "getVersionInfo");
        return new VersionInfo(1, 0, 0);
    }

    @Override
    public void initialize(@NonNull Context context,
                           @NonNull InitializationCompleteCallback initializationCompleteCallback,
                           @NonNull List<MediationConfiguration> list) {
        Log.d(TAG, "Lemma initialize");
        LemmaSDK.init(context, false);
    }

    @Override
    public void loadBannerAd(@NonNull MediationBannerAdConfiguration mediationBannerAdConfiguration,
                             @NonNull MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback) {
        super.loadBannerAd(mediationBannerAdConfiguration, callback);
        Log.d(TAG, "loadBannerAd");

        LMBannerAd bannerAd = new LMBannerAd(mediationBannerAdConfiguration, callback);
        bannerAd.loadAd();
    }

    @Override
    public void loadInterstitialAd(@NonNull MediationInterstitialAdConfiguration mediationInterstitialAdConfiguration,
                                   @NonNull MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> callback) {
        super.loadInterstitialAd(mediationInterstitialAdConfiguration, callback);
        Log.d(TAG, "loadInterstitialAd");

        // Initialize and load the interstitial ad
        LMInterstitialAd interstitialAd = new LMInterstitialAd(mediationInterstitialAdConfiguration, callback);
        interstitialAd.loadAd();
    }
}
