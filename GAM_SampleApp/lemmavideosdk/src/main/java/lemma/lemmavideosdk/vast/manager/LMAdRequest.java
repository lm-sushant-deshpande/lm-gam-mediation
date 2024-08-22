package lemma.lemmavideosdk.vast.manager;

import java.util.HashMap;
import java.util.TimeZone;

public class LMAdRequest {

    public HashMap<String, String> map;
    private String publisherId;
    private String adUnitId;
    public boolean isInterstitial = false;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    int width;
    int height;

    public boolean isCoppaEnabled() {
        return coppaEnabled;
    }

    public void setCoppaEnabled(boolean coppaEnabled) {
        this.coppaEnabled = coppaEnabled;
    }

    boolean coppaEnabled = false;
    private TimeZone timeZone;
    private String adServerBaseURL;

    public LMAdRequest(String publisherId, String adUnitId) {
        this.publisherId = publisherId;
        this.adUnitId = adUnitId;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public String getAdServerBaseURL() {
        return adServerBaseURL;
    }

    public void setAdServerBaseURL(String adServerBaseURL) {
        this.adServerBaseURL = adServerBaseURL;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public String getAdUnitId() {
        return adUnitId;
    }
}
