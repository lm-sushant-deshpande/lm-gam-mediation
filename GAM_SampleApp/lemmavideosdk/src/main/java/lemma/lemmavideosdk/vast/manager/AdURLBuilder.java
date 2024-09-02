package lemma.lemmavideosdk.vast.manager;

import android.location.Location;
import android.net.Uri;
import android.util.DisplayMetrics;

import java.util.HashMap;
import java.util.Map;

import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.common.LMUtils;
import lemma.lemmavideosdk.common.LemmaSDK;


public class AdURLBuilder {

    static String TAG = "AdURLBuilder";

    public AdvertisingIdClient advertisingIdClient;
    public DisplayMetrics displayMetrics;
    public LMDeviceInfo deviceInfo;
    public LMAppInfo appInfo;
    public LMLocationManager locationManager;
    public NetworkStatusMonitor networkStatusMonitor;
    String url;

    public AdURLBuilder(String url) {
        this.url = url;
    }

    public AdURLBuilder(LMAdRequest request) {

        String url = String.format("%s/lemma/servad", LMUtils.SERVER_URL);
        if (request.getAdServerBaseURL() != null) {
            try {
                Uri uri = Uri.parse(request.getAdServerBaseURL());
                url = uri.toString();
            } catch (Exception e) {
                LMLog.e(TAG, e.getMessage());
            }
        }
        Uri.Builder builder = Uri.parse(url).buildUpon();
        builder.appendQueryParameter("pid", request.getPublisherId());
        builder.appendQueryParameter("aid", request.getAdUnitId());
        builder.appendQueryParameter("at", "3");
        if (request.map != null) {
            for (Map.Entry<String, String> entry : request.map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key != null && value != null) {
                    builder.appendQueryParameter(key, value);
                }
            }
        }
        this.url = builder.build().toString();
    }

    public String build(Map<String, String> map) {

        Uri.Builder builder = Uri.parse(url).buildUpon();
        Uri uri = Uri.parse(url);

        for (Map.Entry<String, String> entry : defaultMap().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String existingQueryValue = uri.getQueryParameter(key);

            // Do not override values from original URL
            if (!isValid(existingQueryValue)) {
                builder.appendQueryParameter(key, value);
            }
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.appendQueryParameter(key, value);
        }
        String updatedURL = builder.build().toString();
        return updatedURL;
    }


    private Map<String, String> defaultMap() {
        Map<String, String> map = new HashMap<String, String>();
        if (advertisingIdClient != null) {
            AdvertisingIdClient.AdInfo adInfo = advertisingIdClient.refreshAdvertisingInfo();
            if (adInfo != null && adInfo.getId() != null && adInfo.getId().length() > 0) {
                map.put("ifa", adInfo.getId());
            }
        }
        if (displayMetrics != null && (displayMetrics.widthPixels > 0 && displayMetrics.heightPixels > 0)) {
            map.put("vw", String.valueOf(displayMetrics.widthPixels));
            map.put("vh", String.valueOf(displayMetrics.heightPixels));
            map.put("bw", String.valueOf(displayMetrics.widthPixels));
            map.put("bh", String.valueOf(displayMetrics.heightPixels));
        }

        // Device parameters
        String ua = deviceInfo.getUserAgent();
        if (isValid(ua)) {
            map.put("ua", ua);
        }

        String deviceMake = deviceInfo.getMake();
        if (isValid(deviceMake)) {
            map.put("dmake", deviceMake);
        }

        String deviceModel = deviceInfo.getModel();
        if (isValid(deviceModel)) {
            map.put("dmodel", deviceModel);
        }

        String os = deviceInfo.getOsName();
        if (isValid(os)) {
            map.put("os", os);
        }

        String osv = deviceInfo.getOsVersion();
        if (isValid(osv)) {
            map.put("osv", osv);
        }

        map.put("js", "1");

        Location location = locationManager.getLocation();
        if (location != null) {

            String lat = String.valueOf(location.getLatitude());
            if (isValid(lat)) {
                map.put("dlat", lat);
            }

            String lon = String.valueOf(location.getLatitude());
            if (isValid(lon)) {
                map.put("dlon", lon);
            }
        }


        // Network info
        NetworkStatusMonitor.NETWORK_TYPE networkType = networkStatusMonitor.getCurrentNetworkType();
        if (networkType == NetworkStatusMonitor.NETWORK_TYPE.WIFI) {
            map.put("conntype", "2");
        } else if (networkType == NetworkStatusMonitor.NETWORK_TYPE.CELLULAR) {
            map.put("conntype", "3");
        }

        String carrier = deviceInfo.getCarrierName();
        if (isValid(carrier)) {
            map.put("carrier", carrier);
        }

        // App information
        String appId = appInfo.getPackageName();
        if (isValid(appId)) {
            map.put("apid", appId);
        }

        String appName = appInfo.getAppName();
        if (isValid(appName)) {
            map.put("apnm", appName);
        }


        String appVersion = appInfo.getAppVersion();
        if (isValid(appVersion)) {
            map.put("apver", appVersion);
        }

        String appBundle = appInfo.getPackageName();
        if (isValid(appBundle)) {
            map.put("apbndl", appBundle);
        }

        map.put("apurl", "https://play.google.com/store/apps/details?id=com.lemma.digital");
        map.put("sdkver", LemmaSDK.getVersion());
        map.put("rst", "2");

        return map;
    }

    private boolean isValid(String str) {
        return (str != null && str.length() > 0);
    }


}
