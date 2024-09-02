package lemma.lemmavideosdk.model;

import android.view.ViewGroup;

class CompanionViewInfo {

    private ViewGroup mCompanionUiContainer;
    private int mWidth = 0;
    private int mHeight = 0;

    public CompanionViewInfo() {
        // TODO Auto-generated constructor stub
    }

    public void setSize(int width, int height) {
        this.setWidth(width);
        this.setHeight(height);
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
        this.mWidth = width;
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
        this.mHeight = height;
    }

    /**
     * @return the companionUiContainer
     */
    public ViewGroup getUiContainer() {
        return mCompanionUiContainer;
    }

    /**
     * @param companionUiContainer the companionUiContainer to set
     */
    public void setUiContainer(ViewGroup companionUiContainer) {
        this.mCompanionUiContainer = companionUiContainer;
    }
}
