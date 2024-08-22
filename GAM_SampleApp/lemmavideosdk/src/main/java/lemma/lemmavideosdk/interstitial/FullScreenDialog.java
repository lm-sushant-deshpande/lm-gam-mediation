package lemma.lemmavideosdk.interstitial;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;


import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.gam_sampleapp.R;

import java.lang.ref.WeakReference;


import lemma.lemmavideosdk.common.LMUtils;
import lemma.lemmavideosdk.videointerstitial.LMVideoInterstitial;

public class FullScreenDialog extends Dialog {

    // Fixed width, height for the close button
    public static final int CLOSE_BTN_WIDTH = LMUtils.convertDpToPixel(38);
    public static final int CLOSE_BTN_HEIGHT = LMUtils.convertDpToPixel(38);
    private static final String TAG = "FullScreenDialog";
    private OnDialogCloseListener closeListener;
    private FrameLayout frameLayout;
    private Window dialogWindow;
    private WeakReference<Context> contextWeakReference;
    private   boolean isShowAdCloseButton=true;

    public FullScreenDialog(@NonNull WebView webView, @NonNull OnDialogCloseListener closeListener, Boolean isShowAdCloseButton) {
        super(webView.getContext(), android.R.style.Theme_Black_NoTitleBar);
        this.isShowAdCloseButton =isShowAdCloseButton;
        contextWeakReference = new WeakReference<>(webView.getContext());
        init(closeListener);
        setContentView(newContentView(webView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public FullScreenDialog(@NonNull View videoView, @NonNull OnDialogCloseListener closeListener,Boolean isShowAdCloseButton) {
        super(videoView.getContext(), android.R.style.Theme_Black_NoTitleBar);
        this.isShowAdCloseButton =isShowAdCloseButton;
        contextWeakReference = new WeakReference<>(videoView.getContext());
        init(closeListener);
        setContentView(newContentView(videoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void init(OnDialogCloseListener closeListener) {
        this.closeListener = closeListener;
        dialogWindow = getWindow();
    }

    private FrameLayout newContentView(View view, int width, int height) {

        frameLayout = new FrameLayout(contextWeakReference.get());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width,
                height);
        params.gravity = Gravity.CENTER;
        params.setMargins(0, 0, 0, 0);
        frameLayout.addView(view, params);

        ImageView closeBtn = new ImageButton(contextWeakReference.get());
        closeBtn.setBackgroundResource(R.drawable.ic_close_btn);

        params = new FrameLayout.LayoutParams(CLOSE_BTN_WIDTH, CLOSE_BTN_HEIGHT);
        params.gravity = Gravity.END;
        params.rightMargin = LMUtils.convertDpToPixel(5);
        params.topMargin = LMUtils.convertDpToPixel(5);

        if(isShowAdCloseButton){
            frameLayout.addView(closeBtn, params);
        }
        frameLayout.setBackgroundColor(Color.BLACK);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return frameLayout;
    }

    /**
     * Default onCreate of dialog to set dismiss listener and handle recreate of it
     *
     * @param savedInstanceState Saved instance object
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            dismiss();
        }
    }

    @Override
    protected void onStop() {
        frameLayout.removeAllViews();
        frameLayout = null;
        dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        closeListener.onClose();
        closeListener = null;
        super.onStop();
    }

    /**
     * Dialogs default callback to handle custom backpress
     */
    @Override
    public void onBackPressed() {
        dismiss();
    }

    /**
     * Method to hide the status bar
     */
    private void hideUISystemUI() {
        dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideUISystemUI();
        }
    }

    /**
     * Interface close listener
     */
    public interface OnDialogCloseListener {
        void onClose();
    }

}