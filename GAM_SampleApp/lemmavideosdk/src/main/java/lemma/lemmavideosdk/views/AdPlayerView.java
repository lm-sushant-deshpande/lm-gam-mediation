package lemma.lemmavideosdk.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import java.io.File;

import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.vast.VastBuilder.AdI;
import lemma.lemmavideosdk.vast.VastBuilder.AdType;
import lemma.lemmavideosdk.vast.listeners.AdPlayerAdapter;
import lemma.lemmavideosdk.vast.manager.NetworkStatusMonitor;

public class AdPlayerView extends FrameLayout {

    public AdI ad;
    private Context mContext;
    private AdPlayerAdapter.AdPlayerCallback mAdPlayerCallback;
    private boolean markForFailure;
    //All UI controls
    private VideoView mAdVideoView;
    private NonLinearView mNonLinearView;

    private Handler mHandler = null;
    private Runnable mHandlerRunnable = null;
    private int nlTotalDuration = 6;
    private int nlCurrentDuration = 0;

    private ImageView imageView;
    private NonLinearView htmlView;
    private NetworkStatusMonitor networkStatusMonitor;
    private AdEventStatus adEventStatus;

    private String TAG = "AdVideoPlayer";

    public AdPlayerView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public AdPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public AdPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (visibility == View.VISIBLE) {
            mAdVideoView.requestFocus();
        }
    }

    //Plain image rendering
    private ImageView getImageView() {
        if (imageView == null) {
            ImageView imageVw = new ImageView(getContext());
            imageVw.setScaleType(ImageView.ScaleType.FIT_XY);
            imageVw.setBackgroundColor(Color.BLACK);
            addView(imageVw, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            imageView = imageVw;
        }
        return imageView;
    }

    //Html/script rendering
    private NonLinearView getHtmlView() {
        if (htmlView == null) {

            NonLinearView nonLinearView = new NonLinearView(mContext, createNonLinearTrackingListener());
            addView(nonLinearView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            htmlView = nonLinearView;
        }
        return htmlView;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        networkStatusMonitor = new NetworkStatusMonitor(getContext());
        mAdVideoView = new LMVideoView(getContext());

        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        relativeLayout.addView(mAdVideoView, relativeParams);


        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;

        addView(relativeLayout, params);
        setVideoListeners();

        final NonLinearView nonLinearView = new NonLinearView(mContext, createNonLinearTrackingListener());
        mNonLinearView = htmlView = nonLinearView;
        addView(mNonLinearView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mHandler = new Handler();
    }

    private NonLinearView.NonLinearTrackingListener createNonLinearTrackingListener() {
        // TODO Auto-generated method stub
        return new NonLinearView.NonLinearTrackingListener() {
            @Override
            public void onPageStarted() {

            }

            @Override
            public void onFirstQuartileReached() {

            }

            @Override
            public void onMidPointReached() {

            }

            @Override
            public void onThirdQuartileReached() {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onValidation(boolean isValid) {

            }

            @Override
            public void onError(int arg1, int arg2) {
                if (mHandler != null && mHandlerRunnable != null) {
                    mHandler.removeCallbacks(mHandlerRunnable);
                }
                mAdPlayerCallback.onAdPlayError(ad, 0, 0);
            }
        };
    }

//    public void setDimensions(final int width, final int height) {
//        if (mAdVideoView != null) {
//            mAdVideoView.setDimensions(width, height);
//        }
//    }

    public void setAdPlayerCallback(AdPlayerAdapter.AdPlayerCallback playerCallback) {
        mAdPlayerCallback = playerCallback;
    }

    private void setVideoListeners() {

        if (mAdVideoView == null) {
            return;
        }

        mAdVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                LMLog.i(TAG, "Error occured [" + what + ", " + extra + "] while playing Ad video.");
                mAdPlayerCallback.onAdPlayError(ad, what, extra);
                return true;
            }
        });

        mAdVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {

                mAdVideoView.setBackgroundColor(Color.TRANSPARENT);
                mAdPlayerCallback.onAdPlayerPrepared(ad);


                mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        LMLog.e(TAG, "MediaPlayer Error occured [" + what + ", " + extra + "] while playing Ad video.");
                        mAdPlayerCallback.onAdPlayError(ad, what, extra);
                        return true;
                    }
                });

                mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {

                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        mAdVideoView.start();
                    }
                });

                mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    // show updated information about the buffering progress
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        LMLog.d(TAG, "Buffer percent: " + percent);
                    }
                });

                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {

                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                            LMLog.d(TAG, "Video bufferingg started");
                        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                            LMLog.d(TAG, "Video bufferingg started");
                        } else if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            LMLog.d(TAG, "Video rendering started");
                        }
                        return false;
                    }
                });
            }
        });

        mAdVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                LMLog.i(TAG, "Ad video completed.");
                mAdVideoView.stopPlayback();
                if (!adEventStatus.complete) {
                    mAdPlayerCallback.onAdCompleted(ad);
                }

                if (mHandler != null && mHandlerRunnable != null) {
                    mHandler.removeCallbacks(mHandlerRunnable);
                }
            }
        });
    }

    private void onError(final int arg1, final int arg2) {
        mAdVideoView.stopPlayback();
        mAdPlayerCallback.onAdPlayError(ad, arg1, arg2);
    }

    public void loadAd(AdI ad) {

        adEventStatus = new AdEventStatus();
        markForFailure = false;

        this.ad = ad;

        if (ad.getType() == AdType.LINEAR) {

            if (isImageType(ad.mimeType())) {
                htmlView = null;
                mAdVideoView.setVisibility(INVISIBLE);

                String url = ad.getAdRL();
                File imgFile = new File(url);
                renderImageAd(imgFile);
            } else {
                htmlView.setVisibility(INVISIBLE);
                String url = ad.getAdRL();
                Uri uri = Uri.parse(url);
                mAdVideoView.setVideoURI(uri);
                mAdVideoView.setVisibility(VISIBLE);
            }

            nlTotalDuration = (int) ad.duration() / 1000;//adDuration/1000;


        } else if (ad.getType() == AdType.NONLINEAR) {

            mAdVideoView.setVisibility(INVISIBLE);

            nlTotalDuration = (int) ad.duration() / 1000;//adDuration/1000;
            String url = ad.getAdRL();

            if (ad.isUrl() || (ad.getAdRL() != null && ad.getAdRL().startsWith("/"))) {
                htmlView = null;
                File imgFile = new File(url);
                renderImageAd(imgFile);
            } else {
                getHtmlView().setAd(ad);
            }
        } else {
            markForFailure = true;
            LMLog.e(TAG, "No compatible ad found");
        }
    }

    private boolean isImageType(String type) {
        return (type.equalsIgnoreCase("image/jpeg") ||
                type.equalsIgnoreCase("image/png") ||
                type.equalsIgnoreCase("image/jpg"));
    }

    public void renderImageAd(File imgFile) {

        if (imgFile.exists()) {
            Picasso.with(mContext).load(imgFile).into(this.getImageView());
        } else {
            LMLog.i("AdPlayerView", "Marking for failure for ");
            markForFailure = true;
        }
    }

    public void playAd() {

        if ((!ad.isUrl() && !ad.getAdRL().startsWith("/")) && !networkStatusMonitor.isNetworkConnected()) {
            LMLog.i(TAG, "Error occured while playing Ad.");
            mAdPlayerCallback.onAdPlayError(ad, 1, 1);
            return;
        }

        if (markForFailure) {
            LMLog.i(TAG, "Error occured while playing Ad , marked ForFailure. loading ad may have failed");
            mAdPlayerCallback.onAdPlayError(ad, 1, 1);
            return;
        }

        LMLog.i(TAG, "Playing ad " + ad);
        if (mAdVideoView != null && mAdVideoView.getVisibility() == VISIBLE) {
            mAdVideoView.start();
        } else if (htmlView != null) {
            htmlView.startAd();
            mAdPlayerCallback.onAdPlayerPrepared(ad);
        } else if (imageView != null) {
            mAdPlayerCallback.onAdPlayerPrepared(ad);
        }
        scheduleNlTracking();
    }

    private void scheduleNlTracking() {

        mHandlerRunnable = new Runnable() {
            @Override
            public void run() {

                if (mAdVideoView != null && mAdVideoView.getVisibility() == VISIBLE) {
                    nlCurrentDuration = (mAdVideoView.getCurrentPosition() / 1000);
                } else {
                    nlCurrentDuration++;
                }

                if (nlCurrentDuration < nlTotalDuration) {

                    if (nlCurrentDuration >= (nlTotalDuration * (3.0 / 4)) &&
                            !adEventStatus.thirdQuartile) {
                        mAdPlayerCallback.onThirdQuartileReached(ad);
                        adEventStatus.thirdQuartile = true;
                    } else if (nlCurrentDuration >= (nlTotalDuration * (1.0 / 2)) &&
                            !adEventStatus.midPoint) {
                        mAdPlayerCallback.onMidPointReached(ad);
                        adEventStatus.midPoint = true;
                    } else if (nlCurrentDuration >= (nlTotalDuration * (1.0 / 4)) &&
                            !adEventStatus.firstQuartile) {
                        mAdPlayerCallback.onFirstQuartileReached(ad);
                        adEventStatus.firstQuartile = true;
                    }

                    if (mHandler != null) {
                        mHandler.postDelayed(this, 1000);
                    }
                    return;
                }

                if (mHandler != null) {
                    mHandler.removeCallbacks(this);
                }
                if (!adEventStatus.complete) {
                    mAdPlayerCallback.onAdCompleted(ad);
                }
                adEventStatus.complete = true;
            }
        };
        mHandler.postDelayed(mHandlerRunnable, 1000L);
    }

    private void clean() { }

    public void destroy() {
        if (mAdVideoView != null) {
            mAdVideoView.stopPlayback();
            ViewGroup viewGroup = (ViewGroup)mAdVideoView.getParent();
            if (viewGroup != null){
                viewGroup.removeView(mAdVideoView);
            }
        }
        imageView = null;
        if (htmlView != null) {
            if (htmlView.getParent() != null) {
                ((ViewGroup) htmlView.getParent()).removeView(htmlView);
            }
            htmlView.removeAllViews();
            htmlView.destroyView();
            htmlView.destroy();
        }
        htmlView = null;
        mHandler = null;
    }

    class AdEventStatus {
        public boolean firstQuartile;
        public boolean midPoint;
        public boolean thirdQuartile;
        public boolean complete;
    }

}
