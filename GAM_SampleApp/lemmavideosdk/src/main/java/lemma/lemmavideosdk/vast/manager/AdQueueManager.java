package lemma.lemmavideosdk.vast.manager;

import java.util.ArrayList;
import java.util.HashMap;

import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.vast.VastBuilder.AdGroup;
import lemma.lemmavideosdk.vast.VastBuilder.Vast;

class AdQueueManager {

    static String TAG = "AdQueueManager";
    public AdQueueManagerListener listener;
    public NetworkStatusMonitor monitor;
    public PersistenceStore persistenceStore;
    public Boolean deleteCacheContinuously = false;
    public Boolean keepNextAdAsDefaults = true;
    String rootDirectory;
    AdURLBuilder adURLBuilder;
    ArrayList<AdGroup> defaultAdGroups = new ArrayList<AdGroup>();
    LMDeviceInfo deviceInfo;
    Boolean executeImpressionInWebContainer = false;

    private LMLoop currentLoop;
    private LMLoop nextLoop;
    private boolean retryWithRTB = false;
    Vast defaultAd;
    Vast earlierVast;
    private boolean prefetchNextLoop = false;

    public void setPrefetchNextLoop(boolean prefetchNextLoop) {
        this.prefetchNextLoop = prefetchNextLoop;
    }

    public AdQueueManager(Vast defaultAd, String rootDirectory, AdURLBuilder adURLBuilder) {
        this.defaultAd = defaultAd;
        if (defaultAd != null) {
            defaultAdGroups = AdGroup.adGroupsForAd(defaultAd.ads);
        }
        this.rootDirectory = rootDirectory;
        this.adURLBuilder = adURLBuilder;
    }

    public boolean isRetryWithRTB() {
        return retryWithRTB;
    }

    public void setRetryWithRTB(boolean retryWithRTB) {
        this.retryWithRTB = retryWithRTB;
    }

    public LMAdLoopStat getCurrentAdQueueStat() {
        return currentLoop.adQueueStat;
    }

    public void nextAdGroup(final AdQueueManagerQueueListener ql) {
        nextAdGroup(ql, true);
    }

    private LMLoop newLoop(){
        LMLoop loop = new LMLoop(monitor, rootDirectory, deviceInfo,isRetryWithRTB(),executeImpressionInWebContainer);
        loop.adURLBuilder = adURLBuilder;
        loop.deleteCacheContinuously = deleteCacheContinuously;
        loop.persistenceStore = persistenceStore;
        return loop;
    }

    private void prepareNextLoop(LMLoop recentLoop) {
        earlierVast = recentLoop.getVast();
        if (nextLoop!=null && nextLoop.isLoadingInProgress()) {
            LMLog.i(TAG, "Loop loading is in progress, skipping creation");
            return;
        }
        String altSequence = "";
        if (recentLoop.getVast() != null) {
            altSequence = recentLoop.getVast().altSequence;
        }
        LMLoop loop = newLoop();

        if (keepNextAdAsDefaults) {
            loop.fallbackAd = recentLoop.getFallbackAd();
        }

        HashMap map = new HashMap<String, String>();
        if (altSequence != null && altSequence.length() > 0) {
            map.put("acs", altSequence);
        }
        loop.prepare(map, null);
        loop.setRtbVasts(recentLoop.getRtbVasts());
        nextLoop = loop;
    }

    private LMLoop defaultLoop(){
        LMLoop currentLoop = newLoop();
        if (earlierVast != null){
            currentLoop.setDefaultAd(earlierVast);
        }else if (defaultAd != null) {
            currentLoop.setDefaultAd(defaultAd);
        }
        return currentLoop;
    }

    public void nextAdGroup(final AdQueueManagerQueueListener ql, final boolean shouldCallback) {

        if (currentLoop == null) {
            currentLoop = newLoop();
            if (defaultAd != null) {
                currentLoop.setDefaultAd(defaultAd);
            }
        }

        if (currentLoop.prepared()) {

            if (currentLoop.exhausted()) {
                if (nextLoop != null) {
                    if (!nextLoop.isLoadingInProgress()){
                        currentLoop.destroy();
                        currentLoop = nextLoop;
                        nextAdGroup(ql, true);
                    }else {
                        currentLoop.destroy();
                        currentLoop = defaultLoop();
                        nextAdGroup(ql, true);
                    }
                } else {
                    ql.onQueueError(new Error("No Ads"));
                }
            } else {
                ql.onQueueReady(currentLoop.nextAdGroup());
                if (listener != null) {
                    listener.onLoopStart();
                    listener = null;
                }
            }
        } else {

                LMLog.i(TAG, "Preparing loop");
                currentLoop.prepare(new HashMap(), new LMLoop.LMLoopPreparationCallback() {
                    @Override
                    public void onSuccess(LMLoop loop) {
                        LMLog.i(TAG, "Preparing loop success");

                        if (prefetchNextLoop){
                            prepareNextLoop(loop);
                        }
                        nextAdGroup(ql, true);
                    }

                    @Override
                    public void onError(Error error) {
                        LMLog.i(TAG, "Preparing loop error");

                        ql.onQueueError(error);
                        if (currentLoop != null) {
                            currentLoop.destroy();
                            currentLoop = null;
                        }
                    }
                });

        }
    }

    public void destroy() {
        listener = null;
        monitor = null;
        persistenceStore = null;

        if (currentLoop != null) {
            currentLoop.destroy();
        }
        if (nextLoop != null) {
            nextLoop.destroy();
        }
    }

    interface AdQueueManagerQueueListener {
        void onQueueReady(AdGroup adGroup);
        void onQueueError(Error error);
    }

    public interface AdQueueManagerListener {
        void onLoopStart();
    }

}
