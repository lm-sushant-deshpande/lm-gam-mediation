package lemma.lemmavideosdk.vast.manager;

import android.net.Uri;

import lemma.lemmavideosdk.vast.VastBuilder.Vast;

public class LMConfig {
    Uri uri;
    Vast vast;
    boolean playLastSavedLoop;
    String duration;

    Uri imageUri;
    String imagedDuration;
    Boolean deleteCacheContinuously = false;

    // Executes impression tracker in web context, default value is false
    public void setExecuteImpressionInWebContainer(Boolean executeImpressionInWebContainer) {
        this.executeImpressionInWebContainer = executeImpressionInWebContainer;
    }

    public Boolean getExecuteImpressionInWebContainer() {
        return executeImpressionInWebContainer;
    }

    private Boolean executeImpressionInWebContainer = false;

    public Boolean getDeleteCacheContinuously() {
        return deleteCacheContinuously;
    }

    public void setDeleteCacheContinuously(Boolean deleteCacheContinuously) {
        this.deleteCacheContinuously = deleteCacheContinuously;
    }

    public void setPlayLastSavedLoop(boolean playLastSavedLoop) {
        this.playLastSavedLoop = playLastSavedLoop;
    }

    public Uri getVideoUri() {
        return uri;
    }

    public void setPlaceHolderVideo(Uri uri, long duration) {
        this.uri = uri;
        this.duration = "00:00:" + duration;
    }

    public void setPlaceHolderImage(Uri uri, long duration) {
        if (this.uri == null) {
            this.imageUri = uri;
            this.imagedDuration = "00:00:" + duration;
        }
    }

    public void setPlaceHolderVast(Vast vast) {
        this.vast = vast;
    }

    public String getDuration() {
        return duration;
    }
}
