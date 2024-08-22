package lemma.lemmavideosdk.vast.manager;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.common.LMUtils;
import lemma.lemmavideosdk.vast.VastBuilder.AdGroup;
import lemma.lemmavideosdk.vast.VastBuilder.AdI;
import lemma.lemmavideosdk.vast.VastBuilder.RtBMarkerAdGroup;
import lemma.lemmavideosdk.vast.VastBuilder.Vast;

public class LMLoop {

    static String TAG = "LMLoop";
    public PersistenceStore persistenceStore;
    LMAdLoopStat adQueueStat = new LMAdLoopStat();
    AdURLBuilder adURLBuilder;
    String rootDirectory;

    public Queue<Vast> getRtbVasts() {
        return rtbVasts;
    }

    public void setRtbVasts(Queue<Vast> rtbVasts) {
        this.rtbVasts = rtbVasts;
    }

    Queue<Vast> rtbVasts = new LinkedList<>();
    Vast vast = null;
    boolean isPrepared = false;
    boolean isPartiallyPrepared = false;
    boolean deleteCacheContinuously = false;
    LMLoopPreparationCallback preparationCallback;
    List<AdGroup> directAdGroups = Collections.synchronizedList(new ArrayList<AdGroup>());
    List<AdGroup> realTimeAdGroups = Collections.synchronizedList(new ArrayList<AdGroup>());
    private AdLoader loopAdLoader;
    private RtbAdLoader realTimeAdsLoader;

    private LMDeviceInfo deviceInfo;
    private Boolean executeImpressionInWebContainer = false;

    private Integer preFetchRTBCount = 2;
    private Integer currentPendingRTB = 0;

    public void setDefaultAd(Vast defaultAd) {
        this.defaultAd = defaultAd;
        isPartiallyPrepared = true;
        fetchRealTimeAds(defaultAd);
    }

    Vast defaultAd;
    Vast fallbackAd;

    public boolean isLoadingInProgress() {
        return loadingInProgress;
    }

    private boolean loadingInProgress;

    public Vast getFallbackAd() {
        if (fallbackAd == null) return defaultAd;
        return fallbackAd;
    }

