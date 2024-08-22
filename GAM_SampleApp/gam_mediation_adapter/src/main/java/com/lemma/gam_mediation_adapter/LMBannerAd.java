package com.lemma.gam_mediation_adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;

import lemma.lemmavideosdk.banner.LMBannerView;

public class LMBannerAd implements MediationBannerAd {
    private LMBannerView banner;

    public LMBannerAd(@NonNull MediationBannerAdConfiguration mediationBannerAdConfiguration, @NonNull MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback) {

    }

    public void loadAd() {
//        LMBannerView banner new LMBannerView();
//        banner.loadAd();
    }

    @NonNull
    @Override
    public View getView() {
        return null;
    }
}
