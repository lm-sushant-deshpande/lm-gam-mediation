package lemma.lemmavideosdk.vast.VastBuilder;

import java.util.ArrayList;

public class AdI {

    public AdI parent;

    public Frame frame = null;
    public String id = null;
    public Integer sequence = null;

    public String adTagUrl = null;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nId :").append(id);
        stringBuilder.append("\nSequence :").append(sequence);
        return stringBuilder.toString();
    }

    public boolean isUrl() {
        return true;
    }

    public AdType getType() {
        return AdType.LINEAR;
    }

    public String getAdRL() {
        return "";
    }

    public void setAdRL(String url) {

    }

    // In milliseconds
    public long duration() {
        return 10000;
    }

    public String mimeType() {
        return "";
    }

    public ArrayList<Tracker> getAdTrackers() {
        return new ArrayList<>();
    }

    public ArrayList<Tracker> getCombinedAdTrackers() {
        return new ArrayList<>();
    }

    public ArrayList<Tracker> getEventTrackers(String event) {
        return new ArrayList<>();
    }

    public static class Frame {

        public int id;
        public int startX;
        public int endX;
        public int startY;
        public int endY;

        public Frame(int sx, int ex, int sy, int ey) {
            startX = sx;
            startY = sy;
            endY = ey;
            endX = ex;
        }

        @Override
        public String toString() {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n Id :").append(id);
            stringBuilder.append("\n startX :").append(startX);
            stringBuilder.append("\n startY :").append(startY);
            stringBuilder.append("\n endX :").append(endX);
            stringBuilder.append("\n endY :").append(endY);
            return stringBuilder.toString();
        }
    }


}
