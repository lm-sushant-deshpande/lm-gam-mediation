package lemma.lemmavideosdk.bannervideo;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import lemma.lemmavideosdk.banner.HtmlView;
import lemma.lemmavideosdk.common.AppLog;
import lemma.lemmavideosdk.common.BidDetail;
import lemma.lemmavideosdk.common.DisplayableAdI;
import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.common.LMUtils;
import lemma.lemmavideosdk.common.RTBRequestBuilder;
import lemma.lemmavideosdk.vast.VastBuilder.AdI;
import lemma.lemmavideosdk.vast.VastBuilder.LMVastParser;
import lemma.lemmavideosdk.vast.VastBuilder.Tracker;
import lemma.lemmavideosdk.vast.VastBuilder.Vast;
import lemma.lemmavideosdk.vast.manager.AdLoader;
import lemma.lemmavideosdk.vast.manager.AdvertisingIdClient;
import lemma.lemmavideosdk.vast.manager.LMAdRequest;
import lemma.lemmavideosdk.vast.manager.LMAppInfo;
import lemma.lemmavideosdk.vast.manager.LMDeviceInfo;
import lemma.lemmavideosdk.vast.manager.LMLocationManager;
import lemma.lemmavideosdk.vast.manager.NetworkStatusMonitor;
import lemma.lemmavideosdk.vast.tracker.TrackerDBHandler;
import lemma.lemmavideosdk.vast.tracker.TrackerHandler;
import lemma.lemmavideosdk.videointerstitial.LMVideoInterstitial;
import lemma.lemmavideosdk.videointerstitial.LMVideoPlayerView;


public class LMInBannerVideo extends FrameLayout {

    private TrackerHandler trackerHandler;
    LMVideoPlayerView playerView;
    AdI ad;
    String TAG = "LMBannerVideo";
    AdLoader mAdLoader;
    LMAdRequest mAdRequest;
    Context mContext;
    RTBRequestBuilder requestBuilder;
    InBannerVideoListener mInBannerVideoListener;

    public LMInBannerVideo(@NonNull Context context, @NonNull String pubId, @NonNull String adUnitId, @NonNull LMInBannerVideo.LMAdSize adSize, String adServerUrl) {
        super(context);
        mAdRequest = new LMAdRequest(pubId, adUnitId);
        if (adServerUrl != null) {
            mAdRequest.setAdServerBaseURL(adServerUrl);
        }
        mContext = context;
        mAdRequest.setWidth(adSize.width);
        mAdRequest.setHeight(adSize.height);
        requestBuilder = new RTBRequestBuilder(mAdRequest, context);
        try {
            WebView webView = new WebView(context);
            trackerHandler = new TrackerHandler(new NetworkStatusMonitor(context), webView);
            trackerHandler.executeImpressionInWebContainer = true;
        }catch (Exception e){
            AppLog.e(TAG, e.getLocalizedMessage());
            trackerHandler = new TrackerHandler(new NetworkStatusMonitor(context));
        }
        try {
            TrackerDBHandler handler = new TrackerDBHandler(context);
            trackerHandler.trackerDBHandler = handler;
        } catch (Exception e) {
            AppLog.e(TAG, "Unable to create tracker db handler");
        }
    }

    public void setListener(LMInBannerVideo.InBannerVideoListener listener) {
        this.mInBannerVideoListener = listener;
    }

    private void parseAndRenderAd(BidDetail bidDetail) {
        try {
            String vastXML = bidDetail.creative;
            if (vastXML != null && vastXML.length() <= 0) {
                mInBannerVideoListener.onAdFailed(LMInBannerVideo.this, new Error("Empty Vast"));
            } else {

                LMVastParser vastParser = new LMVastParser();
                vastParser.parse(vastXML, new LMVastParser.VastParserCompletionListener() {
                    @Override
                    public void onSuccess(Vast vast) {
                        try {
                            AdI ad = vast.ads.get(0);
                            render(ad);
                        }catch (Exception e){
                            mInBannerVideoListener.onAdFailed(LMInBannerVideo.this, new Error("Empty Vast"));
                        }

                    }

                    @Override
                    public void onError(Vast vast, Error error) {
                        mInBannerVideoListener.onAdFailed(LMInBannerVideo.this, new Error("Empty Vast"));
                    }
                });
            }
        } catch (Exception e) {
            mInBannerVideoListener.onAdFailed(LMInBannerVideo.this, new Error("Empty Vast"));
        }
    }

    public void loadAd() {

        mAdLoader = new AdLoader(new AdLoader.AdLoaderListener() {
            @Override
            public void onSuccess(final DisplayableAdI ad) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        parseAndRenderAd((BidDetail) ad);
                    }
                });
            }

            @Override
            public void onError(final Error err) {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mInBannerVideoListener.onAdFailed(LMInBannerVideo.this, err);
                    }
                });
            }
        });


