package lemma.lemmavideosdk.vast.VastBuilder;

/**
 * Created by lemma on 16/04/18.
 */

public class Resource {

    String creativeType;
    String type;
    String value;
    String width;
    String height;

    public Resource() {

    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nCreativeType : ").append(creativeType);
        stringBuilder.append("\nValue : ").append(value);
        stringBuilder.append("\nWidth : ").append(width);
        stringBuilder.append("\nHeight : ").append(height);
        stringBuilder.append("\nType : ").append(type);
        return stringBuilder.toString();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCreativeType() {
        return creativeType;
    }

    public void setCreativeType(String creativeType) {
        this.creativeType = creativeType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

}
