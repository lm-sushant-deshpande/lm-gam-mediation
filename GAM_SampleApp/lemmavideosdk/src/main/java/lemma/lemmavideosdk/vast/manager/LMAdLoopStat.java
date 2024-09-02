package lemma.lemmavideosdk.vast.manager;

import android.net.Uri;

import lemma.lemmavideosdk.vast.VastBuilder.AdGroup;
import lemma.lemmavideosdk.vast.VastBuilder.AdI;

public class LMAdLoopStat {

    int currentAdIndex = -1;
    int remainingAdLoopSize = -1;
    int currentAdLoopLength = -1;
    AdGroup adGroup;

    public boolean isLoopEmpty() {
        return (remainingAdLoopSize == 0);
    }

    public int getCurrentAdIndex() {
        return currentAdIndex;
    }

    public int getCurrentAdLoopLength() {
        return currentAdLoopLength;
    }

    public String getCurrentAdCreativeName() {
        String crName = "Unknown";

        if (getCurrentAd() != null && getCurrentAd().getAdRL()!= null) {
            Uri uri = Uri.parse(getCurrentAd().getAdRL());
            crName = uri.getLastPathSegment();
            if (crName != null) {

                String[] splittedCr = crName.split("_", 3);
                if (splittedCr.length >= 3) {
                    crName = splittedCr[2];
                }
            }

        }
        return crName;
    }

    public float getCurrentAdDuration() {
        if (getCurrentAd() != null) {
            return getCurrentAd().duration() / 1000;
        }
        return -1;
    }

    private AdI getCurrentAd(){
        if (adGroup != null && adGroup.ads != null && adGroup.ads.size() > 0) {
            return adGroup.ads.get(0);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\ncurrentAdIndex :" + currentAdIndex + "\n");
        sb.append("currentAdLoopLength :" + currentAdLoopLength + "\n");
        sb.append("remainingAdLoopSize :" + remainingAdLoopSize + "\n");
        if (getCurrentAd() != null) {
            sb.append("Ad duration :" + getCurrentAdDuration() + " Seconds \n");
            sb.append("Ad Type :" + getCurrentAd().getType() + "\n");
            sb.append("Ad creative name  :" + getCurrentAdCreativeName() + "\n");
        }
        return sb.toString();
    }
}
