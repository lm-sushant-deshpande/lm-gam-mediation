package lemma.lemmavideosdk.vast.VastBuilder;

import java.util.ArrayList;

public class WrapperAd extends AdI {

    public void setImpTrackers(ArrayList<Tracker> impTrackers) {
        this.impTrackers = impTrackers;
    }

    ArrayList<Tracker> impTrackers = new ArrayList<>();

    @Override
    public ArrayList<Tracker> getAdTrackers() {
        return impTrackers;
    }
}
