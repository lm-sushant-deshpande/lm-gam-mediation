package lemma.lemmavideosdk.vast.VastBuilder;

import java.util.ArrayList;

import lemma.lemmavideosdk.common.DisplayableAdI;

public class Vast implements DisplayableAdI {

    @Override
    public DisplayableAdI.Type getType() {
        return Type.Video;
    }

    public Integer extPodSize = null;
    public boolean isRTB = false;
    public String altSequence = null;
    public ArrayList<ArrayList<AdI.Frame>> customLayoutExt;
    public ArrayList<AdI> ads = new ArrayList();

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n Layout exts :").append(customLayoutExt);
        stringBuilder.append("\n Pod Size :").append(extPodSize);
        for (AdI ad : ads) {
            stringBuilder.append("\n").append(ad.toString());
        }
        return stringBuilder.toString();
    }

    public boolean isEmpty() {
        return (ads.isEmpty());
    }
}
