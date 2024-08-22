package lemma.lemmavideosdk.banner;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Map;

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

public class LMBannerView extends FrameLayout {

    String TAG = "LMBannerView";
    AdLoader mAdLoader;
    LMAdRequest mAdRequest;
    Context mContext;
    RTBRequestBuilder requestBuilder;
    private HtmlView htmlView = null;
    private BannerViewListener mBannerViewListener;
    private LMAdSize requestedAdSize;

    public LMBannerView(@NonNull Context context, @NonNull String pubId, @NonNull String adUnitId, @NonNull LMAdSize adSize, String adServerUrl) {
        super(context);
        requestedAdSize = adSize;
        mAdRequest = new LMAdRequest(pubId, adUnitId);
        if (adServerUrl != null) {
            mAdRequest.setAdServerBaseURL(adServerUrl);
        }
        mContext = context;
        mAdRequest.setWidth(adSize.width);
        mAdRequest.setHeight(adSize.height);
        requestBuilder = new RTBRequestBuilder(mAdRequest, context);
    }

    public LMBannerView(@NonNull Context context, @NonNull String pubId, @NonNull String adUnitId, @NonNull LMAdSize adSize) {
        this(context, pubId, adUnitId, adSize, null);
    }

    public void setBannerViewListener(BannerViewListener listener) {
        this.mBannerViewListener = listener;
    }

    public void destroy() {
        if (htmlView != null) {
            htmlView.destroy();
            htmlView = null;
        }
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
                        mBannerViewListener.onAdError(err);
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


//        TODO : Enable when RTB params are fixed
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

    private void attachAd(HtmlView view) {

        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;

        if (requestedAdSize != null) {
            width = LMUtils.convertDpToPixel(requestedAdSize.width);
            height = LMUtils.convertDpToPixel(requestedAdSize.height);
        }
        FrameLayout.LayoutParams params = new LayoutParams(width, height);
        addView(view, params);
    }

    private void render(final BidDetail bid) {

        try {
            htmlView = new HtmlView(mContext);
        } catch (Exception e) {
            AppLog.e(TAG, "WebView Object Creation failed");
            mBannerViewListener.onAdError(new Error(e.toString()));
            return;
        }

        htmlView.listener = new HtmlView.HtmlViewListener() {
            @Override
            public void onClick() {

            }

            @Override
            public void onRender() {
                mBannerViewListener.onAdReceived();
                attachAd(htmlView);
            }

            @Override
            public void onError(Error error) {
                mBannerViewListener.onAdError(error);
            }
        };
        htmlView.loadHtml(bid.creative);
    }

    public interface BannerViewListener {
        void onAdReceived();

        void onAdError(Error error);
    }

    public static class LMAdSize {

        public int width;
        public int height;

        public LMAdSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

}
