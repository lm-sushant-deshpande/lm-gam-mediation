package lemma.lemmavideosdk.vast.listeners;


import lemma.lemmavideosdk.vast.VastBuilder.AdI;

public interface AdPlayerAdapter {

    void loadMedia(String url, String type);

    void loadMedia(final String url, final String mimeType, final int duration);

    void playAd();

    void pauseAd();

    void resumeAd();

    boolean isPlaying();

    void mute();

    void unmute();

    void skipAd();

    void stopAd();

    int getTotalDuration();

    int getCurrentPlaybackTime();

    AdPlayerCallback getAdPlayerCallback();

    void setAdPlayerCallback(AdPlayerCallback playerCallback);

    void loadMedia(String url);

    void destroy();

    interface AdPlayerCallback {

        void onAdPlayerPrepared(AdI ad);

        void onAdStarted();

        void onAdPause();

        void onAdResume();

        void onAdMute();

        void onAdUnmute();

        void onAdSkip();

        void onAdStop();

        void onAdCompleted(AdI ad);

        void onAdFullScreen();

        void onMinimize();

        void onAdPlayError(AdI ad, int what, int extra);


        void onFirstQuartileReached(AdI ad);

        void onMidPointReached(AdI ad);

        void onThirdQuartileReached(AdI ad);

    }
}
