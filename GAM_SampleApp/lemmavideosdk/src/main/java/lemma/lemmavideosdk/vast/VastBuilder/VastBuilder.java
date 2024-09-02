package lemma.lemmavideosdk.vast.VastBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.common.LMUtils;


public class VastBuilder {

    private Document vastsDocument;
    private static String TAG = "VastBuilder";
    boolean processWrapper = false;

    public Vast build(String xml) throws Exception {

        vastsDocument = XmlTools.stringToDocument(xml);
        Vast vast = new Vast();
        vast.ads = getAds();
        vast.extPodSize = getExtPodSize();
        vast.altSequence = getAltSequence();
        vast.customLayoutExt = layoutExtention();
        return vast;
    }

    public Vast buildWithJson(String json) {

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("lmCustResp");

            Vast vast = new Vast();
            vast.ads = getAds(jsonArray);
            vast.extPodSize = jsonObject.optInt("podSize");
            JSONObject extObj = jsonObject.optJSONObject("ext");
            if (extObj != null) {
                vast.altSequence = extObj.optString("altseq");
                JSONArray layoutJsonArray = extObj.optJSONArray("customLayout");
                if (layoutJsonArray != null) {
                    ArrayList layouts = new ArrayList();
                    try {

                        for (int i = 0; i < layoutJsonArray.length(); i++) {
                            JSONArray jArray = layoutJsonArray.getJSONArray(i);
                            ArrayList frames = new ArrayList();

                            for (int j = 0; j < jArray.length(); j++) {
                                JSONObject jObj = jArray.getJSONObject(j);
                                int sx = jObj.getInt("XStart");
                                int ex = jObj.getInt("XEnd");
                                int sy = jObj.getInt("YStart");
                                int ey = jObj.getInt("YEnd");
                                int id = jObj.getInt("Id");

                                AdI.Frame frame = new AdI.Frame(sx, ex, sy, ey);
                                frame.id = id;
                                frames.add(frame);
                            }

                            layouts.add(frames);
                        }

                    } catch (JSONException e) {
                        LMLog.e(TAG, e.getLocalizedMessage());
                    }
                    vast.customLayoutExt = layouts;
                }
            }
            return vast;

        } catch (Exception e) {
            LMLog.e(TAG, e.getLocalizedMessage());
        }
        return null;
    }

    private Boolean isLinear(JSONObject mediaObj) throws JSONException {
        String type = mediaObj.getString("Type");
        if (type != null) {
            if (type.equalsIgnoreCase("script")) {
                return false;
            } else {
                return true;
            }
        }
        return null;
    }

    private AdI getAd(JSONObject jsonObject) throws JSONException {

        JSONArray mediaArray = jsonObject.getJSONArray("media");
        if (mediaArray.length() > 0) {
            JSONObject mediaObj = mediaArray.getJSONObject(0);
            Boolean isLinear = isLinear(mediaObj);
            if (isLinear == true) {

                LinearAd ad = new LinearAd();
                Integer id = jsonObject.getInt("id");
                if (id != null) {
                    ad.id = String.valueOf(id);
                } else {
                    ad.id = "";
                }

                ArrayList<MediaFile> mediaFiles = new ArrayList();
                for (int i = 0; i < mediaArray.length(); i++) {

                    mediaObj = mediaArray.getJSONObject(i);

                    if (i == 0) {
                        Integer duration = mediaObj.getInt("Duration");
                        if (duration != null) {
                            ad.setDurationString(String.valueOf(duration));
                        }
                    }

                    MediaFile mf = new MediaFile();
                    String url = mediaObj.getString("Creative");
                    mf.setUrl(url);

                    Integer width = mediaObj.getInt("Width");
                    if (width != null) {

                        String widthString = String.valueOf(width);
                        mf.setWidth(widthString);
                    }


                    Integer height = mediaObj.getInt("Height");
                    if (width != null) {

                        String heightString = String.valueOf(height);
                        mf.setHeight(heightString);
                    }

                    String type = mediaObj.getString("Type");
                    mf.setType(type);

                    mediaFiles.add(mf);

                }

                ad.setMediaFiles(mediaFiles);

                ArrayList<Tracker> impressions = new ArrayList();
                if (impressions != null) {
                    JSONArray impressionArray = jsonObject.optJSONArray("trackers");

                    for (int i = 0; i < impressionArray.length(); i++) {
                        String imressionURL = impressionArray.getString(i);
                        Tracker t = new Tracker();
                        t.setUrl(imressionURL);
                        impressions.add(t);
                    }
                    ad.setImpTrackers(impressions);
                }

                ArrayList<Tracker> eventTrackers = new ArrayList();
                JSONArray eventTrackerArray = jsonObject.optJSONArray("events");

                if (eventTrackerArray != null) {

                    for (int i = 0; i < eventTrackerArray.length(); i++) {
                        JSONObject trackerObj = eventTrackerArray.getJSONObject(i);
                        Tracker tracker = new Tracker();
                        tracker.setUrl(trackerObj.getString("EventUrl"));

                        String eventName = trackerObj.getString("EventName");
                        tracker.setEvent(eventName);
                        eventTrackers.add(tracker);
                    }
                    ad.setTrackers(eventTrackers);
                }

                return ad;

            } else if (isLinear == false) {

                NonLinearAd ad = new NonLinearAd();
                Integer id = jsonObject.getInt("id");
                if (id != null) {
                    ad.id = String.valueOf(id);
                } else {
                    ad.id = "";
                }

                ArrayList<Resource> resources = new ArrayList<>();

                for (int i = 0; i < mediaArray.length(); i++) {

                    mediaObj = mediaArray.getJSONObject(i);
                    Resource r = new Resource();

                    if (i == 0) {
                        Integer duration = mediaObj.getInt("Duration");
                        if (duration != null) {
                            ad.setMinSuggestedDuration(String.valueOf(duration));
                        }

                    }
                    r.setValue(mediaObj.getString("Creative"));

                    Integer width = mediaObj.getInt("Width");
                    if (width != null) {

                        String widthString = String.valueOf(width);
                        r.setWidth(widthString);
                    }


                    Integer height = mediaObj.getInt("Height");
                    if (width != null) {

                        String heightString = String.valueOf(height);
                        r.setHeight(heightString);
                    }

                    r.setType("HTMLResource");
                    r.setCreativeType("text/html");
                    resources.add(r);
                }

                ad.setResources(resources);

                ArrayList<Tracker> impressions = new ArrayList();
                JSONArray impressionArray = jsonObject.optJSONArray("trackers");
                if (impressionArray != null) {

                    for (int i = 0; i < impressionArray.length(); i++) {
                        String imressionURL = impressionArray.getString(i);
                        Tracker t = new Tracker();
                        t.setUrl(imressionURL);
                        impressions.add(t);
                    }
                }
                ad.setImpTrackers(impressions);

                ArrayList<Tracker> eventTrackers = new ArrayList();
                JSONArray eventTrackerArray = jsonObject.optJSONArray("events");
                if (eventTrackerArray != null) {
                    for (int i = 0; i < eventTrackerArray.length(); i++) {
                        JSONObject trackerObj = eventTrackerArray.getJSONObject(i);
                        Tracker tracker = new Tracker();
                        tracker.setUrl(trackerObj.getString("EventUrl"));

                        String eventName = trackerObj.getString("EventName");
                        tracker.setEvent(eventName);
                        eventTrackers.add(tracker);
                    }
                }
                ad.setTrackers(eventTrackers);
                return ad;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public ArrayList getAds(JSONArray jsonArray) {

        ArrayList<AdI> ads = new ArrayList();

        try {

            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject adObject = jsonArray.getJSONObject(i);
                //Get Linear ads
                AdI ad = getAd(adObject);
                if (ad != null) {
                    ads.add(ad);
                }
            }

        } catch (Exception e) {
            LMLog.e(TAG, e.getLocalizedMessage());
        }
        return ads;
    }

    public Vast copyWithNewAdList(Vast oldVast, ArrayList<AdI> ads) {

        Collections.sort(ads, new Comparator<AdI>() {
            public int compare(AdI ad1, AdI ad2) {
                return ad1.sequence.compareTo(ad2.sequence);
            }
        });

        Vast vast = new Vast();
        vast.ads = ads;
        vast.extPodSize = oldVast.extPodSize;
        vast.altSequence = oldVast.altSequence;
        vast.customLayoutExt = oldVast.customLayoutExt;
        return vast;
    }

    private Integer getExtPodSize() {

        final String extPodSizeXpath = "/VAST/Extensions/Extension[@type='podSize']";
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            Node node = (Node) xpath.evaluate(extPodSizeXpath, vastsDocument, XPathConstants.NODE);
            String value = getNodeVaue(node);
            if (value != null && value.length() > 0) {
                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            LMLog.d(TAG, e.getMessage());
        }
        return null;
    }

    private String getAltSequence() {

        final String extPodSizeXpath = "/VAST/Extensions/Extension[@type='altSeq']";
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            Node node = (Node) xpath.evaluate(extPodSizeXpath, vastsDocument, XPathConstants.NODE);
            String value = getNodeVaue(node);
            if (value != null && value.length() > 0) {
                return value;
            }
        } catch (Exception e) {
            LMLog.d(TAG, e.getMessage());
        }
        return null;
    }

    private ArrayList customLayoutArray(Node extNode) {

        ArrayList layouts = new ArrayList();
        String jsonString = getNodeVaue(extNode);
        try {

            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray jArray = jsonArray.getJSONArray(i);
                ArrayList frames = new ArrayList();

                for (int j = 0; j < jArray.length(); j++) {
                    JSONObject jObj = jArray.getJSONObject(j);
                    int sx = jObj.getInt("XStart");
                    int ex = jObj.getInt("XEnd");
                    int sy = jObj.getInt("YStart");
                    int ey = jObj.getInt("YEnd");
                    int id = jObj.getInt("Id");

                    AdI.Frame frame = new AdI.Frame(sx, ex, sy, ey);
                    frame.id = id;
                    frames.add(frame);
                }

                layouts.add(frames);
            }

        } catch (JSONException e) {
            LMLog.e(TAG, e.getLocalizedMessage());
        }
        return layouts;
    }

    public ArrayList layoutExtention() {

        final String extCustomLayoutXpath = "/VAST/Extensions/Extension[@type='customLayout']";
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node extNode = null;
        try {
            extNode = (Node) xpath.evaluate(extCustomLayoutXpath, vastsDocument, XPathConstants.NODE);
        } catch (Exception e) {
            LMLog.d(TAG, e.getMessage());
        }

        if (extNode != null) {
            return customLayoutArray(extNode);
        }
        return null;
    }

    private String getNodeVaue(Node node) {
        if (node == null) return "";
        return XmlTools.getElementValue(node);
    }

    private ArrayList<Tracker> impTrackers(Node inlineNode) throws XPathExpressionException {

        ArrayList<Tracker> impressions = new ArrayList();

        String impressionPath = "Impression";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList impNodes = (NodeList) xpath.evaluate(impressionPath, inlineNode, XPathConstants.NODESET);
        if (impNodes != null) {

            for (int i = 0; i < impNodes.getLength(); i++) {
                Node impNode = impNodes.item(i);
                Tracker t = new Tracker();
                t.setUrl(XmlTools.getElementValue(impNode));
                impressions.add(t);
            }
        }
        return impressions;
    }

    private ArrayList<LinearAd> getLinearAds(Node inlineNode) throws XPathExpressionException {

        ArrayList<LinearAd> linearAds = new ArrayList();

        String linearAdPath = "Creatives/Creative/Linear";
        final String durationValuePath = "Duration/text()";

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList linearAdNodes = (NodeList) xpath.evaluate(linearAdPath, inlineNode, XPathConstants.NODESET);

        Node adNode = inlineNode.getParentNode();

        if (linearAdNodes != null) {

            for (int i = 0; i < linearAdNodes.getLength(); i++) {

                LinearAd ad = new LinearAd();
                ad.id = nodeAttrValue(adNode, "id");
                String sequence = nodeAttrValue(adNode, "sequence");
                if (sequence != null && sequence.length() > 0) {
                    ad.sequence = Integer.parseInt(sequence);
                }

                Node lAdnode = linearAdNodes.item(i);
                String duration = (String) xpath.evaluate(durationValuePath, lAdnode, XPathConstants.STRING);
                ad.setDurationString(duration);

                ArrayList<MediaFile> mediaFiles = mediaFiles(lAdnode);
                ad.setMediaFiles(mediaFiles);

                ArrayList<Tracker> trackers = eventTrackers(lAdnode);
                ad.setTrackers(trackers);
                linearAds.add(ad);
            }
        }
        return linearAds;
    }

    private ArrayList<NonLinearAd> getNonLinearAds(Node inlineNode) throws XPathExpressionException {

        ArrayList<NonLinearAd> nonlinearAds = new ArrayList();

        String nonlinearAdPath = "Creatives/Creative/NonLinearAds";

        XPath xpath = XPathFactory.newInstance().newXPath();

        NodeList nonLinearNodes = (NodeList) xpath.evaluate(nonlinearAdPath, inlineNode, XPathConstants.NODESET);

        Node adNode = inlineNode.getParentNode();

        if (nonLinearNodes != null) {
            for (int i = 0; i < nonLinearNodes.getLength(); i++) {

                NonLinearAd ad = new NonLinearAd();
                ad.id = nodeAttrValue(adNode, "id");
                String sequence = nodeAttrValue(adNode, "sequence");
                if (sequence != null && sequence.length() > 0) {
                    ad.sequence = Integer.parseInt(sequence);
                }

                Node node = nonLinearNodes.item(i);
                ArrayList<Tracker> trackers = eventTrackers(node);
                ad.setTrackers(trackers);
                nonlinearAds.add(ad);
                ad.setResources(resources(node, ad));
            }
        }
        return nonlinearAds;
    }

    public ArrayList getAds() {

        if (processWrapper) {
            ArrayList ads = new ArrayList();
            final String wrapperAdPath = "/VAST/Ad/Wrapper";
            XPath xpath = XPathFactory.newInstance().newXPath();

            try {
                NodeList wrapperNodes = (NodeList) xpath.evaluate(wrapperAdPath, vastsDocument, XPathConstants.NODESET);

                if (wrapperNodes != null && wrapperNodes.getLength() > 0) {
                    Node wrapperNode = wrapperNodes.item(0);
                    AdI ad = getWrapperAd(wrapperNode);
                    if (ad != null) {
                        ads.add(ad);
                    }
                }

            } catch (XPathExpressionException e) {
                LMLog.e(TAG, e.getLocalizedMessage());
            }
            if (ads.size() > 0) {
                return ads;
            }
        }

        return getInLineAd();
    }

    private AdI getWrapperAd(Node wrapperNode) {

        final String wrapperAdPath = "VASTAdTagURI";
        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            Node VASTAdTagURINode = (Node) xpath.evaluate(wrapperAdPath, wrapperNode, XPathConstants.NODE);

            WrapperAd ad = new WrapperAd();
            ad.adTagUrl = getNodeVaue(VASTAdTagURINode);

            ArrayList<Tracker> impressions = new ArrayList();

            String impressionPath = "Impression";
            xpath = XPathFactory.newInstance().newXPath();
            NodeList impNodes = (NodeList) xpath.evaluate(impressionPath, wrapperNode, XPathConstants.NODESET);
            if (impNodes != null) {
                for (int i = 0; i < impNodes.getLength(); i++) {
                    Node impNode = impNodes.item(i);
                    Tracker t = new Tracker();
                    t.setUrl(getNodeVaue(impNode));
                    impressions.add(t);
                }
            }
            ad.setImpTrackers(impressions);
            return ad;

        } catch (XPathExpressionException e) {
            LMLog.e(TAG, e.getLocalizedMessage());
            return null;
        }
    }

    private ArrayList getInLineAd() {

        ArrayList ads = new ArrayList();
        final String inlineAdPath = "/VAST/Ad/InLine";
        XPath xpath = XPathFactory.newInstance().newXPath();

        try {

            NodeList inlineNodes = (NodeList) xpath.evaluate(inlineAdPath, vastsDocument, XPathConstants.NODESET);
            if (inlineNodes != null) {
                for (int i = 0; i < inlineNodes.getLength(); i++) {
                    Node inlineNode = inlineNodes.item(i);

                    //Set Imp trackers
                    ArrayList<Tracker> impressions = impTrackers(inlineNode);

                    //Get Linear ads
                    ArrayList<LinearAd> lAds = getLinearAds(inlineNode);
                    for (LinearAd ad : lAds) {
                        ad.setImpTrackers(impressions);
                    }

                    //Get Non-Linear ads
                    ArrayList<NonLinearAd> nlAds = getNonLinearAds(inlineNode);
                    for (NonLinearAd nad : nlAds) {
                        nad.setImpTrackers(impressions);
                    }

                    ads.addAll(lAds);
                    ads.addAll(nlAds);
                }
            }
        } catch (Exception e) {
            LMLog.e(TAG, e.getLocalizedMessage());
        }
        return ads;
    }

    private String nodeAttrValue(Node node, String attrName) {
        Node attrNode = node.getAttributes().getNamedItem(attrName);
        if (attrNode != null) {
            return attrNode.getNodeValue();
        }
        return "";
    }

    private Resource resourceWithInfo(Node staticRNode, NonLinearAd ad) {

        Resource r = new Resource();

        Node parent = staticRNode.getParentNode();
        if (parent != null) {

            String width = nodeAttrValue(parent, "width");
            r.setWidth(width);

            String height = nodeAttrValue(parent, "height");
            r.setHeight(height);

            String minSugDuration = nodeAttrValue(parent, "minSuggestedDuration");
            ad.setMinSuggestedDuration(minSugDuration);
        }
        return r;
    }

    public ArrayList<Resource> resources(Node nonlinearNode, NonLinearAd ad) {


        ArrayList<Resource> resources = new ArrayList<>();

        XPath xpath = XPathFactory.newInstance().newXPath();
        final String staticResourcePath = "NonLinear/StaticResource";
        final String iframeResourcePath = "NonLinear/IFrameResource";
        final String htmlResourcePath = "NonLinear/HTMLResource";

        try {

            NodeList staticResourceNodes = (NodeList) xpath.evaluate(staticResourcePath, nonlinearNode, XPathConstants.NODESET);
            if (staticResourceNodes != null) {

                for (int j = 0; j < staticResourceNodes.getLength(); j++) {

                    Node staticRNode = staticResourceNodes.item(j);
                    Resource r = resourceWithInfo(staticRNode, ad);
                    r.setType("StaticResource");

                    Node crtvTypeNode = staticRNode.getAttributes().getNamedItem("creativeType");
                    String crtvType = null;

                    if (crtvTypeNode == null) {

                        // try to infer mime type if not explicitly mentioned
                        String resourceURLString = getNodeVaue(staticRNode);
                        if (LMUtils.isImageType(resourceURLString)) {
                            crtvType = "image/png";
                        }
                    } else {
                        crtvType = crtvTypeNode.getNodeValue();
                    }

                    if (crtvType != null) {
                        r.setCreativeType(crtvType);
                        r.setValue(getNodeVaue(staticRNode));
                        resources.add(r);
                    }
                }
            }

            NodeList iframeResourceNodes = (NodeList) xpath.evaluate(iframeResourcePath, nonlinearNode, XPathConstants.NODESET);
            if (iframeResourceNodes != null) {

                for (int j = 0; j < iframeResourceNodes.getLength(); j++) {

                    Node staticRNode = iframeResourceNodes.item(j);
                    Resource r = resourceWithInfo(staticRNode, ad);
                    r.setType("IFRameResource");

                    r.setValue(getNodeVaue(staticRNode));
                    resources.add(r);
                }
            }

            NodeList htmleResourceNodes = (NodeList) xpath.evaluate(htmlResourcePath, nonlinearNode, XPathConstants.NODESET);

            if (htmleResourceNodes != null) {

                for (int j = 0; j < htmleResourceNodes.getLength(); j++) {

                    Node staticRNode = htmleResourceNodes.item(j);
                    Resource r = resourceWithInfo(staticRNode, ad);
                    r.setType("HTMLResource");
                    r.setValue(getNodeVaue(staticRNode));
                    resources.add(r);
                }
            }

        } catch (Exception e) {
            LMLog.e(TAG, e.getLocalizedMessage());

        }
        return resources;
    }

    public ArrayList<MediaFile> mediaFiles(Node linearNode) {

        ArrayList<MediaFile> mediaFiles = new ArrayList();
        XPath xpath = XPathFactory.newInstance().newXPath();
        final String mediaFilesXPath = "MediaFiles/MediaFile";

        try {
            NodeList mediaFileNodes = (NodeList) xpath.evaluate(mediaFilesXPath, linearNode, XPathConstants.NODESET);
            if (mediaFileNodes != null) {
                for (int i = 0; i < mediaFileNodes.getLength(); i++) {

                    Node node = mediaFileNodes.item(i);
                    MediaFile mf = new MediaFile();
                    String url = getNodeVaue(node);
                    mf.setUrl(url);


                    Node widthAttr = node.getAttributes().getNamedItem("width");
                    if (widthAttr != null) {
                        String width = widthAttr.getNodeValue();
                        mf.setWidth(width);
                    } else {
                        mf.setWidth("480");
                    }

                    Node heightAttr = node.getAttributes().getNamedItem("height");
                    if (heightAttr != null) {
                        String height = heightAttr.getNodeValue();
                        mf.setHeight(height);
                    } else {
                        mf.setHeight("320");
                    }

                    String type = node.getAttributes().getNamedItem("type").getNodeValue();
                    mf.setType(type);

                    mediaFiles.add(mf);
                }
            }
        } catch (Exception e) {
            LMLog.e(TAG, e.getLocalizedMessage());
        }
        return mediaFiles;
    }

    public ArrayList<Tracker> eventTrackers(Node linearNode) {

        ArrayList<Tracker> trackers = new ArrayList();
        XPath xpath = XPathFactory.newInstance().newXPath();
        final String trackersXPath = "TrackingEvents/Tracking";
        final String eventAttrValueXPath = "TrackingEvents/Tracking/@event";

        try {
            NodeList trackerNodes = (NodeList) xpath.evaluate(trackersXPath, linearNode, XPathConstants.NODESET);
            if (trackerNodes != null) {
                for (int i = 0; i < trackerNodes.getLength(); i++) {
                    Node node = trackerNodes.item(i);
                    Tracker tracker = new Tracker();
                    String url = getNodeVaue(node);
                    tracker.setUrl(url);

                    String eventName = node.getAttributes().getNamedItem("event").getNodeValue();
                    tracker.setEvent(eventName);

                    trackers.add(tracker);
                }
            }
        } catch (Exception e) {
            LMLog.e(TAG, e.getLocalizedMessage());
        }
        return trackers;
    }


}
