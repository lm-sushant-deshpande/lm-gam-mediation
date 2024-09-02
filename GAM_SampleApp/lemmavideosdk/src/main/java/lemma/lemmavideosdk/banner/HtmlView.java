package lemma.lemmavideosdk.banner;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import lemma.lemmavideosdk.common.AppLog;
import lemma.lemmavideosdk.views.LMWebView;

public class HtmlView extends LMWebView {

    //public static final String HTML_WRAP = "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width,user-scalable=no,initial-scale=1,maximum-scale=1'></head><body>%s</body></html>";
      public static final String HTML_WRAP = "<html><head><meta name=\"viewport\" content=\"user-scalable=0\"/><style>body{margin:0;padding:0;}</style></head><body><div align=\"center\">%s</div></body></html>";
    private static final String TAG = "NonLinearView";
    public HtmlViewListener listener;
    private Context mContext = null;

    public HtmlView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    protected void init() {
        super.init();
        getSettings().setJavaScriptEnabled(true);
        setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                listener.onRender();
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                AppLog.d(TAG, "shouldOverrideUrlLoading.resource");
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && shouldLoadURL(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                AppLog.d(TAG, "shouldOverrideUrlLoading.url");
                return Build.VERSION.SDK_INT < Build.VERSION_CODES.N && shouldLoadURL(url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                listener.onError(new Error(errorCode + ""));
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
        addJavascriptInterface(this, "lmbridge");
    }

    @Override
    protected void launchLandingPage() {
        // No action required
    }

    private boolean shouldLoadURL(String url) {
        return openLandingPage(url);
    }

    public void loadHtml(String creative) {
        String finalHtml = String.format(HTML_WRAP, creative);
        // Added base url to handle protocol independent resources
        loadDataWithBaseURL("http://lemmadigital.com/", finalHtml, "text/html", "UTF-8", null);
    }

    // Cleans up object
    public void destroy() {
        stopLoading();
    }

    protected boolean openLandingPage(String url) {

        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        boolean result = false;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            mContext.startActivity(intent);
            result = true;
        } catch (ActivityNotFoundException e) {
            AppLog.w(TAG, "Activity not found for the uri: " + url);
        }

        AppLog.d(TAG, "Opening Browser with URL : " + url);

        sendClickTracking();
        return result;
    }

    private void sendClickTracking() {
        // No action required
    }

    public interface HtmlViewListener {

        void onClick();

        void onRender();

        void onError(Error error);
    }

}
