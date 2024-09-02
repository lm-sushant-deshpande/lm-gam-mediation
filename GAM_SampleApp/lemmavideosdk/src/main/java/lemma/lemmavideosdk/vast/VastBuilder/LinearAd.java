package lemma.lemmavideosdk.vast.VastBuilder;

import java.util.ArrayList;

import lemma.lemmavideosdk.common.LMUtils;

/**
 * Created by lemma on 14/04/18.
 */

public class LinearAd extends AdI {

    String type = "LinearAd";
    ArrayList<MediaFile> mediaFiles;
    String durationString;
    ArrayList<Tracker> trackers = new ArrayList<>();
    ArrayList<Tracker> impTrackers = new ArrayList<>();

    public LinearAd() {
        setMediaFiles(new ArrayList<MediaFile>());
        setImpTrackers(new ArrayList<Tracker>());
    }

    public LinearAd(MediaFile mf) {
        ArrayList<MediaFile> mfiles = new ArrayList<>();
        mfiles.add(mf);
        setMediaFiles(mfiles);
    }

    public String mimeType() {

        if (mediaFiles.size() > 0) {
            return mediaFiles.get(0).getType();
        }
        return super.mimeType();
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString());
        stringBuilder.append("\nType : ").append(type);
        stringBuilder.append("\nDuration : ").append(duration());
        stringBuilder.append("\nDurationString : ").append(durationString);
        stringBuilder.append("\nImpressions : ").append(impTrackers);
        stringBuilder.append("\nMedia Files : ").append(mediaFiles);
        return stringBuilder.toString();
    }

    public ArrayList<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(ArrayList<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    public void setDurationString(String duration) {
        this.durationString = duration;
    }

    @Override
    public long duration() {

        if (this.durationString == null) {
            return super.duration();
        }
        long duration = LMUtils.convertTimeToMillis(this.durationString);
        if (duration <= 0) {
            return super.duration();
        }
        return duration;
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

    public ArrayList<Tracker> getAdTrackers() {
        return impTrackers;
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
        return AdType.LINEAR;
    }

    @Override
    public String getAdRL() {
        if (mediaFiles.size() > 0) {
            return mediaFiles.get(0).getUrl();
        }
        return null;
    }

    @Override
    public void setAdRL(String url) {
        if (mediaFiles.size() > 0) {
            mediaFiles.get(0).setUrl(url);
        }
    }

    public void filter() {
        if (mediaFiles.size() > 1) {
            setMediaFiles(LMUtils.filteredListForBestMatchingMedia(mediaFiles));
        }
    }
}
