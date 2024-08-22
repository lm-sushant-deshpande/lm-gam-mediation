package lemma.lemmavideosdk.views;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.vast.VastBuilder.AdGroup;
import lemma.lemmavideosdk.vast.VastBuilder.AdI;
import lemma.lemmavideosdk.vast.listeners.AdPlayerAdapter;


public class AdGroupPlayerView extends LinearLayout {

    AdGroup adGroup;
    ArrayList<AdPlayerView> players;
    ArrayList<AdI> adQueue;
    boolean alreadyLayedOut;
    AdGroupPlayerViewListener playerViewListener;

    public AdGroupPlayerView(Context context, AdGroup adGroup) {
        super(context);
        players = new ArrayList<>();
        adQueue = new ArrayList<>();
    }

    public void setPlayerViewListener(AdGroupPlayerViewListener playerViewListener) {
        this.playerViewListener = playerViewListener;
    }

    public void reset() {

        if (players != null) {
            for (AdPlayerView adPlayerView : players) {
                adPlayerView.destroy();
            }
            players.clear();
            players = null;
        }
    }

    public void prepareLayoutForAdGroup(AdGroup adGroup) {
        this.adGroup = adGroup;

        if (!alreadyLayedOut) {
            prepareLayoutForFourAds(adGroup);
            alreadyLayedOut = true;
        }
    }

    boolean exists(ArrayList<Pair> yArray, int index) {

        for (Pair p : yArray) {
            int i = p.coordinate;
            if (i == index) {
                return true;
            }

        }
        return false;
    }

    public void prepareLayoutForFourAds(AdGroup adGroup) {

        ArrayList<Pair> xArray = new ArrayList<>();
        ArrayList<Pair> yArray = new ArrayList<>();

        int maxDiff = 0;
        boolean maxDiffAlongX = true;
        for (AdI ad : adGroup.ads) {
            adQueue.add(ad);

            if (!exists(xArray, ad.frame.startX)) {
                Pair p = new Pair();
                p.frame = ad.frame;
                p.coordinate = ad.frame.startX;
                xArray.add(p);
            }


            if (!exists(yArray, ad.frame.startY)) {
                Pair p = new Pair();
                p.frame = ad.frame;
                p.coordinate = ad.frame.startY;
                yArray.add(p);
            }

            int diff = Math.abs(ad.frame.startX - ad.frame.endX);
            if (maxDiff < diff) {
                maxDiff = diff;
                maxDiffAlongX = true;
            }

            diff = Math.abs(ad.frame.startY - ad.frame.endY);
            if (maxDiff < diff) {
                maxDiff = diff;
                maxDiffAlongX = false;
            }
        }

        if (maxDiffAlongX) {
            //cut horizontal
            setOrientation(VERTICAL);
            //Unique y n umber of linear layouts
            createLayoutsY(yArray);

        } else {
            //cut vertical
            setOrientation(HORIZONTAL);
            //Unique x
            createLayoutsX(xArray);
        }

    }