//        TODO : Enable when RTB params are fixed
        RTBRequestBuilder adURLBuilder = requestBuilder;
        adURLBuilder.isVideo = true;
        adURLBuilder.advertisingIdClient = new AdvertisingIdClient(mContext);
        adURLBuilder.displayMetrics = mContext.getResources().getDisplayMetrics();
        adURLBuilder.appInfo = new LMAppInfo(mContext.getApplicationContext());
        adURLBuilder.networkStatusMonitor = new NetworkStatusMonitor(mContext);
        adURLBuilder.deviceInfo = new LMDeviceInfo(mContext);
        adURLBuilder.locationManager = new LMLocationManager(mContext);
        adURLBuilder.shouldAddDisplayManager = true;

        String url = String.format("%s/lemma/servad", LMUtils.SERVER_URL);
        if (mAdRequest.getAdServerBaseURL() != null) {
            try {
                Uri uri = Uri.parse(mAdRequest.getAdServerBaseURL());
                url = uri.toString();
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage());
            }
        }
        Uri.Builder builder = Uri.parse(url).buildUpon();
        builder.appendQueryParameter("pid", mAdRequest.getPublisherId());
        builder.appendQueryParameter("aid", mAdRequest.getAdUnitId());
        if (mAdRequest.map != null) {
            for (Map.Entry<String, String> entry : mAdRequest.map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key != null && value != null) {
                    builder.appendQueryParameter(key, value);
                }
            }
        }
        mAdLoader.load(builder.build().toString(), adURLBuilder.build());
    }

    private void attachAd(LMVideoPlayerView  view) {
        FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(view, params);
    }

    private void trackImpressions(AdI ad) {
        ArrayList<Tracker> impressionList = ad.getAdTrackers();
        trackerHandler.sendRTBImpression(impressionList);
    }

    private void trackEventImpressions(AdI ad, String event) {
        ArrayList<Tracker> eventTrackers = ad.getEventTrackers(event);
        LMLog.d(TAG, "Scheduling tracker execution " + event + " for event : " + eventTrackers);
        trackerHandler.sendRTBImpression(eventTrackers);
    }

    private LMVideoPlayerView createVideoPlayer(Context context) {

        final LMVideoPlayerView videoPlayer = new LMVideoPlayerView(context);
        videoPlayer.setAutoPlayOnForeground(true);
        videoPlayer.setListener(new LMVideoPlayerView.LMVideoPlayerListener() {
            @Override
            public void onReady(LMVideoPlayerView player) {
                mInBannerVideoListener.onAdReceived(LMInBannerVideo.this);
            }

            @Override
            public void onFailure(int errorCode, String errorMessage) {
                mInBannerVideoListener.onAdFailed(LMInBannerVideo.this, new Error("errorCode"+errorCode+"errorMessage"+errorMessage));
            }

            @Override
            public void onBufferUpdate(int buffer) {

            }

            @Override
            public void onCompletion() {
                trackEventImpressions(LMInBannerVideo.this.ad, "complete");
                if(mInBannerVideoListener != null) {
                    mInBannerVideoListener.onAdCompletion(LMInBannerVideo.this);
                }
            }

            @Override
            public void onStart() {
                trackImpressions(LMInBannerVideo.this.ad);
                trackEventImpressions(LMInBannerVideo.this.ad, "start");
            }

            @Override
            public void onPause() {

            }

            @Override
            public void onProgressUpdate(int seekPosition) {

            }

            @Override
            public void onFirstQuartileReached() {
                trackEventImpressions(LMInBannerVideo.this.ad, "firstQuartile");
            }

            @Override
            public void onMidPointReached() {
                trackEventImpressions(LMInBannerVideo.this.ad, "midpoint");

            }

            @Override
            public void onThirdQuartileReached() {
                trackEventImpressions(LMInBannerVideo.this.ad, "thirdQuartile");
            }
        });

        // Attach media controller view to video player
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;

        // Attach video player View inside VAST player View's hierarchy
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;

        return videoPlayer;
    }

    private void render(AdI ad) {
        this.ad = ad;
        playerView = createVideoPlayer(mContext);
        String url = ad.getAdRL();
        Uri uri = Uri.parse(url);
        playerView.load(uri);

    }

    public void play(){
        attachAd(playerView);
        playerView.play();

        if(null != mInBannerVideoListener){
            mInBannerVideoListener.onAdOpened(this);
        }
    }

    public void destroy() {
        if (playerView != null) {
            playerView.destroy();
            playerView = null;
        }
    }
    public interface InBannerVideoListener {
        void onAdReceived(LMInBannerVideo ad);

        void onAdFailed(LMInBannerVideo ad, Error error);

        void onAdOpened(LMInBannerVideo ad);

        void onAdClosed(LMInBannerVideo ad);

        void onAdCompletion(LMInBannerVideo ad);

    }

    public static class LMAdSize {

        public int width;
        public int height;

        public LMAdSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

}


