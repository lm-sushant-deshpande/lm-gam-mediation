package lemma.lemmavideosdk.vast.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;

import lemma.lemmavideosdk.common.AppLog;
import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.common.LMUtils;
import lemma.lemmavideosdk.common.LemmaSDK;
import lemma.lemmavideosdk.vast.VastBuilder.AdGroup;
import lemma.lemmavideosdk.vast.VastBuilder.AdI;
import lemma.lemmavideosdk.vast.VastBuilder.LinearAd;
import lemma.lemmavideosdk.vast.VastBuilder.MediaFile;
import lemma.lemmavideosdk.vast.VastBuilder.NonLinearAd;
import lemma.lemmavideosdk.vast.VastBuilder.Resource;
import lemma.lemmavideosdk.vast.VastBuilder.Tracker;
import lemma.lemmavideosdk.vast.VastBuilder.Vast;
import lemma.lemmavideosdk.vast.listeners.AdManagerCallback;
import lemma.lemmavideosdk.vast.tracker.TrackerDBHandler;
import lemma.lemmavideosdk.vast.tracker.TrackerHandler;
import lemma.lemmavideosdk.views.AdGroupPlayerView;

public class LMSharedVideoManager implements AdQueueManager.AdQueueManagerListener {

    private class LMUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        public Thread.UncaughtExceptionHandler exceptionHandler;

