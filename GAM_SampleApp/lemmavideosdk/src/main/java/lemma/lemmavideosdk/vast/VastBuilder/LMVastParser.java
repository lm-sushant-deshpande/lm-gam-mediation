package lemma.lemmavideosdk.vast.VastBuilder;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;

import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.common.LMNetworkHandler;

public class LMVastParser {

    private static String TAG = "LMVastParser";

    Integer outstandingRequests = 0;
    ArrayList<AdI> vastAds = new ArrayList<>();
    VastProcessorListener completionListener;

    public static interface VastParserCompletionListener {
        public void onSuccess(Vast vast);

        public void onError(Vast vast, Error error);
    }

    static interface VastProcessorListener {

        public void onSuccess(ArrayList<AdI> vastAds);

        public void onError(Error error);
    }

    public void parse(String vastXml, final VastParserCompletionListener listener) {

        VastBuilder builder = new VastBuilder();
        builder.processWrapper = true;
        try {
            final Vast mainVast = builder.build(vastXml);
            processVast(mainVast, new VastProcessorListener() {
                @Override
                public void onSuccess(ArrayList<AdI> vastAds) {

                    final Vast vast = new Vast();
                    vast.ads = vastAds;

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSuccess(vast);
                        }
                    });

                }

                @Override
                public void onError(final Error error) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(mainVast, error);
                        }
                    });
                }
            });
        } catch (Exception e) {

        }
    }

    private void markRequestStart() {
        outstandingRequests++;
    }


    private void markRequestComplete() {
        outstandingRequests--;
        if (outstandingRequests < 1) {
            completionListener.onSuccess(vastAds);
        }
    }

    private void processWrapperAd(Vast vast, final Integer depth) {

        for (final AdI ad : vast.ads) {
            markRequestStart();

            if (ad.adTagUrl == null) {
                vastAds.add(ad);
                markRequestComplete();
            } else {

                if (depth > 2) {
                    vastAds.add(ad);
                    markRequestComplete();
                } else {

                    LMNetworkHandler handler = new LMNetworkHandler();
                    handler.fetch(ad.adTagUrl, new LMNetworkHandler.LMNetworkHandlerListener() {
                        @Override
                        public void onSuccess(String data) {

                            VastBuilder builder = new VastBuilder();
                            builder.processWrapper = true;
                            try {
                                Vast vast = builder.build(data);
                                for (AdI currentAd : vast.ads) {
                                    currentAd.parent = ad;
                                }

                                processWrapperAd(vast, depth + 1);
                            } catch (Exception e) {
                                LMLog.w(TAG, e.getLocalizedMessage());
                            } finally {
                                markRequestComplete();
                            }
                        }

                        @Override
                        public void onFailure(Error error) {
                            markRequestComplete();
                        }
                    });
                }
            }
        }
    }

    private void processVast(Vast vast, VastProcessorListener listener) {
        completionListener = listener;
        processWrapperAd(vast, 0);
    }
}
