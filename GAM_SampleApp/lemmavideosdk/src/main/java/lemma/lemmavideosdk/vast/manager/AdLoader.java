package lemma.lemmavideosdk.vast.manager;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lemma.lemmavideosdk.common.AppLog;
import lemma.lemmavideosdk.common.BidDetail;
import lemma.lemmavideosdk.common.DisplayableAdI;
import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.common.LMUtils;
import lemma.lemmavideosdk.vast.VastBuilder.AdI;
import lemma.lemmavideosdk.vast.VastBuilder.Vast;
import lemma.lemmavideosdk.vast.VastBuilder.VastBuilder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AdLoader {

    private static final String KEY_SEAT_BID = "seatbid";
    private static final String KEY_BID = "bid";

    private static String TAG = "AdLoader";
    public NetworkStatusMonitor monitor;
    public DownloadManager downloadManager = null;
    private AdLoaderListener listener;
    private OkHttpClient httpClient;
    private Call httpCall;

    LMDeviceInfo deviceInfo;

    public void setExecuteImpressionInWebContainer(Boolean executeImpressionInWebContainer) {
        this.executeImpressionInWebContainer = executeImpressionInWebContainer;
    }

    private Boolean executeImpressionInWebContainer = false;

    private int rtbRetryCount = 2;


    public boolean isRetryWithRTB() {
        return retryWithRTB;
    }

    public void setRetryWithRTB(boolean retryWithRTB) {
        this.retryWithRTB = retryWithRTB;
    }

    private boolean retryWithRTB = false;


    private static OkHttpClient getOkHttpClient() {
        return new OkHttpClient();
    }

    public AdLoader(AdLoaderListener listener) {
        this.listener = listener;
        httpClient = getOkHttpClient();
    }

    public void destroy() {
        monitor = null;
        downloadManager.destroy();
        downloadManager = null;
        httpClient = null;
        httpCall = null;
        listener = null;
    }

    private void handleSuccess(final Vast vast, final boolean isRTB) {

        // Selects best matching media file, current vast object is updated internally
        LMUtils.filterUnsupportedAds(vast);
        if (vast.isEmpty()) {
            handleError(new Error("After applying media filter, vast became empty"));
        } else {

            // Add sequence if not
            Integer index = 1;
            for (AdI ad : vast.ads) {
                // Generate sequence if not available
                if (ad.sequence == null) {
                    ad.sequence = index;
                    index++;
                }
            }

            if (this.downloadManager != null) {
                this.downloadManager.download(vast, new DownloadManager.CompletionCallback() {
                    @Override
                    public void onDownloadComplete(ArrayList<AdI> ads) {
                        Vast newVast = new VastBuilder().copyWithNewAdList(vast, ads);
                        if (AdLoader.this.listener != null) {
                            newVast.isRTB = isRTB;
                            AdLoader.this.listener.onSuccess(newVast);
                        }
                    }
                });
            } else {
                LMLog.w(TAG, "DownloadManager is not provided in ad loader, so all ads will have remote URLs");
                if (AdLoader.this.listener != null) {
                    vast.isRTB = isRTB;
                    this.listener.onSuccess(vast);
                }
            }
        }
    }

    private void handleError(Error error) {

        if (AdLoader.this.listener != null) {
            this.listener.onError(error);
        }
    }

    public void load(final String url) {
        load(url, false);
    }



    public void load(final String url, final boolean isRTB) {
        if (monitor != null && !monitor.isNetworkConnected()) {
            handleError(new Error("Network is not available"));
            return;
        }

        Request request = null;
        if (executeImpressionInWebContainer && deviceInfo != null){
            String userAgent = deviceInfo.getUserAgent();
            request = new Request.Builder()
                    .url(url)
                    .header("User-Agent",userAgent)
                    .build();
            Log.i(TAG, "Using custom User-Agent "+userAgent);
        }else {
            request = new Request.Builder()
                    .url(url)
                    .build();
        }

        LMLog.i(TAG, "Requesting URL : " + url +" with RTB ? "+isRTB);
        httpCall = httpClient.newCall(request);
        httpCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LMLog.e(TAG, e.getLocalizedMessage());
                handleError(new Error(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String vastXML = response.body().string();
                LMLog.i(TAG, "Response : "+vastXML);

                try {
                    if (vastXML != null && vastXML.length() <= 0) {
                        if (isRetryWithRTB()) {
                            if (rtbRetryCount == 2) {
                                --rtbRetryCount;
                                LMLog.i(TAG,"Retrying");
                                load(url);
                            }else if (rtbRetryCount == 1){
                                --rtbRetryCount;
                                LMLog.i(TAG,"Retrying with RTB");
                                retryRTB(url);
                            }else {
                                handleError(new Error("Empty Vast"));
                            }
                        }else {
                            handleError(new Error("Empty Vast"));
                        }
                    } else {
                        lemma.lemmavideosdk.vast.VastBuilder.Vast vast = new VastBuilder().buildWithJson(vastXML);
                        if (vast == null) {
                            vast = new VastBuilder().build(vastXML);
                        }
                        if (vast.ads != null && vast.ads.size() > 0){
                            handleSuccess(vast, isRTB);
                        }else {
                            handleError(new Error("Empty Vast"));
                        }
                    }
                } catch (Exception e) {
                    handleError(new Error(e));
                }

            }
        });
    }

    interface ResponseCallback{
        public void onResponse(ResponseBody body);
    }

    private void loadRequest(Request request, final ResponseCallback responseCallback){
        httpCall = httpClient.newCall(request);
        httpCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AppLog.e("AdLoader", e.getLocalizedMessage());
                handleError(new Error(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseCallback.onResponse(response.body());
            }
        });
    }

    public void load(String url, JSONObject jsonObject) {


        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        AppLog.d("AdLoader","JSON Req - "+jsonObject.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        loadRequest(request, new ResponseCallback() {
            @Override
            public void onResponse(ResponseBody body) {

                try {
                    String responseString = body.string();
                    AppLog.d("AdLoader","JSON response - "+responseString);

                    if (responseString != null && responseString.length() <= 0) {
                        handleError(new Error("No ads"));
                    } else {
                        JSONObject json = null;
                        try {
                            json = new JSONObject(responseString);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        JSONObject serverData = json;

                        Map<String, BidDetail> bidMap = new HashMap<>();
                        JSONArray setBidJsonArray;
                        JSONObject bidParentJsonObject;
                        JSONArray bidJsonArray;
                        JSONObject bidJsonObject;

                        setBidJsonArray = serverData.optJSONArray(KEY_SEAT_BID);

                        if (setBidJsonArray != null && setBidJsonArray.length() > 0) {

                            for (int j = 0; j < setBidJsonArray.length(); j++) {

                                bidParentJsonObject = setBidJsonArray.optJSONObject(j);

                                if (bidParentJsonObject != null) {
                                    bidJsonArray = bidParentJsonObject.optJSONArray(KEY_BID);

                                    if (bidJsonArray != null) {
                                        for (int i = 0; i < bidJsonArray.length(); i++) {
                                            bidJsonObject = bidJsonArray.optJSONObject(i);
                                            BidDetail bid = new BidDetail(bidJsonObject);
                                            bidMap.put(bid.impressionId, bid);
                                        }
                                    }
                                }
                            }
                        }
                        Map.Entry<String,BidDetail> entry = bidMap.entrySet().iterator().next();
                        BidDetail value = entry.getValue();
                        AdLoader.this.listener.onSuccess(value);
                    }
                } catch (Exception e) {
                    handleError(new Error(e));
                }
            }
        });
    }

    private void retryRTB(String url){
        Uri.Builder builder = Uri.parse(url).buildUpon();
        builder.appendQueryParameter("rtb", "1");

        String rtbUrl = builder.build().toString();
        load(rtbUrl, true);
    }

    public interface AdLoaderListener <T extends DisplayableAdI> {
        void onSuccess(T vast);
        void onError(Error err);
    }
}
