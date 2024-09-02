package lemma.lemmavideosdk.vast.VastBuilder;

import java.util.ArrayList;

import lemma.lemmavideosdk.common.LMLog;

public class AdGroup {

    public ArrayList<AdI> ads = null;
    public boolean isRTB = false;

    public AdGroup(ArrayList<AdI> ads) {
        this.ads = ads;
    }


    public static ArrayList<AdGroup> adGroupsForAd(ArrayList<AdI> ads) {
        return adGroupsForAd(ads, false);
    }

    public static ArrayList<AdGroup> adGroupsForAd(ArrayList<AdI> ads, boolean isRTB) {

        ArrayList<AdGroup> adGroups = new ArrayList<>();
        for (AdI ad : ads) {
            ArrayList<AdI> groupAds = new ArrayList<>();
            ad.frame = new AdI.Frame(0, 100, 0, 100);
            groupAds.add(ad);
            AdGroup group = new AdGroup(groupAds);
            group.isRTB = isRTB;
            adGroups.add(group);
        }
        return adGroups;
    }

    private static AdI addForId(int id, ArrayList<AdI> adList) {
        for (AdI ad : adList) {
            if (Integer.parseInt(ad.id) == id) return ad;
        }
        return null;
    }

    private static boolean isAdInExtGroup(AdI ad, ArrayList<ArrayList<AdI.Frame>> customLayoutExt) {

        for (ArrayList<AdI.Frame> group : customLayoutExt) {
            for (AdI.Frame frm : group) {
                if (Integer.parseInt(ad.id) == frm.id) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ArrayList<AdGroup> adGroups(Vast vast,
                                              ArrayList<ArrayList<AdI.Frame>> customLayoutExt) {

        ArrayList<AdI> adList = vast.ads;
        ArrayList<AdGroup> adGroups = new ArrayList<>();

        if (customLayoutExt == null ||
                (customLayoutExt != null &&
                        customLayoutExt.size() == 0)) {

            if (vast.extPodSize != null) {
                Integer extra = vast.extPodSize;
                if (extra > 0) {
                    for (int i = 0; i < extra; i++) {
                        adGroups.add(new RtBMarkerAdGroup(null));
                    }
                }
            }

            Integer index = 1;
            for (AdI ad : adList) {
                ArrayList<AdI> ads = new ArrayList<>(10);
                ad.frame = new AdI.Frame(0, 100, 0, 100);
                ads.add(ad);

                // Generate sequence if not available
                if (ad.sequence == null) {
                    ad.sequence = index;
                    index++;
                }

                if (ad.sequence <= adGroups.size()) {
                    Integer grpIndex = ad.sequence - 1;
                    if (grpIndex < 0) {
                        grpIndex = 0;
                    }
                    adGroups.set(grpIndex, new AdGroup(ads));
                } else {
                    adGroups.add(new AdGroup(ads));
                }
            }

        } else {

            for (AdI ad : adList) {

                if (!isAdInExtGroup(ad, customLayoutExt)) {
                    ArrayList<AdI> ads = new ArrayList<>();
                    ad.frame = new AdI.Frame(0, 100, 0, 100);
                    ads.add(ad);
                    adGroups.add(new AdGroup(ads));
                }
            }

            for (ArrayList<AdI.Frame> group : customLayoutExt) {

                ArrayList<AdI> ads = new ArrayList<>();
                for (AdI.Frame frm : group) {

                    AdI ad = addForId(frm.id, adList);
                    if (ad != null) {
                        ad.frame = frm;
                        ads.add(ad);
                    } else {
                        LMLog.e("AdGroup", "addForId should provide Ad");
                    }
                }
                adGroups.add(new AdGroup(ads));
            }

            if (vast.extPodSize != null) {
                Integer extra = vast.extPodSize - adList.size();
                if (extra > 0) {
                    for (int i = 0; i < extra; i++) {
                        adGroups.add(new RtBMarkerAdGroup(null));
                    }
                }
            }

        }

        return adGroups;
    }
}