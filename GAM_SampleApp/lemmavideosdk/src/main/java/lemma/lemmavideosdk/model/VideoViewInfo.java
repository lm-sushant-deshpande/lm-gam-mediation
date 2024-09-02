package lemma.lemmavideosdk.model;

import android.view.ViewGroup;

import lemma.lemmavideosdk.vast.listeners.AdPlayerAdapter;

public class VideoViewInfo {

    private ViewGroup mVideoAdUiContainer = null;
    private AdPlayerAdapter mPlayerAdapter = null;

    public VideoViewInfo() {

    }

    /**
     * @return the playerAdapter
     */
    public AdPlayerAdapter getPlayerAdapter() {
        return mPlayerAdapter;
    }

    /**
     * @param playerAdapter the playerAdapter to set
     */
    public void setPlayerAdapter(AdPlayerAdapter playerAdapter) {
        this.mPlayerAdapter = playerAdapter;
    }

    /**
     * @return the videoAdUiContainer
     */
    public ViewGroup getVideoAdUiContainer() {
        return mVideoAdUiContainer;
    }

    /**
     * @param videoAdUiContainer the videoAdUiContainer to set
     */
    public void setVideoAdUiContainer(ViewGroup videoAdUiContainer) {
        this.mVideoAdUiContainer = videoAdUiContainer;
    }

}