    public LMLoop(final NetworkStatusMonitor monitor, String rootDirectory,
                  LMDeviceInfo deviceInfo,
                  boolean isRetryWithRTB,
                  Boolean executeImpressionInWebContainer) {

        this.deviceInfo = deviceInfo;
        this.executeImpressionInWebContainer = executeImpressionInWebContainer;
        this.rootDirectory = rootDirectory;
        LMLog.i(TAG, "New loop created");
        loopAdLoader = new AdLoader(new AdLoader.AdLoaderListener<Vast>() {
            @Override
            public void onSuccess(final Vast vast) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        loadingInProgress = false;
                        enqueueAdVast(vast);
                        LMLoop.this.vast = vast;
                        if (preparationCallback != null) {
                            isPrepared = true;
                            preparationCallback.onSuccess(LMLoop.this);
                        }

                        if (preparationCallback == null) {
                            isPartiallyPrepared = true;
                        }
                        loopAdLoader = null;
                    }
                });
            }

            @Override
            public void onError(final Error err) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        loadingInProgress = false;

                        if (fallbackAd != null){

                            LMLog.i(TAG, "Using fallback ad due to error: "+err.getLocalizedMessage());
                            directAdGroups = AdGroup.adGroups(fallbackAd, fallbackAd.customLayoutExt);
                            adQueueStat.currentAdLoopLength = directAdGroups.size();

                            LMLoop.this.vast = fallbackAd;

                            if (preparationCallback != null) {
                                isPrepared = true;
                                preparationCallback.onSuccess(LMLoop.this);
                            }

                            if (preparationCallback == null) {
                                isPartiallyPrepared = true;
                            }
                            loopAdLoader = null;

                        }else {
                            LMLog.e(TAG, err.getLocalizedMessage());

                            if (preparationCallback != null) {
                                preparationCallback.onError(err);
                            }
                        }
                    }
                });

            }
        });
        loopAdLoader.setRetryWithRTB(isRetryWithRTB);
        loopAdLoader.deviceInfo = this.deviceInfo;
        loopAdLoader.setExecuteImpressionInWebContainer(executeImpressionInWebContainer);
        loopAdLoader.monitor = monitor;
        loopAdLoader.downloadManager = new DownloadManager(rootDirectory + "direct/");
    }

    public Vast getVast() {
        return vast;
    }

    private void enqueueAdVast(Vast newVast) {
        if (newVast.ads.size() == 0) {
            LMLog.i(TAG, "Vast with zero ads");
            return;
        }
        directAdGroups = AdGroup.adGroups(newVast, newVast.customLayoutExt);
        adQueueStat.currentAdLoopLength = directAdGroups.size();
        fetchRealTimeAds(newVast);
        if (!newVast.isRTB) {
            saveVast(newVast);
        }
        Queue<Vast> vasts = new LinkedList<>();
        vasts.add(newVast);
        scheduleDeletionIfNeeded(vasts, rootDirectory + "direct/");

        Queue<Vast> rtbVasts = new LinkedList<>();
        rtbVasts.addAll(this.rtbVasts);
        scheduleDeletionIfNeeded(rtbVasts, rootDirectory + "rtb/");
    }

    private void scheduleDeletionIfNeeded(Queue<Vast> vastQ, String path) {

        // Check if at least 40% free memory is available on device, if available then do not delete,
        // Delete max 40 files at time
        if (deleteCacheContinuously || !LMUtils.isDeviceMemoryAvailable(0.4f)) {
            ArrayList<Vast> recentVasts = new ArrayList<>();
            recentVasts.addAll(vastQ);
            ArrayList<File> files = LMUtils.listFiles(new File(path), filePaths(recentVasts), 0, 40);
            LMLog.i(TAG, "Deleting files " + files.size() + files);
            LMUtils.delete(files);
        }
    }

    private ArrayList<String> filePaths(ArrayList<Vast> vasts) {

        ArrayList<String> filePaths = new ArrayList<>();

        for (Vast v : vasts) {
            for (AdI ad : v.ads) {

                if (ad.getAdRL() != null) {
                    filePaths.add(ad.getAdRL());
                }
            }
        }
        return filePaths;
    }

    private void fetchRealTimeAds(Vast vast) {
        if (vast.extPodSize != null) {
            Integer extra = vast.extPodSize - vast.ads.size();
            currentPendingRTB = extra;
            fetchNextRTBAds();
        }
    }

    private void fetchRealTimeAds(final int count) {
        if (realTimeAdsLoader == null) {
            String rootDirectory = this.rootDirectory + "rtb/";
            realTimeAdsLoader = new RtbAdLoader(new DownloadManager(rootDirectory), new RtbAdLoader.RtbAdLoaderListener() {
                @Override
                public void onAdReceived(Vast vast) {
                    List<AdGroup> _realTimeAdGroups = AdGroup.adGroupsForAd(vast.ads, true);
                    currentPendingRTB -= _realTimeAdGroups.size();

                    realTimeAdGroups.addAll(_realTimeAdGroups);
                    LMLog.i(TAG, "RTB ad queue update\n Current length: " + realTimeAdGroups.size()+
                            ",\n Deferred pending count: "+currentPendingRTB);

                    rtbVasts.add(vast);
                    if (rtbVasts.size() > 4) {
                        rtbVasts.remove();
                    }
                }
            });
            realTimeAdsLoader.executeImpressionInWebContainer = executeImpressionInWebContainer;
            realTimeAdsLoader.deviceInfo = this.deviceInfo;
        }

        Map map = new HashMap();
        map.put("rtb", "1");
        String rtbURL = adURLBuilder.build(map);
        LMLog.i(TAG, "Initiated RTB ad fetching for count - "+count);
        realTimeAdsLoader.loadAds(rtbURL, count);
    }

    private void saveVast(Vast vast) {
        if (persistenceStore != null) {
            persistenceStore.save(vast);
            LMLog.i(TAG, "Persisted Vast");
        }
    }

    private void fetchNextRTBAds(){
        if (realTimeAdGroups.size() <=1){
            Integer rbtFetchCount = preFetchRTBCount;
            if (currentPendingRTB < rbtFetchCount){
                rbtFetchCount = currentPendingRTB;
            }
            if (rbtFetchCount != 0){
                fetchRealTimeAds(rbtFetchCount);
            }
        }
    }

    private AdGroup popAdGroupIfAvailable() {
        if (directAdGroups.size() > 0) {

            AdGroup group = directAdGroups.get(0);
            directAdGroups.remove(group);

            adQueueStat.currentAdIndex = adQueueStat.currentAdLoopLength - directAdGroups.size();
            adQueueStat.remainingAdLoopSize = directAdGroups.size();

            if (group instanceof RtBMarkerAdGroup) {
                if (realTimeAdGroups.size() > 0) {
                    group = realTimeAdGroups.get(0);
                    realTimeAdGroups.remove(group);

                    fetchNextRTBAds();

                    adQueueStat.currentAdIndex = adQueueStat.currentAdLoopLength - directAdGroups.size();
                    LMLog.i(TAG, "RTB ad Popped [" + adQueueStat.currentAdIndex + "] of " + adQueueStat.currentAdLoopLength);
                    adQueueStat.adGroup = group;
                    return group;
                } else {
                    LMLog.i(TAG, "Found RTB Ad Marker but no RTB ad is available, jumping to next ad");
                    return popAdGroupIfAvailable();
                }
            } else {
                LMLog.i(TAG, "Direct ad Popped [" + adQueueStat.currentAdIndex + "] of " + adQueueStat.currentAdLoopLength);
                adQueueStat.adGroup = group;
                return group;
            }
        } else {
            return null;
        }
    }

    public boolean exhausted() {
        return (loopAdLoader == null && directAdGroups.isEmpty());
    }

    public boolean prepared() {
        return isPrepared;
    }

    public void prepare(HashMap map, LMLoopPreparationCallback callback) {
        preparationCallback = callback;
        if (isPrepared) {
            preparationCallback.onSuccess(this);
        } else {

            if (isPartiallyPrepared) {

                if (defaultAd != null){
                    LMLog.i(TAG, "Using default ad");

                    directAdGroups = AdGroup.adGroups(defaultAd, defaultAd.customLayoutExt);
                    adQueueStat.currentAdLoopLength = directAdGroups.size();
                    LMLoop.this.vast = defaultAd;
                    loopAdLoader = null;
                }
                isPrepared = true;
                preparationCallback.onSuccess(this);
            } else {
                loadingInProgress = true;
                loopAdLoader.load(adURLBuilder.build(map));
            }
        }
    }

    public AdGroup nextAdGroup() {
        return popAdGroupIfAvailable();
    }

    public void destroy() {
        if (realTimeAdsLoader != null) {
            realTimeAdsLoader.reset();
        }
    }

    interface LMLoopPreparationCallback {
        void onSuccess(LMLoop loop);

        void onError(Error error);
    }
}
