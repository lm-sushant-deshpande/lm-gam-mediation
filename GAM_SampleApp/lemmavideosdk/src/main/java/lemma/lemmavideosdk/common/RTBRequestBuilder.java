package lemma.lemmavideosdk.common;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lemma.lemmavideosdk.vast.manager.AdvertisingIdClient;
import lemma.lemmavideosdk.vast.manager.LMAdRequest;
import lemma.lemmavideosdk.vast.manager.LMAppInfo;
import lemma.lemmavideosdk.vast.manager.LMDeviceInfo;
import lemma.lemmavideosdk.vast.manager.LMLocationManager;
import lemma.lemmavideosdk.vast.manager.NetworkStatusMonitor;

public class RTBRequestBuilder {

    private static final String TAG = "RTBRequestBuilder";
    private final LMAdRequest request;
    public AdvertisingIdClient advertisingIdClient;
    public DisplayMetrics displayMetrics;
    public LMDeviceInfo deviceInfo;
    public LMAppInfo appInfo;
    public LMLocationManager locationManager;
    public NetworkStatusMonitor networkStatusMonitor;
    public boolean isVideo = false;
    private final Context context;

    public boolean shouldAddDisplayManager = false;

    public RTBRequestBuilder(LMAdRequest request, @NonNull Context aContext) {
        this.context = aContext.getApplicationContext();
        this.request = request;
    }

    public JSONObject build() {

        // Prepare Header map
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(LMUtils.CONTENT_TYPE, LMUtils.RESPONSE_HEADER_CONTENT_TYPE_JSON);
        headerMap.put(LMUtils.ORTB_VERSION_PARAM, LMUtils.ORTB_VERSION);

        if (deviceInfo != null) {
            // Request to update the Advertising info details.
            deviceInfo.updateAdvertisingIdInfo();
        }

        String serverUrl;
        if (request.getAdServerBaseURL() != null){
            try {
                Uri uri = Uri.parse(request.getAdServerBaseURL());
                serverUrl = uri.toString();
            }catch (Exception e){
                AppLog.e(TAG,e.getMessage());
            }
        }

        return getBody(context);
    }


    private JSONObject getBody(Context context) {

        JSONObject parentJsonObject = new JSONObject();

        try {
            parentJsonObject.put(LMUtils.KEY_ID, UUID.randomUUID().toString());
            parentJsonObject.put(LMUtils.KEY_AT, 1);

            parentJsonObject.put(LMUtils.KEY_CURRENCY, getCurrencyJson());
            parentJsonObject.put(LMUtils.KEY_IMPRESSION, getImpressionJsonArray());
            parentJsonObject.put(LMUtils.KEY_APP, getAppJson());
            parentJsonObject.put(LMUtils.KEY_DEVICE, getDeviceObject(context));

            parentJsonObject.put(LMUtils.KEY_USER, getUserJson());

            JSONObject regsObject = getRegsJson();
            if (regsObject != null && regsObject.length() > 0) {
                parentJsonObject.put(LMUtils.KEY_REGS, regsObject);
            }

            parentJsonObject.put(LMUtils.KEY_EXTENSION, getExtJson());
        } catch (JSONException e) {
            AppLog.e(TAG, "Exception occurred in getBody() : " + e.getMessage());
        }

        return parentJsonObject;
    }

    //Private methods
    private JSONArray getCurrencyJson() {
        JSONArray currencyJsonArray = new JSONArray();

        currencyJsonArray.put("USD");

        return currencyJsonArray;
    }