    public void createLayoutsX(ArrayList<Pair> mapsX) {

        for (Pair pairX : mapsX) {
            AdI.Frame f = pairX.frame;

            int weight = Math.abs(f.startX - f.endX);
            LinearLayout linearLayout1 = new LinearLayout(getContext());
            linearLayout1.setOrientation(VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 1 - (float) (weight / 100.0);
            addViewsInFrameRangeX(linearLayout1, new AdI.Frame(f.startX, f.endX, 0, 100));
            addView(linearLayout1, params);
        }
    }

    public void createLayoutsY(ArrayList<Pair> mapsY) {

        for (Pair pairY : mapsY) {
            AdI.Frame f = pairY.frame;

            int weight = Math.abs(f.startY - f.endY);
            LinearLayout linearLayout1 = new LinearLayout(getContext());
            linearLayout1.setOrientation(HORIZONTAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 1 - (float) (weight / 100.0);
            addViewsInFrameRangeY(linearLayout1, new AdI.Frame(0, 100, f.startY, f.endY));
            addView(linearLayout1, params);
        }
    }

    View viewForAd(AdI ad) {

        AdPlayerView sdkPlayer = new AdPlayerView(getContext());
        sdkPlayer.setAdPlayerCallback(new AdPlayerAdapter.AdPlayerCallback() {
            @Override
            public void onAdPlayerPrepared(AdI ad) {

                LMLog.i("AdGroupPlayerView", "Started playing ad " + ad);
                if (playerViewListener != null) {
                    playerViewListener.startedPlayingAd(adGroup, ad);
                }

            }

            @Override
            public void onAdStarted() {

            }

            @Override
            public void onAdPause() {

            }

            @Override
            public void onAdResume() {

            }

            @Override
            public void onAdMute() {

            }

            @Override
            public void onAdUnmute() {

            }

            @Override
            public void onAdSkip() {

            }

            @Override
            public void onAdStop() {

            }

            @Override
            public void onAdCompleted(AdI ad) {

                playerViewListener.endPlayingAd(adGroup, ad);

                LMLog.i("AdGroupPlayerView", "Ad playing completed " + ad);

                adQueue.remove(ad);
                if (adQueue.size() == 0 && playerViewListener != null) {
                    LMLog.i("AdGroupPlayerView", "Ad Group playing End");
                    playerViewListener.endPlayingAdGroup(adGroup);
                }
            }

            @Override
            public void onAdFullScreen() {

            }

            @Override
            public void onMinimize() {

            }

            @Override
            public void onAdPlayError(AdI ad, int what, int extra) {
                LMLog.i("AdGroupPlayerView", "Ad playing error for ad" + ad);
                adQueue.remove(ad);
                if (adQueue.size() == 0 && playerViewListener != null) {
                    playerViewListener.endPlayingAdGroup(adGroup);
                }
            }

            @Override
            public void onFirstQuartileReached(AdI ad) {
                playerViewListener.onFirstQuartileReached(adGroup, ad);

            }

            @Override
            public void onMidPointReached(AdI ad) {
                playerViewListener.onMidPointReached(adGroup, ad);
            }

            @Override
            public void onThirdQuartileReached(AdI ad) {
                playerViewListener.onThirdQuartileReached(adGroup, ad);
            }
        });

        sdkPlayer.loadAd(ad);
        players.add(sdkPlayer);
        return sdkPlayer;
    }

    public void play() {

        for (AdPlayerView sdkPlayer : players) {
            sdkPlayer.playAd();
        }
    }

    void addViewsInFrameRangeY(LinearLayout parentLayout, AdI.Frame f) {

        ArrayList<AdI> matchedAds = new ArrayList<>();
        for (AdI ad : adGroup.ads) {
            if (ad.frame.startY == f.startY && ad.frame.endY == f.endY) {
                matchedAds.add(ad);
            }
        }

        if (matchedAds.size() == 1) {
            AdI ad = matchedAds.get(0);
            View view = viewForAd(ad);
            parentLayout.addView(view);

        } else if (matchedAds.size() > 1) {

            for (AdI ad : matchedAds) {

                View view = viewForAd(ad);

                int weight = Math.abs(ad.frame.startX - ad.frame.endX);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params.weight = 1 - (float) (weight / 100.0);

                parentLayout.addView(view, params);
            }
        }
    }

    void addViewsInFrameRangeX(LinearLayout parentLayout, AdI.Frame f) {

        ArrayList<AdI> matchedAds = new ArrayList<>();
        for (AdI ad : adGroup.ads) {
            if (ad.frame.startX == f.startX && ad.frame.endX == f.endX) {
                matchedAds.add(ad);
            }
        }

        if (matchedAds.size() == 1) {
            AdI ad = matchedAds.get(0);
            View view = viewForAd(ad);
            parentLayout.addView(view);

        } else if (matchedAds.size() > 1) {

            for (AdI ad : matchedAds) {

                View view = viewForAd(ad);

                int weight = Math.abs(ad.frame.startY - ad.frame.endY);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params.weight = 1 - (float) (weight / 100.0);
                parentLayout.addView(view, params);
            }
        }
    }

    public interface AdGroupPlayerViewListener {
        void onSuccessLayout();

        void onFailureLayout(String reason);

        void endPlayingAdGroup(AdGroup adGroup);

        void endPlayingAd(AdGroup adGroup, AdI ad);

        void startedPlayingAd(AdGroup adGroup, AdI ad);


        void onFirstQuartileReached(AdGroup adGroup, AdI ad);

        void onMidPointReached(AdGroup adGroup, AdI ad);

        void onThirdQuartileReached(AdGroup adGroup, AdI ad);
    }

    class Pair {

        public Integer coordinate;
        public AdI.Frame frame;
    }
}
