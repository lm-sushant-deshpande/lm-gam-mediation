package lemma.lemmavideosdk.interstitial;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.Map;

import lemma.lemmavideosdk.banner.HtmlView;
import lemma.lemmavideosdk.common.AppLog;
import lemma.lemmavideosdk.common.BidDetail;
import lemma.lemmavideosdk.common.DisplayableAdI;
import lemma.lemmavideosdk.common.LMUtils;
import lemma.lemmavideosdk.common.RTBRequestBuilder;
import lemma.lemmavideosdk.vast.manager.AdLoader;
import lemma.lemmavideosdk.vast.manager.AdvertisingIdClient;
import lemma.lemmavideosdk.vast.manager.LMAdRequest;
import lemma.lemmavideosdk.vast.manager.LMAppInfo;
import lemma.lemmavideosdk.vast.manager.LMDeviceInfo;
import lemma.lemmavideosdk.vast.manager.LMLocationManager;
import lemma.lemmavideosdk.vast.manager.NetworkStatusMonitor;

public class LMInterstitial {

    String TAG = "LMInterstitial";
    AdLoader mAdLoader;
    LMAdRequest mAdRequest;
    Context mContext;
    RTBRequestBuilder requestBuilder;
    LMInterstitialListener lmInterstitialListener;
    private HtmlView htmlView = null;
    public Boolean ShowAdCloseButton = true;


    public LMInterstitial(@NonNull Context context, String pubId, String adUnitId, String adServerUrl) {
        mAdRequest = new LMAdRequest(pubId, adUnitId);
        if (adServerUrl != null) {
            mAdRequest.setAdServerBaseURL(adServerUrl);
        }
        mContext = context;
        mAdRequest.isInterstitial = true;
        mAdRequest.setWidth(320);
        mAdRequest.setHeight(480);
        requestBuilder = new RTBRequestBuilder(mAdRequest, context);
    }

    public LMInterstitial(@NonNull Context context, String pubId, String adUnitId) {
        this(context, pubId, adUnitId, null);
    }

    public void setListener(LMInterstitialListener listener) {
        this.lmInterstitialListener = listener;
    }

    public void loadAd() {

        mAdLoader = new AdLoader(new AdLoader.AdLoaderListener() {
            @Override
            public void onSuccess(DisplayableAdI ad) {
                final BidDetail bid = (BidDetail) ad;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        render(bid);
                    }
                });
            }

            @Override
            public void onError(final Error err) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        lmInterstitialListener.onAdFailed(LMInterstitial.this, err);
                    }
                });

            }
        });

//        AdURLBuilder adURLBuilder = new AdURLBuilder(mAdRequest);
//        adURLBuilder.advertisingIdClient = new AdvertisingIdClient(mContext);
//        adURLBuilder.displayMetrics = mContext.getResources().getDisplayMetrics();
//        adURLBuilder.appInfo = new LMAppInfo(mContext.getApplicationContext());
//        adURLBuilder.networkStatusMonitor = new NetworkStatusMonitor(mContext);
//        adURLBuilder.deviceInfo = new LMDeviceInfo(mContext);
//        adURLBuilder.locationManager = new LMLocationManager(mContext);
//
//        mAdLoader.load(adURLBuilder.build(new HashMap<String, String>()));

        RTBRequestBuilder adURLBuilder = requestBuilder;
        adURLBuilder.advertisingIdClient = new AdvertisingIdClient(mContext);
        adURLBuilder.displayMetrics = mContext.getResources().getDisplayMetrics();
        adURLBuilder.appInfo = new LMAppInfo(mContext.getApplicationContext());
        adURLBuilder.networkStatusMonitor = new NetworkStatusMonitor(mContext);
        adURLBuilder.deviceInfo = new LMDeviceInfo(mContext);
        adURLBuilder.locationManager = new LMLocationManager(mContext);
        adURLBuilder.shouldAddDisplayManager = true;

        String url = String.format("%s/lemma/servad", LMUtils.SERVER_URL);
        if (mAdRequest.getAdServerBaseURL() != null) {
            try {
                Uri uri = Uri.parse(mAdRequest.getAdServerBaseURL());
                url = uri.toString();
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage());
            }
        }
        Uri.Builder builder = Uri.parse(url).buildUpon();
        builder.appendQueryParameter("pid", mAdRequest.getPublisherId());
        builder.appendQueryParameter("aid", mAdRequest.getAdUnitId());
        if (mAdRequest.map != null) {
            for (Map.Entry<String, String> entry : mAdRequest.map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key != null && value != null) {
                    builder.appendQueryParameter(key, value);
                }
            }
        }

        mAdLoader.load(builder.build().toString(), adURLBuilder.build());
    }

    private void render(BidDetail bid) {

        try {
            htmlView = new HtmlView(mContext);
        } catch (Exception e) {
            AppLog.e(TAG, "WebView Object Creation failed");
            lmInterstitialListener.onAdFailed(LMInterstitial.this,new Error(e));
            return;
        }

        htmlView.listener = new HtmlView.HtmlViewListener() {
            @Override
            public void onClick() {

            }

            @Override
            public void onRender() {
                lmInterstitialListener.onAdReceived(LMInterstitial.this);
            }

            @Override
            public void onError(Error error) {
                lmInterstitialListener.onAdFailed(LMInterstitial.this, error);
            }
        };
        htmlView.loadHtml(bid.creative);
    }

    public void show() {

        final FullScreenDialog interstitialDialog = new FullScreenDialog(htmlView, new FullScreenDialog.OnDialogCloseListener() {
            @Override
            public void onClose() {
                AppLog.d(TAG, "interstitialDialog onClose");
              //  Log.d(TAG, "interstitialDialog onClose");
                if (null != lmInterstitialListener) {
                    lmInterstitialListener.onAdClosed(LMInterstitial.this);
                }
            }
        },this.ShowAdCloseButton);
        interstitialDialog.show();


        // Notify Interstitial ad about ad interaction.
        if (null != lmInterstitialListener) {
            lmInterstitialListener.onAdOpened(this);
        }

    }

    public void destroy() {
        if (htmlView != null) {
            htmlView.destroy();
            htmlView = null;
        }
    }

    public interface LMInterstitialListener {

        void onAdReceived(LMInterstitial ad);

        void onAdFailed(LMInterstitial ad, Error error);

        void onAdOpened(LMInterstitial ad);

        void onAdClosed(LMInterstitial ad);

    }
}