        public LMUncaughtExceptionHandler() {
        }

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            LMLog.e("UncaughtExceptionHandler","Thread: "+thread.toString()+"Exception :"+throwable.getLocalizedMessage());
            if (exceptionHandler != null) {
                exceptionHandler.uncaughtException(thread, throwable);
            }
        }
    }

    private void installGlobalExceptionHandler() {
        Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        LMUncaughtExceptionHandler uncaughtExceptionHandler = new LMUncaughtExceptionHandler();
        uncaughtExceptionHandler.exceptionHandler = exceptionHandler;
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

    private static volatile LMSharedVideoManager sSoleInstance = new LMSharedVideoManager();
    String TAG = "LMSharedVideoManager";
    AdGroupPlayerView mAdGroupPlayerView;
    private LMSharedVideoManagerPrefetchCallback prefetchCallback;
    private Context mContext = null;
    private AdManagerCallback mAdManagerListener = null;
    private LMVideoAdManager.AD_STATE mCurrentAdState = LMVideoAdManager.AD_STATE.IDLE;
    private AdQueueManager adQueueManager;
    private LMVideoAdManager.MANAGER_STATE managerState = LMVideoAdManager.MANAGER_STATE.IDLE;
    private TrackerHandler trackerHandler;

    private AdGroup currentAdGroup;
    private ViewGroup savedViewGroup;
    private AdManagerCallback savedAdManagerListener;

    private Boolean isPrepared = false;
    private LMConfig lmConfig;
    private LMAdRequest lmAdRequest;
    private Vast mVast;
    private int retryCount = 2;
    private boolean prefetchNextLoop = false;

    //private constructor.
    private LMSharedVideoManager() {
    }

    public static LMSharedVideoManager getInstance() {
        if (sSoleInstance == null){
            sSoleInstance = new LMSharedVideoManager();
        }
        return sSoleInstance;
    }

    public int getRetryCount() {
        return retryCount;
    }

    /*
    Set count for retrying for getting valid ad if not ad is not received in first attempt.
    When count is exhausted, onFailure callback will be called to notify error
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    /*
    If set to true, video manager pre-fetches next ad loop & continues playing next loop when current loop exhausts.
    Manager considers false as default value.
    Note: This setter should be called before #LMSharedVideoManager.prepare(), else it will not have any effect
     */
    public void setPrefetchNextLoop(boolean prefetchNextLoop) {
        this.prefetchNextLoop = prefetchNextLoop;
    }

    public void prefetch(LMSharedVideoManagerPrefetchCallback callback) {
        createAdQueueManagerIfNotExist();
        prefetchCallback = callback;
        loadNextAd();
    }

    private void cancelPrefetch() {
        if (adQueueManager != null) {
            prefetchCallback = null;
            adQueueManager.destroy();
            adQueueManager = null;
        }
    }

    /**
     * Constructor will initialize the passback handler & will download the ad
     * serving config properties from the CDN.
     *
     * @param context
     * @param request
     * @param config
     * @throws IllegalArgumentException
     */
    private void init(Context context,
                      LMAdRequest request,
                      LMConfig config) throws IllegalArgumentException {

        installGlobalExceptionHandler();
        if (context == null) {
            throw new IllegalArgumentException("Argument of LMVideoAdManager constructor can not be null. These are mandatory parameters.");
        }

        LemmaSDK.init(context, true);

        if (request.getAdUnitId() == null || request.getPublisherId() == null) {
            LMLog.e(TAG, "Publisher Id and Ad unit id should be provided.");
            throw new IllegalArgumentException("Publisher Id and Ad unit id should be provided.");
        }

        Vast defaultVast = null;
        // Create default config if not provided
        if (config == null) {
            config = new LMConfig();
            config.deleteCacheContinuously = false;
            config.playLastSavedLoop = false;
        }

        if (config.playLastSavedLoop) {
            Vast vast = savedVast(context);
            if (vast != null) {
                if (areAdsAvailableOnDisk(vast)) {
                    defaultVast = vast;
                }
            }
        }

        if (defaultVast == null) {
            defaultVast = config.vast;
        }

        if (defaultVast == null) {
            defaultVast = vastForInfo(config);
        }
        try {
            WebView webView = new WebView(context);
            trackerHandler = new TrackerHandler(new NetworkStatusMonitor(context), webView);
            trackerHandler.executeImpressionInWebContainer = config.getExecuteImpressionInWebContainer();
        }catch (Exception e){
            AppLog.e(TAG,e.getLocalizedMessage());
            trackerHandler = new TrackerHandler(new NetworkStatusMonitor(context));
        }

        try {
            TrackerDBHandler handler = new TrackerDBHandler(context);
            trackerHandler.trackerDBHandler = handler;
        } catch (Exception e) {
            LMLog.e(TAG, "Unable to create tracker db handler");
        }

        if (request.getTimeZone() != null) {
            trackerHandler.timeZone = request.getTimeZone();
        }

//        AdURLBuilder adURLBuilder = new AdURLBuilder(request);
//        adURLBuilder.advertisingIdClient = new AdvertisingIdClient(context);
//        adURLBuilder.displayMetrics = context.getResources().getDisplayMetrics();
//        adURLBuilder.appInfo = new LMAppInfo(context.getApplicationContext());
//        adURLBuilder.networkStatusMonitor = new NetworkStatusMonitor(context);
//        adURLBuilder.deviceInfo = new LMDeviceInfo(context);
//        adURLBuilder.locationManager = new LMLocationManager(context);
//
//        adQueueManager = new AdQueueManager(defaultVast, rootDirectory, adURLBuilder);
//        adQueueManager.monitor = new NetworkStatusMonitor(context);
//        adQueueManager.persistenceStore = new PersistenceStore(context);
//        adQueueManager.listener = this;
//        adQueueManager.deleteCacheContinuously = config.deleteCacheContinuously;

        mContext = context;
        lmAdRequest = request;
        mVast = defaultVast;
        lmConfig = config;
        createAdQueueManagerIfNotExist();
    }

    private void createAdQueueManagerIfNotExist() {
        if (adQueueManager != null) {
            return;
        }
        Context context = mContext;
        LMAdRequest request = lmAdRequest;
        Vast defaultVast = mVast;
        LMConfig config = lmConfig;

        String rootDirectory = null;
        if (rootDirectory == null) {
            rootDirectory = LMUtils.getLemmaRootDir();
        }

        AdURLBuilder adURLBuilder = new AdURLBuilder(request);
        adURLBuilder.advertisingIdClient = new AdvertisingIdClient(context);
        adURLBuilder.displayMetrics = context.getResources().getDisplayMetrics();
        adURLBuilder.appInfo = new LMAppInfo(context.getApplicationContext());
        adURLBuilder.networkStatusMonitor = new NetworkStatusMonitor(context);
        adURLBuilder.deviceInfo = new LMDeviceInfo(context);
        adURLBuilder.locationManager = new LMLocationManager(context);

        adQueueManager = new AdQueueManager(defaultVast, rootDirectory, adURLBuilder);
        adQueueManager.monitor = new NetworkStatusMonitor(context);
        adQueueManager.persistenceStore = new PersistenceStore(context);
        adQueueManager.listener = this;
        adQueueManager.deleteCacheContinuously = config.deleteCacheContinuously;
        adQueueManager.keepNextAdAsDefaults = true;
        adQueueManager.setPrefetchNextLoop(prefetchNextLoop);
        adQueueManager.deviceInfo = new LMDeviceInfo(context);
        adQueueManager.executeImpressionInWebContainer = config.getExecuteImpressionInWebContainer();
        adQueueManager.setRetryWithRTB(true);
    }

    public void prepare(Context context, LMAdRequest request,
                        LMConfig config) {
        if (isPrepared) {
            return;
        }
        init(context.getApplicationContext(), request, config);
        isPrepared = true;
    }

    private Vast vastForInfo(LMConfig info) {

        try {

            ArrayList<AdI> linearAds = new ArrayList<>();

            if (info.uri != null) {

                LinearAd ad = new LinearAd();
                ad.id = "1";
                ad.sequence = 1;
                ad.setDurationString(info.duration);


                ArrayList<MediaFile> mediaFiles = new ArrayList<>();

                MediaFile mf = new MediaFile();
                mf.setUrl(info.uri.toString());

                mf.setWidth("320");
                mf.setHeight("480");
                mf.setType("video/mp4");

                mediaFiles.add(mf);

                ad.setMediaFiles(mediaFiles);
                linearAds.add(ad);

            } else if (info.imageUri != null) {
                NonLinearAd ad = new NonLinearAd();
                ad.id = "1";
                ad.sequence = 1;

                Resource r = new Resource();
                r.setType("StaticResource");
                r.setCreativeType("image/png");
                r.setValue(info.imageUri.toString());
                r.setWidth("320");
                r.setHeight("480");
                ArrayList<Resource> resources = new ArrayList<>();
                resources.add(r);
                ad.setResources(resources);

                linearAds.add(ad);
            }

            if (linearAds.size() > 0) {
                Vast vast = new Vast();
                vast.ads = linearAds;
                return vast;
            }
            return null;

        } catch (Exception i) {
            LMLog.i(TAG, i.getLocalizedMessage());
        }
        return null;
    }

    public LMAdLoopStat getCurrentAdLoopStat() {
        if (adQueueManager != null) {

            return adQueueManager.getCurrentAdQueueStat();
        }
        return new LMAdLoopStat();
    }

    private void pauseLoop() {
        managerState = LMVideoAdManager.MANAGER_STATE.PAUSED;
    }

    private void resumeLoop() {
        if (managerState == LMVideoAdManager.MANAGER_STATE.PLAYING) {
            return;
        }
        managerState = LMVideoAdManager.MANAGER_STATE.PLAYING;
        if (mAdManagerListener != null) {
            mAdManagerListener.onAdEvent(AdManagerCallback.AD_EVENT.AD_RESUMED);
        }
        loadNextAd();
    }

    private Vast savedVast(Context context) {
        PersistenceStore persistenceStore = new PersistenceStore(context);
        Vast vast = persistenceStore.retrieve();
        if (vast != null && vast.ads.size() > 0) {
            LMLog.i(TAG, "Found persisted Vast -> " + vast);
            return vast;
        }
        return null;
    }

    private boolean areAdsAvailableOnDisk(Vast vast) {

        boolean areAdsAvailableOnDisk = true;
        for (AdI ad : vast.ads) {
            File file = new File(ad.getAdRL());
            if (ad.isUrl() && !file.exists()) {
                areAdsAvailableOnDisk = false;
                break;
            }
        }
        return areAdsAvailableOnDisk;
    }

    private void loadNextAd() {

        if (managerState == LMVideoAdManager.MANAGER_STATE.PAUSED) {
            // Wait until resumed again
            if (mAdManagerListener != null) {
                mAdManagerListener.onAdEvent(AdManagerCallback.AD_EVENT.AD_PAUSED);
            }
            return;
        }
        if (adQueueManager == null) {
            return;
        }

        adQueueManager.nextAdGroup(new AdQueueManager.AdQueueManagerQueueListener() {

            @Override
            public void onQueueError(final Error error) {
                retry();
            }

            @Override
            public void onQueueReady(AdGroup adGroup) {

                if (adGroup == null) {
                    // Retry
                    if (mAdManagerListener != null) {
                        LMLog.i(TAG, "AD_LOOP_COMPLETED");
                        mAdManagerListener.onAdEvent(AdManagerCallback.AD_EVENT.AD_LOOP_COMPLETED);
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            loadNextAd();
                        }
                    });

                } else if (adGroup.ads.size() == 0) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            loadNextAd();
                        }
                    });

                } else {

                    if (managerState == LMVideoAdManager.MANAGER_STATE.PAUSED) {
                        // Wait until resumed again\
                        return;
                    }

                    currentAdGroup = adGroup;
                    mCurrentAdState = LMVideoAdManager.AD_STATE.LOADED;
                    if (prefetchCallback != null) {
                        retryCount = 2;
                        prefetchCallback.onSuccess();
                        prefetchCallback = null;
                    } else {

                        if (LMSharedVideoManager.this.savedViewGroup != null &&
                                LMSharedVideoManager.this.savedAdManagerListener != null) {
                            renderAdInView(LMSharedVideoManager.this.savedViewGroup,
                                    LMSharedVideoManager.this.savedAdManagerListener);

                        }
                    }

                }
            }
        });
    }

    private void retry() {

        final long period = 1000;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                if (LMSharedVideoManager.this.retryCount > 0) {
                    --LMSharedVideoManager.this.retryCount;
                    loadNextAd();
                } else {
                    if (prefetchCallback != null) {
                        prefetchCallback.onFailure();
                        prefetchCallback = null;
                    }
                }
            }
        }, period);
    }

    public void renderAdInView(ViewGroup viewGroup, AdManagerCallback listener) {

        try {
            renderAdInViewInternal(viewGroup, listener);
        }catch (Exception e){
            LMLog.e("%s",e.getLocalizedMessage());
        }
    }

    private void renderAdInViewInternal(ViewGroup viewGroup, AdManagerCallback listener) {

        if (adQueueManager == null) {
            LMLog.e(TAG, "renderAdInView called without preparing & prefetching");
            return;
        }

        mAdManagerListener = listener;
        this.savedViewGroup = viewGroup;
        this.savedAdManagerListener = listener;

        AdGroup adGroup = currentAdGroup;

        ViewGroup videoAdUiContainer = viewGroup;
        //mVAdParam.linearViewInfo.getVideoAdUiContainer();
        AdGroupPlayerView oldAdGroupV = mAdGroupPlayerView;

        mAdGroupPlayerView = new AdGroupPlayerView(mContext, adGroup);
        mAdGroupPlayerView.setPlayerViewListener(new AdGroupPlayerView.AdGroupPlayerViewListener() {
            @Override
            public void onSuccessLayout() {

            }

            @Override
            public void onFailureLayout(String reason) {

            }

            @Override
            public void endPlayingAdGroup(AdGroup adGroup) {

                if (adQueueManager != null && adQueueManager.getCurrentAdQueueStat().isLoopEmpty()) {
                    if (mAdManagerListener != null) {
                        LMLog.i(TAG, "AD_LOOP_COMPLETED");
                        mAdManagerListener.onAdEvent(AdManagerCallback.AD_EVENT.AD_LOOP_COMPLETED);
                    }
                }
                loadNextAd();
            }

            @Override
            public void endPlayingAd(AdGroup adGroup, AdI ad) {
                trackEventImpressions(ad, "complete", adGroup.isRTB);
                if (mAdManagerListener != null) {
                    mAdManagerListener.onAdEvent(AdManagerCallback.AD_EVENT.AD_COMPLETED);
                }

            }

            @Override
            public void startedPlayingAd(AdGroup adGroup, AdI ad) {
                trackImpressions(ad,adGroup.isRTB);
                trackEventImpressions(ad, "start",adGroup.isRTB);
                if (mAdManagerListener != null) {
                    mAdManagerListener.onAdEvent(AdManagerCallback.AD_EVENT.AD_STARTED);
                }
            }

            @Override
            public void onFirstQuartileReached(AdGroup adGroup, AdI ad) {
                trackEventImpressions(ad, "firstQuartile",adGroup.isRTB);
                if (mAdManagerListener != null) {
                    mAdManagerListener.onAdEvent(AdManagerCallback.AD_EVENT.AD_FIRST_QUARTILE);
                }
            }

            @Override
            public void onMidPointReached(AdGroup adGroup, AdI ad) {
                trackEventImpressions(ad, "midpoint",adGroup.isRTB);
                if (mAdManagerListener != null) {
                    mAdManagerListener.onAdEvent(AdManagerCallback.AD_EVENT.AD_MID_POINT);
                }
            }

            @Override
            public void onThirdQuartileReached(AdGroup adGroup, AdI ad) {
                trackEventImpressions(ad, "thirdQuartile",adGroup.isRTB);
                if (mAdManagerListener != null) {
                    mAdManagerListener.onAdEvent(AdManagerCallback.AD_EVENT.AD_THIRD_QUARTILE);
                }
            }
        });

        mAdGroupPlayerView.prepareLayoutForAdGroup(adGroup);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        videoAdUiContainer.addView(mAdGroupPlayerView, params);


        if (oldAdGroupV != null) {
            videoAdUiContainer.removeView(oldAdGroupV);
            oldAdGroupV.reset();
        }

        mAdGroupPlayerView.prepareLayoutForAdGroup(adGroup);
        startAd();
    }

    private void trackImpressions(AdI ad, boolean isRTB) {
        ArrayList<Tracker> impressionList = ad.getAdTrackers();
        sendImpression(impressionList, isRTB);
    }

    private void trackEventImpressions(AdI ad, String event, boolean isRTB) {
        ArrayList<Tracker> eventTrackers = ad.getEventTrackers(event);
        LMLog.d(TAG, "Scheduling tracker execution " + event + " for event : " + eventTrackers);
        sendImpression(eventTrackers,isRTB);
    }

    private void sendImpression(ArrayList<Tracker> trackers, boolean isRTB) {
        if (isRTB){
            trackerHandler.sendRTBImpression(trackers);
        } else if (mAdManagerListener != null) {
            if (mAdManagerListener.shouldFireImpressions()) {
                trackerHandler.sendImpression(trackers);
            } else {
                LMLog.d("LMVideoAdManager", "App restricted impression tracking, is display off/not connected ?");
            }
        }
    }

    /**
     * Publisher should call this method to start/play the ad.
     */
    private void startAd() {

        //Render views
        //Notify AdManagerListener about pausing content
        mCurrentAdState = LMVideoAdManager.AD_STATE.STARTED;
        managerState = LMVideoAdManager.MANAGER_STATE.PLAYING;
        //Play ad
        startPlaying();
    }


    private void stopRendering() {
        if (mAdGroupPlayerView != null) {
            mAdGroupPlayerView.reset();
            if (mAdGroupPlayerView.getParent() != null){
                ((ViewGroup)mAdGroupPlayerView.getParent()).removeView(mAdGroupPlayerView);

            }
        }
    }

    public void destroySharedInstance(){
        destroyIfNeeded();
        stopRendering();
        cancelPrefetch();
        sSoleInstance = null;
    }

    private void destroyIfNeeded() {
        pauseLoop();
        if (adQueueManager != null) {
            adQueueManager.destroy();
            mAdManagerListener = null;
        }
        if (mAdGroupPlayerView != null) {
            mAdGroupPlayerView.reset();
        }
    }


    /**
     * Video ad serving passes thru the multiple states of ad life cycle. It
     * returns the current state of the ad.
     *
     * @return current ad state.
     */
    public LMVideoAdManager.AD_STATE getCurrentAdState() {
        return mCurrentAdState;
    }

    /**
     * It will actually start playing linear/non-linear creative.
     */
    private void startPlaying() {
        if (mAdGroupPlayerView != null) {
            mAdGroupPlayerView.play();
        } else {
            LMLog.e(TAG, "mAdGroupPlayerView  is null");
        }
    }

    /**
     * It will send the error tracking url for given error code. It will send
     * the tracking url for inline, all wrappers & passback VAST as well.
     *
     * @param errorCode
     */
    private void sendErrorTracking(final int errorCode) {
    }

    @Override
    public void onLoopStart() {
        trackerHandler.sendImpressionFromQueue();
    }

    public void setPlayerVisibility(int visibility) {
        if (mAdGroupPlayerView != null) {
            mAdGroupPlayerView.setVisibility(visibility);
        }
    }

    public enum AD_STATE {
        IDLE, INIT, REQUESTED, RECEIVED, LOADED, STARTED, PAUSED, RESUME,
        MUTE, UNMUTE, SKIP, FIRST_QUARTILE, MID_POINT, THIRD_QUARTILE,
        COMPLETED, ERROR, AD_LOOP_COMPLETED
    }

    public enum MANAGER_STATE {
        IDLE, PLAYING, PAUSED
    }

    public interface LMSharedVideoManagerPrefetchCallback {
        void onSuccess();

        void onFailure();
    }

}
