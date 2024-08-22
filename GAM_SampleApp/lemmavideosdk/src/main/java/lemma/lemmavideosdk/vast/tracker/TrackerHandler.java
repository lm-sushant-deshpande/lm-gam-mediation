package lemma.lemmavideosdk.vast.tracker;

import android.net.Uri;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Looper;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.vast.VastBuilder.Tracker;
import lemma.lemmavideosdk.vast.manager.NetworkStatusMonitor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TrackerHandler {

    private static final String TAG = "TrackerHandler";
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    public TrackerDBHandler trackerDBHandler;
    public TimeZone timeZone;
    private NetworkStatusMonitor monitor;
    OkHttpClient client = null;
    android.text.format.DateFormat df = new android.text.format.DateFormat();
    public Boolean executeImpressionInWebContainer = false;
    private WebView trackerWebContainer = null;
    private String trackerJSHandler = "<html><head><script>function fireURL(url) {"+
            "        new Image().src = url;" +
            "console.log('Tracking impression url - '+url);" +
            "}</script></head><body></body></html>";


    public TrackerHandler(NetworkStatusMonitor monitor) {
        this.monitor = monitor;

        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public TrackerHandler(NetworkStatusMonitor monitor, WebView trackerWebContainer) {
        this.monitor = monitor;

        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        this.trackerWebContainer = trackerWebContainer;
        this.trackerWebContainer.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                LMLog.i(TAG, "Error - "+errorCode+description);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }


        });
        this.trackerWebContainer.getSettings().setJavaScriptEnabled(true);

        this.trackerWebContainer.setWebChromeClient(new WebChromeClient(){

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                LMLog.i(TAG, consoleMessage.message());
                return true;
            }
        });
        this.trackerWebContainer.loadData(trackerJSHandler, "text/html", "UTF-8");
    }

    private static String replaceUriParameter(String url, String key, String newValue) {
        if (url == null || key == null || newValue == null) {
            return url;
        }
        Uri uri = Uri.parse(url);
        final Set<String> params = uri.getQueryParameterNames();
        final Uri.Builder newUri = uri.buildUpon().clearQuery();
        for (String param : params) {
            newUri.appendQueryParameter(param,
                    param.equals(key) ? newValue : uri.getQueryParameter(param));
        }
        return newUri.build().toString();
    }

    private void executeTracker(String url) {

        if (url == null) {
            return;
        }

        if (!URLUtil.isValidUrl(url)) {
            LMLog.w(TAG, "Invalid impression URL " + url);
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LMLog.d(TAG, "Tracking request failed with err: "+e.getLocalizedMessage()+"for [" + call.request().toString() + "]");

                    // Add url again for retry in persistent queue
                    final String url = call.request().url().toString();
                    if (url != null && url.length() > 0 && trackerDBHandler != null) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                trackerDBHandler.add(url);
                            }
                        });
                    }
                }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LMLog.d(TAG, "Tracking request completed for [" + call.request().toString() + "]");
            }
        });
    }

    public void sendRTBImpression(final List<Tracker> impressionList) {
        if (executeImpressionInWebContainer && this.trackerWebContainer != null) {
            for (Tracker impression : impressionList) {

                String url = impression.getUrl();
                if (url != null && URLUtil.isValidUrl(url)) {
                    String jsURL= String.format("javascript:fireURL('%s')",impression.getUrl());
                    LMLog.d("RTBTRACKER",jsURL);
                    this.trackerWebContainer.loadUrl(jsURL);
                }else {
                    LMLog.w(TAG, "Invalid impression URL " + url);
                }
            }
        }else {
            sendImpression(impressionList);
        }
    }

    public void sendImpression(final List<Tracker> impressionList) {

        if (impressionList == null || impressionList.isEmpty()) {
            return;
        }

        if (monitor != null && !monitor.isNetworkConnected()) {
            // Save for future execution
            for (Tracker impression : impressionList) {
                try {
                    String url = impression.getUrl();

                    if (url != null && url.length() > 0 && trackerDBHandler != null) {

                        TimeZone timeZone = this.timeZone;
                        if (timeZone == null) {
                            timeZone = TimeZone.getTimeZone("UTC");
                        }

                        sdf.setTimeZone(timeZone);
                        String ts = String.valueOf(sdf.format(new Date()));
                        // String ts = String.valueOf(df.format("yyyyMMddHHmmss", new java.util.Date()));
                        String updatedUrl = replaceUriParameter(url, "ts", ts);
                        LMLog.i(TAG, "Saving tracker " + url + " in persistent queue with updated time stamp URL " + impression.getUrl() + " bcoz network is not available");
                        LMLog.i(TAG, updatedUrl);
                        trackerDBHandler.add(updatedUrl);
                    }
                } catch (Exception e) {
                    LMLog.e(TAG, "Failed to save tracker " + impression.getUrl());
                }
            }
        } else {
            for (Tracker impression : impressionList) {
                executeTracker(impression.getUrl());
            }
        }
    }

    private void sendImpressionList(final List<String> impressionList) {

        if (impressionList == null || impressionList.isEmpty()) {
            LMLog.d(TAG, "ImpressionList is invalid");
            return;
        }

        for (String url : impressionList) {
            executeTracker(url);
        }
    }

    public void sendImpressionFromQueue() {

        if (monitor.isNetworkConnected()) {
            ArrayList<String> trackers = trackerDBHandler.popAll();
            LMLog.i(TAG, "Tracking Impression backlog count " + trackers.size());
            sendImpressionList(trackers);
        }
    }
}
