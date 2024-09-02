package lemma.lemmavideosdk.model;

import android.view.ViewGroup;

class NonLinearViewInfo {

    private int mWidth;
    private int mHeight;
    private ViewGroup mParentView;

    public NonLinearViewInfo() {

    }

    public NonLinearViewInfo(int width, int height) {
        mHeight = height;
        mWidth = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        mHeight = height;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        mWidth = width;
    }

    /**
     * @return the parentView
     */
    public ViewGroup getUiContainer() {
        return mParentView;
    }

    /**
     * @param parentView the parentView to set
     */
    public void setUiContainer(ViewGroup parentView) {
        this.mParentView = parentView;
    }
}
