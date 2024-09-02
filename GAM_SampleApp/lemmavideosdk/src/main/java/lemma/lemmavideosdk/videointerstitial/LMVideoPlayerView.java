package lemma.lemmavideosdk.videointerstitial;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;

import lemma.lemmavideosdk.common.AppLog;
import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.common.LMUtils;


public class LMVideoPlayerView extends FrameLayout implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnVideoSizeChangedListener {

    private static final int SEEK_BAR_LEFT_RIGHT_MARGIN = -15;
    private static final int SEEK_BAR_BOTTOM_MARGIN = 0;
    private static final String TAG = "LMVideoPlayerView";
    private static final double PROGRESS_UPDATE_DELAY = 0.5;
    private boolean onStartNotified = false;
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private LMVideoPlayerListener listener;
    private Timer timer;
    private boolean autoPlayOnForeground;
    private boolean isMute;
    private SeekBar seekBar;
    private AdEventStatus adEventStatus;

    private Handler mHandler = null;
    private Runnable mHandlerRunnable = null;

    private int currentDuration = 0;
    private int totalDuration = 0;

    public LMVideoPlayerView(Context context) {
        super(context);
        initVideoView();
        init();
    }

    private SeekBar createSeekBar() {
        SeekBar seekBar = new SeekBar(getContext());
        seekBar.setThumb(null);
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_ATOP);

        seekBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        return seekBar;
    }


    private void init() {

        LayoutParams seekBarParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LMUtils.convertDpToPixel(3));
        seekBarParams.gravity = Gravity.BOTTOM;
        seekBarParams.leftMargin = LMUtils.convertDpToPixel(SEEK_BAR_LEFT_RIGHT_MARGIN);
        seekBarParams.rightMargin = LMUtils.convertDpToPixel(SEEK_BAR_LEFT_RIGHT_MARGIN);
        seekBarParams.bottomMargin = LMUtils.convertDpToPixel(SEEK_BAR_BOTTOM_MARGIN);
        // Add and align seek bar
        seekBar = createSeekBar();
        addView(seekBar, seekBarParams);
    }


    private void initVideoView() {
        surfaceView = new SurfaceView(getContext());
        //Add Surface holder callbacks
        surfaceView.getHolder().addCallback(this);
        FrameLayout.LayoutParams layoutparams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        layoutparams.gravity = Gravity.CENTER;
        addView(surfaceView, layoutparams);
    }

    public void setAutoPlayOnForeground(boolean autoPlayOnForeground) {
        this.autoPlayOnForeground = autoPlayOnForeground;
    }

    /**
     * Loads the video for given Uri
     */
    public void load(Uri uri) {
        prepareVideo(uri);
    }

    private void prepareVideo(Uri uri) {
        initMediaPlayer();
        try {
            mediaPlayer.setDataSource(getContext(), uri);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            AppLog.d(TAG, e.getMessage());
            if (null != listener) {
                listener.onFailure(100, e.getMessage());
            }
        }
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnVideoSizeChangedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int w, int e) {
                //The player just pushed the first video frame for rendering after play/resume.
                if (w == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START && !onStartNotified) {
                    listener.onStart();
                    onStartNotified = true;
                    return true;
                }
                return false;
            }
        });

    }

    public void play() {
        if (null != mediaPlayer) {
            mediaPlayer.start();
            adEventStatus = new AdEventStatus();
            mHandler = new Handler();
            scheduleTracking(mediaPlayer);
            if (null != seekBar) {
                seekBar.setMax(getMediaDuration());
            }
        } else {
            AppLog.w(TAG, "mediaPlayer :" + null);
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            AppLog.w(TAG, "mediaPlayer :" + mediaPlayer);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void setListener(@NonNull LMVideoPlayerListener listener) {
        this.listener = listener;
    }

    public void stop() {
        stopProgressTimer();
        if (null != mediaPlayer) {
            mediaPlayer.stop();
        }
    }

    public void seekTo(int position) {
        if (null != mediaPlayer) {
            mediaPlayer.seekTo(position);
        }
    }

    public int getSeekPosition() {
        if (null != mediaPlayer) {
            return mediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    /**
     * Mute audio of VideoPlayer, notifies it to video controller
     */
    public void mute() {
        if (null != mediaPlayer && mediaPlayer.isPlaying()) {
            isMute = true;
            mediaPlayer.setVolume(0, 0);
        } else {
            AppLog.w(TAG, "mediaPlayer :" + mediaPlayer);
        }
    }

    public void unMute() {
        if (null != mediaPlayer && mediaPlayer.isPlaying()) {
            isMute = false;
            mediaPlayer.setVolume(1, 1);
        } else {
            AppLog.w(TAG, "mediaPlayer :" + mediaPlayer);
        }
    }

    public boolean isMute() {
        return isMute;
    }

    public int getMediaDuration() {
        if (null != mediaPlayer) {
            return mediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    /**
     * Cleanup
     */
    public void destroy() {
        if(mHandler != null){
            mHandler.removeCallbacks(mHandlerRunnable);
            mHandler = null;
            mHandlerRunnable = null;
        }
        stop();
        removeAllViews();
        surfaceView = null;
        if (null != mediaPlayer) {
            mediaPlayer.release();
        }
        mediaPlayer = null;
        listener = null;


    }

    private void stopProgressTimer() {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (null != listener) {
            listener.onBufferUpdate(percent);
        }
    }

    /**
     * Callback invoked once the media player has completed it playback
     * also notifies it to reference of LMVideoPlayerListener
     *
     * @param mp instance of media player
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (null != listener) {
            listener.onCompletion();

        }
    }


    public void onFirstQuartileReached() {
        listener.onFirstQuartileReached();
    }

    public void onMidPointReached() {
        listener.onMidPointReached();
    }

    public void onThirdQuartileReached() {
        listener.onThirdQuartileReached();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        String errorMessage;
        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO: {
                errorMessage = "MEDIA_ERROR_IO";
                break;
            }
            case MediaPlayer.MEDIA_ERROR_MALFORMED: {
                errorMessage = "MEDIA_ERROR_MALFORMED";
                break;
            }
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED: {
                errorMessage = "MEDIA_ERROR_UNSUPPORTED";
                break;
            }
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT: {
                errorMessage = "MEDIA_ERROR_TIMED_OUT";
                break;
            }
            default: {
                errorMessage = "error message not found!";
                break;
            }
        }
        AppLog.e(TAG, "errorCode: " + extra + ", errorMsg:" + errorMessage);
        if (null != listener) {
            listener.onFailure(extra, errorMessage);
        }
        return true;
    }

    /**
     * Called when the media content is available for playback.
     * also notifies it to reference of LMVideoPlayerListener
     *
     * @param mp current instance of media player
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (null != listener) {

            listener.onReady(this);

        }
    }

    public void scheduleTracking(final MediaPlayer mp) {
        totalDuration = (mp.getDuration() / 1000);
        LMLog.d("Total Duration",String.valueOf(totalDuration));
        mHandlerRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                     currentDuration = (mp.getCurrentPosition() / 1000);
                     LMLog.d("Current Duration",String.valueOf(currentDuration));
                    if (currentDuration < totalDuration) {
                        if (currentDuration >= (totalDuration * (3.0 / 4)) && !adEventStatus.thirdQuartile) {
                            onThirdQuartileReached();
                            adEventStatus.thirdQuartile = true;
                        } else if (currentDuration >= (totalDuration * (1.0 / 2)) &&
                                !adEventStatus.midPoint) {
                            onMidPointReached();
                            adEventStatus.midPoint = true;
                        } else if (currentDuration >= (totalDuration * (1.0 / 4)) &&
                                !adEventStatus.firstQuartile) {
                            onFirstQuartileReached();
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
                } catch (Exception e) {
                    LMLog.d("Exception", e.toString());
                }
            }

        };
        mHandler.postDelayed(mHandlerRunnable, 1000L);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (null != mediaPlayer) {
            mediaPlayer.setDisplay(holder);
            startProgressTimer();
        }
        if (autoPlayOnForeground) {
            play();
        }
    }

    private void startProgressTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (null != seekBar && null != mediaPlayer) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
            }
        }, 0, (long) (1000 * PROGRESS_UPDATE_DELAY));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // No Action required
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopProgressTimer();
        pause();
        if (null != mediaPlayer) {
            mediaPlayer.setDisplay(null);
        }
    }

    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        setVideoSize(mp);
    }

    private void setVideoSize(MediaPlayer mediaPlayer) {
        // Get the dimensions of the video
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;

        // Get the width of the screen
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        float screenProportion = (float) screenWidth / (float) screenHeight;

        // Get the SurfaceView layout parameters
        ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
        if (videoProportion > screenProportion) {
            layoutParams.width = screenWidth;
            layoutParams.height = (int) ((float) screenWidth / videoProportion);
        } else {
            layoutParams.width = (int) (videoProportion * (float) screenHeight);
            layoutParams.height = screenHeight;
        }
        // Commit the layout parameters
        surfaceView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (null != mediaPlayer) {
            setVideoSize(mediaPlayer);
        }
    }

    /**
     * Video Player callback methodss.
     */
    public interface LMVideoPlayerListener {
        void onReady(LMVideoPlayerView player);

        void onFailure(int errorCode, String errorMessage);

        void onBufferUpdate(int buffer);

        void onCompletion();

        void onStart();

        void onPause();

        void onProgressUpdate(int seekPosition);

        void onFirstQuartileReached();

        void onMidPointReached();

        void onThirdQuartileReached();
    }

    class AdEventStatus {
        public boolean firstQuartile;
        public boolean midPoint;
        public boolean thirdQuartile;
        public boolean complete;
    }
}