package lemma.lemmavideosdk.vast.VastBuilder;

/**
 * Created by lemma on 16/04/18.
 */

public class MediaFile {

    String url;
    String width;
    String height;
    String type;

    public MediaFile() {

    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nURL : ").append(url);
        stringBuilder.append("\nWidth : ").append(width);
        stringBuilder.append("\nHeight : ").append(height);
        stringBuilder.append("\nType : ").append(type);
        return stringBuilder.toString();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
