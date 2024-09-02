package lemma.lemmavideosdk.views;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;

import lemma.lemmavideosdk.common.LMError;
import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.vast.VastBuilder.AdI;


public class NonLinearView extends LMWebView {

//    public static final String HTML_WRAP = "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width,user-scalable=no,initial-scale=1,maximum-scale=1'><style>html,body{height:100%s;margin:0;padding:0;overflow:hidden;}img,video{width:100%s!important;height:auto!important}#lemmaAd{height:100%s!important;max-height:100%s;text-align:center;display:flex;flex-direction:column;justify-content:center;resize:vertical;}</style>%s</head><body><div id='lemmaAd'>%s</div></body></html>";
    public static final String HTML_WRAP = "<html><title>Lemma</title><head>%s</head><body>%s</body></html>";
    private static final String TAG = "NonLinearView";
    private static final int FIRST_QUARTILE = 101;
    private static final int MID_POINT = 102;
    private static final int THIRD_QUARTILE = 103;
    private static final int COMPLETE = 104;
    private static final int INVALID_URL_EVENT = 106;
    final String BRIDGE_SCRIPT = "<script>\n" +
            "lemmaPassback = function () { \n" +
            "\tlmbridge.doPassback();\n" +
            "};\n" +
            "</script>\n";
    private Handler mHandler = null;
    private NonLinearTrackingListener mObserver = null;
    private boolean AD_CLOSED = false;
    private AdI ad = null;

    public NonLinearView(Context context, NonLinearTrackingListener listener) {
        super(context);
        mObserver = listener;
        init();
    }

    public NonLinearView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public NonLinearTrackingListener getTrackingListener() {
        return mObserver;
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    protected void init() {
        super.init();
        mHandler = new NonLinearViewHandler(this);
        getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        setSoundEffectsEnabled(true);
        setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                LMLog.i(TAG, consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }

        });
        setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                if (mHandler != null) {
                    long adDuration = ad.duration();
                    if (adDuration <= 0) {
                        adDuration = 10 * 1000;//1000 is to convert sec -> msec
                    }
                    mHandler.sendEmptyMessageDelayed(FIRST_QUARTILE, adDuration / 4);
                    mHandler.sendEmptyMessageDelayed(MID_POINT, adDuration / 2);
                    mHandler.sendEmptyMessageDelayed(THIRD_QUARTILE, adDuration * 3 / 4);
                    mHandler.sendEmptyMessageDelayed(COMPLETE, adDuration);
                }
                if (mObserver != null) {
                    mObserver.onPageStarted();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                LMLog.d(TAG,"shouldOverrideUrlLoading.resource");
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && shouldLoadURL(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                LMLog.d(TAG,"shouldOverrideUrlLoading.url");
                return Build.VERSION.SDK_INT < Build.VERSION_CODES.N && shouldLoadURL(url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                onError(LMError.VAST_GENERAL_NONLINEAR_ERROR, 0);
            }

            
        });
        addJavascriptInterface(this, "lmbridge");
    }

    @JavascriptInterface
    public void doPassback() {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                LMLog.e(TAG, "do Pass back called");
                mObserver.onError(LMError.VAST_GENERAL_NONLINEAR_ERROR, 0);
            }
        });
    }

    private boolean shouldLoadURL(String url){
        return openLandingPage(url);
    }

    public void setAd(AdI ad) {
        this.ad = ad;
    }

    public void startAd() {
        loadData();
    }

    private void loadData() {

        if (this.ad == null) {
            LMLog.e("", "Null ?");
            return;
        }
        if (this.ad.isUrl()) {
            loadUrl(this.ad.getAdRL());
        } else {

            String creative = this.ad.getAdRL();
            String html = String.format(HTML_WRAP, BRIDGE_SCRIPT, creative);

            // Added base url to handle protocol independent resources
            loadDataWithBaseURL("http://lemmadigital.com/", html, "text/html", "UTF-8", null);
        }
    }

    private void onError(final int arg1, final int arg2) {
        if (mObserver != null)
            mObserver.onError(arg1, arg2);
    }

    public void destroyView() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
    }

    protected void launchLandingPage(){

    }

    protected boolean openLandingPage(String url) {

            if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }

            boolean result = false;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            getContext().startActivity(intent);
            result = true;
        } catch (ActivityNotFoundException e) {
            LMLog.w(TAG, "Activity not found for the uri: " + url);
        }

        LMLog.d(TAG, "Opening Browser with URL : " + url);

            sendClickTracking();
            return result;
    }

    private void sendClickTracking() { }

    /**
     * @return the aD_CLOSED
     */
    public boolean isAdClosed() {
        return AD_CLOSED;
    }

    /**
     * @param state the close state to set
     */
    public void setAdStateToClose(boolean state) {
        AD_CLOSED = state;
    }

    public interface NonLinearTrackingListener {

        void onPageStarted();

        void onFirstQuartileReached();

        void onMidPointReached();

        void onThirdQuartileReached();

        void onComplete();

        void onValidation(boolean isValid);

        void onError(final int arg1, final int arg2);
    }

    static private class NonLinearViewHandler extends Handler {
        private final WeakReference<NonLinearView> mView;

        public NonLinearViewHandler(NonLinearView view) {
            mView = new WeakReference<NonLinearView>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            NonLinearView view = mView.get();
            if (view == null) {
                return;
            }
            NonLinearTrackingListener trackingListener = view.getTrackingListener();

            switch (msg.what) {
                case FIRST_QUARTILE:
                    if (trackingListener != null && !view.isAdClosed()) {
                        Log.d(TAG, "FIRST_QUARTILE reached.");
                        trackingListener.onFirstQuartileReached();
                    }
                    break;
                case MID_POINT:
                    if (trackingListener != null && !view.isAdClosed()) {
                        Log.d(TAG, "MID_POINT reached.");
                        trackingListener.onMidPointReached();
                    }
                    break;
                case THIRD_QUARTILE:
                    if (trackingListener != null && !view.isAdClosed()) {
                        Log.d(TAG, "THIRD_QUARTILE reached.");
                        trackingListener.onThirdQuartileReached();
                    }
                    break;
                case COMPLETE:
                    //((ViewGroup)view.getParent()).removeView(view);//View will be removed by LMVideoAdManager
                    if (trackingListener != null && !view.isAdClosed()) {
                        Log.d(TAG, "COMPLETE reached.");
                        trackingListener.onComplete();
                    }
                    break;

                case INVALID_URL_EVENT:
                    if (msg.arg2 == 1)
                    //if(true)
                    {
                        view.loadData();
                        if (trackingListener != null) {
                            trackingListener.onValidation(true);
                        }
                    } else {
                        view.mObserver.onError(LMError.VAST_NONLINEAR_RESOURCE_NOT_FETCHED, 0);
                    }
                    break;
            }
        }
    }
}
