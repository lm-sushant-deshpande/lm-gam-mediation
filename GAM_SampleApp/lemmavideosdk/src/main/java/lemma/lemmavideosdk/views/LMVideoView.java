package lemma.lemmavideosdk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class LMVideoView extends VideoView {

    private int mForceHeight = 0;
    private int mForceWidth = 0;
    private DisplayMode screenMode = DisplayMode.ORIGINAL;

    public LMVideoView(Context context) {
        super(context);
        init();
    }

    public LMVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LMVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public DisplayMode getScreenMode() {
        return screenMode;
    }

    public void setScreenMode(DisplayMode screenMode) {
        this.screenMode = screenMode;
        requestLayout();
        invalidate();
    }

    private void init() {

    }

    public void setDimensions(int w, int h) {
        this.mForceHeight = h;
        this.mForceWidth = w;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mForceWidth > 0 && mForceHeight > 0) {
            setMeasuredDimension(mForceWidth, mForceHeight);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        requestLayout();
        invalidate();
    }

    public enum DisplayMode {
        ORIGINAL,       // original aspect ratio
        FULL_SCREEN,    // fit to screen
        ZOOM            // zoom in
    }
}