    private JSONArray getImpressionJsonArray() {
        JSONArray impressionJsonArray = new JSONArray();

        JSONObject impJsonObject = new JSONObject();

        if (isVideo){
            JSONObject videoJsonObject = new JSONObject();
            updateJsonObjectWithKeyValue(videoJsonObject,LMUtils.KEY_WIDTH, request.getWidth());
            updateJsonObjectWithKeyValue(videoJsonObject,LMUtils.KEY_HEIGHT, request.getHeight());

            updateJsonObjectWithKeyValue(videoJsonObject,LMUtils.KEY_INTERSTITIAL, request.isInterstitial?1:0);
            updateJsonObjectWithKeyValue(videoJsonObject,LMUtils.KEY_VIDEO_MIN_DURATION, 15);
            updateJsonObjectWithKeyValue(videoJsonObject,LMUtils.KEY_VIDEO_MAX_DURATION, 120);
            updateJsonObjectWithKeyValue(videoJsonObject,LMUtils.KEY_LINEARITY, 1);
            updateJsonObjectWithKeyValue(impJsonObject,LMUtils.KEY_TAGID,request.getAdUnitId());

            JSONArray protocolsArray = new JSONArray();
            protocolsArray.put(2);
            protocolsArray.put(3);

            updateJsonObjectWithKeyValue(videoJsonObject,LMUtils.KEY_VIDEO_PROTOCOLS, protocolsArray);

            JSONArray mimesArray = new JSONArray();
            mimesArray.put("video/mp4");
            mimesArray.put("video/3gpp");
            updateJsonObjectWithKeyValue(videoJsonObject,LMUtils.KEY_VIDEO_MIMES, mimesArray);

            updateJsonObjectWithKeyValue(impJsonObject,LMUtils.KEY_VIDEO,videoJsonObject);
            updateJsonObjectWithKeyValue(impJsonObject,LMUtils.KEY_ID,"1");

        }else{
            JSONObject bannerJsonObject = new JSONObject();
            updateJsonObjectWithKeyValue(bannerJsonObject,LMUtils.KEY_WIDTH, request.getWidth());
            updateJsonObjectWithKeyValue(bannerJsonObject,LMUtils.KEY_HEIGHT, request.getHeight());

            updateJsonObjectWithKeyValue(bannerJsonObject,LMUtils.KEY_INTERSTITIAL, request.isInterstitial?1:0);

            updateJsonObjectWithKeyValue(impJsonObject,LMUtils.KEY_BANNER,bannerJsonObject);
            updateJsonObjectWithKeyValue(impJsonObject,LMUtils.KEY_ID,"1");
            updateJsonObjectWithKeyValue(impJsonObject,LMUtils.KEY_TAGID,request.getAdUnitId());
        }

        if (shouldAddDisplayManager){
            updateJsonObjectWithKeyValue(impJsonObject, LMUtils.KEY_DISPLAY_MANAGER_VERSION,LemmaSDK.getVersion());
            updateJsonObjectWithKeyValue(impJsonObject, LMUtils.KEY_DISPLAY_MANAGER,LMUtils.VALUE_DISPLAY_MANAGER);
        }
        impressionJsonArray.put(impJsonObject);
        return impressionJsonArray;
    }

    private JSONObject getAppJson() {
        JSONObject appJsonObject = new JSONObject();

        try {
            updateJsonObjectWithKeyValue(appJsonObject, LMUtils.KEY_NAME, appInfo.getAppName());
            updateJsonObjectWithKeyValue(appJsonObject, LMUtils.KEY_BUNDLE, appInfo.getPackageName());
            appJsonObject.put(LMUtils.KEY_VERSION, appInfo.getAppVersion());


            JSONObject publisherJsonObject = new JSONObject();
            publisherJsonObject.put(LMUtils.KEY_ID, request.getPublisherId());

            appJsonObject.put(LMUtils.KEY_PUBLISHER, publisherJsonObject);

        } catch (JSONException jsonExcAppJson) {
            AppLog.e(TAG, "Exception occurred in getAppJson() : " + jsonExcAppJson.getMessage());
        }

        return appJsonObject;
    }

    private void updateJsonObjectWithKeyValue(JSONObject json, String key, Object param) {
        if (json != null && key!= null && param != null) {
            try {
                json.put(key, param);
            } catch (JSONException e) {
                AppLog.w(TAG, "Unable to add " + key + " and " + param);
            }
        } else {
            AppLog.w(TAG, "Unable to add " + key + " and " + param + " due to invalid values.");
        }
    }

