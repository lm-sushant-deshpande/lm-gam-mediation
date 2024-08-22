package lemma.lemmavideosdk.vast.VastBuilder;

import android.webkit.URLUtil;

import java.util.ArrayList;

import lemma.lemmavideosdk.common.LMUtils;

/**
 * Created by lemma on 16/04/18.
 */

public class NonLinearAd extends AdI {

    String type = "NonLinearAd";
    String minSuggestedDuration;
    ArrayList<Resource> resources = new ArrayList<>();
    ArrayList<Tracker> trackers = new ArrayList<>();
    ArrayList<Tracker> impTrackers = new ArrayList<>();


    public NonLinearAd() {


    }

    public NonLinearAd(Resource r) {
        ArrayList<Resource> resource = new ArrayList<>();
        resource.add(r);
        setResources(resource);
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString());
        stringBuilder.append("\nType : ").append(type);
        stringBuilder.append("\nDuration : ").append(duration());
        stringBuilder.append("\nDurationString : ").append(minSuggestedDuration);
        stringBuilder.append("\nImpressions : ").append(impTrackers);
        stringBuilder.append("\nResource : ").append(resources);
        return stringBuilder.toString();
    }

    public void setMinSuggestedDuration(String minSuggestedDuration) {
        this.minSuggestedDuration = minSuggestedDuration;
    }

    public ArrayList<Resource> getResources() {
        return resources;
    }

    public void setResources(ArrayList<Resource> resources) {
        this.resources = resources;
    }

    public ArrayList<Tracker> getAdTrackers() {
        return impTrackers;
    }

    public ArrayList<Tracker> getTrackers() {
        return trackers;
    }

    public void setTrackers(ArrayList<Tracker> trackers) {
        this.trackers = trackers;
    }

    public ArrayList<Tracker> getImpTrackers() {
        return impTrackers;
    }

    public void setImpTrackers(ArrayList<Tracker> impTrackers) {
        this.impTrackers = impTrackers;
    }

    public ArrayList<Tracker> getCombinedAdTrackers() {
        ArrayList combinedImpTrackers = new ArrayList();
        AdI ad = this;
        while (ad != null) {
            combinedImpTrackers.addAll(ad.getAdTrackers());
            ad = ad.parent;
        }
        return combinedImpTrackers;
    }

    @Override
    public AdType getType() {
        return AdType.NONLINEAR;
    }


    @Override
    public boolean isUrl() {
        String resouceContent = "";
        if (resources.size() > 0) {
            if (resources.get(0).type.equalsIgnoreCase("StaticResource")) {
                resouceContent = resources.get(0).getValue();
            } else if (resources.get(0).type.equalsIgnoreCase("HTMLResource")) {
                resouceContent = resources.get(0).getValue();
            }
            return URLUtil.isValidUrl(resouceContent);
        }
        return false;
    }

    public ArrayList<Tracker> getEventTrackers(String event) {

        ArrayList<Tracker> filteredTrackers = new ArrayList<Tracker>();

        ArrayList<Tracker> trackers = getTrackers();
        for (Tracker t : trackers) {
            if (t.event != null && t.event.equalsIgnoreCase(event)) {
                filteredTrackers.add(t);
            }
        }
        return filteredTrackers;
    }

    @Override
    public String getAdRL() {

        if (resources.size() > 0) {
            if (resources.get(0).type.equalsIgnoreCase("StaticResource")) {
                return resources.get(0).getValue();
            } else if (resources.get(0).type.equalsIgnoreCase("HTMLResource")) {
                return resources.get(0).getValue();
            }
        }
        return null;
    }

    @Override
    public void setAdRL(String url) {
        if (resources.size() > 0) {
            resources.get(0).setValue(url);
        }
    }

    public Resource getResource() {

        if (resources.size() > 0) {
            if (resources.get(0).type.equalsIgnoreCase("StaticResource")) {
                return resources.get(0);
            }
        }
        return null;
    }

    @Override
    public long duration() {
        if (this.minSuggestedDuration == null) {
            return super.duration();
        }
        long duration = LMUtils.convertTimeToMillis(this.minSuggestedDuration);
        if (duration <= 0) {
            return super.duration();
        }
        return duration;
    }
}
