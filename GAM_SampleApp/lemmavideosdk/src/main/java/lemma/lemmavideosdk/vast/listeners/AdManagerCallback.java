package lemma.lemmavideosdk.vast.listeners;

import lemma.lemmavideosdk.vast.manager.LMVideoAdManager;


abstract public class AdManagerCallback {

    /**
     * This method is called when SDK gets error while fetching Vast ad from Ad server
     *
     * @param adManager Instance of LMVideoAdManager related to Ad request
     * @param error     Occurred Error details
     */
    public abstract void onAdError(LMVideoAdManager adManager, Error error);

    /**
     * @param adManager
     * @deprecated use onAdEvent instead
     */
    @Deprecated
    public void onAdLoopComplete(LMVideoAdManager adManager) {

    }


    /**
     * @method onAdEvent()
     * @discussion This method is called for each event from AdEventType
     */
    public abstract void onAdEvent(final AD_EVENT event);

    public boolean shouldFireImpressions() {
        return true;
    }

    public enum AD_EVENT {
        AD_LOADED, AD_STARTED, AD_PAUSED, AD_RESUMED,
        AD_FIRST_QUARTILE, AD_MID_POINT, AD_THIRD_QUARTILE,
        AD_LOOP_COMPLETED, AD_COMPLETED
    }
}

