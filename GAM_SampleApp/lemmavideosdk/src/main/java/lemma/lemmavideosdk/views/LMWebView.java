package lemma.lemmavideosdk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;

import java.util.Calendar;

import lemma.lemmavideosdk.common.LMLog;

public abstract class LMWebView extends WebView implements OnClickListener, OnTouchListener {

    private static String TAG = "PubWebView";
    private long mStartTouchTime = 0;

    public LMWebView(Context context) {
        super(context);
    }

    public LMWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mStartTouchTime = Calendar.getInstance().getTimeInMillis();
                break;
            }
            case MotionEvent.ACTION_UP: {
                long clickDuration = Calendar.getInstance().getTimeInMillis() - mStartTouchTime;

                if (clickDuration < 1000) {
                    LMLog.d(TAG, "View clicked for " + clickDuration + " msec");
                    performClick();
                } else {
                    LMLog.d(TAG, "View long press detected for " + clickDuration + " msec. Not handeled long press.");
                }
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        LMLog.d(TAG, "NonLinear/Companion view clicked.");
        launchLandingPage();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    protected void init() {
        setClickable(true);
        setOnClickListener(this);
        setOnTouchListener(this);
    }

    protected abstract void launchLandingPage();
}