    private JSONObject getDeviceObject(Context context) {
        JSONObject deviceJsonObject = new JSONObject();

        if (deviceInfo != null) {
            try {
                deviceJsonObject.put(LMUtils.KEY_GEO, getGeoObject());
                deviceJsonObject.put(LMUtils.KEY_PXRATIO, this.context.getResources().getDisplayMetrics().density);

                if (deviceInfo.getLmtEnabled() != null) {
                    deviceJsonObject.put(LMUtils.KEY_LMT, deviceInfo.getLmtEnabled() ? 1 : 0);
                }


                String advertisingId = deviceInfo.getAdvertisingID();
                deviceJsonObject.put(LMUtils.KEY_IFA, advertisingId);


                // Value of connection type

                NetworkStatusMonitor.NETWORK_TYPE networkType = networkStatusMonitor.getCurrentNetworkType();
                if (networkType == NetworkStatusMonitor.NETWORK_TYPE.WIFI) {
                    deviceJsonObject.put(LMUtils.KEY_CONNECTION_TYPE, 2);

                } else if (networkType == NetworkStatusMonitor.NETWORK_TYPE.CELLULAR) {
                    deviceJsonObject.put(LMUtils.KEY_CONNECTION_TYPE, 3);
                }

                updateJsonObjectWithKeyValue(deviceJsonObject, LMUtils.KEY_CARRIER, deviceInfo.getCarrierName());

                deviceJsonObject.put(LMUtils.KEY_JS, 1);

                deviceJsonObject.put(LMUtils.KEY_USER_AGENT, deviceInfo.getUserAgent());
                deviceJsonObject.put(LMUtils.KEY_MAKE, deviceInfo.getMake());
                deviceJsonObject.put(LMUtils.KEY_MODEL, deviceInfo.getModel());
                deviceJsonObject.put(LMUtils.KEY_OS, deviceInfo.getOsName());
                deviceJsonObject.put(LMUtils.KEY_OS_VERSION, deviceInfo.getOsVersion());
                deviceJsonObject.put(LMUtils.KEY_H, displayMetrics.heightPixels);
                deviceJsonObject.put(LMUtils.KEY_W, displayMetrics.widthPixels);
                deviceJsonObject.put(LMUtils.KEY_LANGUAGE, deviceInfo.getAcceptLanguage());

                if (LMUtils.isTablet(context)) {
                    deviceJsonObject.put(LMUtils.KEY_DEVICE_TYPE, 5);
                } else {
                    deviceJsonObject.put(LMUtils.KEY_DEVICE_TYPE, 4);
                }
            } catch (Exception exception) {
                AppLog.e(TAG, "Exception occurred in getDeviceObject() : " + exception.getMessage());
            }
        }
        return deviceJsonObject;
    }

    private JSONObject getGeoObject() {
        JSONObject geoJsonObject = new JSONObject();

        try {

            //Set Location Params
            Location location = locationManager.getLocation();
            if (location != null) {
                geoJsonObject.put(LMUtils.KEY_GEO_TYPE, 1);
                geoJsonObject.put(LMUtils.KEY_LATITUDE, location.getLatitude());
                geoJsonObject.put(LMUtils.KEY_LONGITUDE, location.getLongitude());
            }

        } catch (Exception exception) {
            AppLog.e(TAG, "Exception occurred in getGeoObject() : " + exception.getMessage());
        }
        return geoJsonObject;
    }

    private JSONObject getExtJson() {
        JSONObject extJsonObject = new JSONObject();
        return extJsonObject;
    }

    private JSONObject getUserJson() {
        JSONObject userJsonObject = new JSONObject();
        return userJsonObject;
    }

    private JSONObject getRegsJson() {
        try {
            JSONObject regsJsonObject = new JSONObject();
            regsJsonObject.put(LMUtils.KEY_COPPA, request.isCoppaEnabled()? 1 : 0);
            return regsJsonObject;

        } catch (JSONException e) {
            AppLog.e(TAG, "Exception occurred in getRegsJson() : " + e.getMessage());
        }
        return null;
    }
}
