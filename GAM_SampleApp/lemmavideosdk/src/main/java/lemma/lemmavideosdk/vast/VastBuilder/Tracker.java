package lemma.lemmavideosdk.vast.VastBuilder;

/**
 * Created by lemma on 16/04/18.
 */

public class Tracker {

    String url;
    String event;

    public Tracker() {

    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nURL : ").append(url);
        return stringBuilder.toString();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
