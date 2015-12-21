package com.sonaive.toaster;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class ResizeLayout extends FrameLayout {

    private int mMaxParentHeight = 0;
    private ArrayList<Integer> mHeightList = new ArrayList<>();

    public ResizeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mMaxParentHeight == 0) {
            mMaxParentHeight = h;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureHeight = measureHeight(heightMeasureSpec);
        mHeightList.add(measureHeight);
        if (mMaxParentHeight != 0) {
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int expandSpec = MeasureSpec.makeMeasureSpec(mMaxParentHeight, heightMode);
            super.onMeasure(widthMeasureSpec, expandSpec);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mHeightList.size() >= 2) {
            int oldH = mHeightList.get(0);
            int newH = mHeightList.get(mHeightList.size() - 1);
            int softHeight = mMaxParentHeight - newH;
            if (oldH == mMaxParentHeight) {
                if (mListener != null) {
                    mListener.onPopKeyboard(softHeight);
                }
            } else if (newH == mMaxParentHeight) {
                if (mListener != null) {
                    mListener.onCloseKeyboard(softHeight);
                }
            } else {
                if (mListener != null) {
                    mListener.onKeyboardHeightChange(softHeight);
                }
            }
            mHeightList.clear();
        } else {
            mHeightList.clear();
        }
    }

    private int measureHeight(int pHeightMeasureSpec) {
        int result = 0;
        int heightMode = MeasureSpec.getMode(pHeightMeasureSpec);
        int heightSize = MeasureSpec.getSize(pHeightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = heightSize;
                break;
        }
        return result;
    }

    private OnResizeListener mListener;

    public void setOnResizeListener(OnResizeListener listener) {
        mListener = listener;
    }

    public interface OnResizeListener {

        void onPopKeyboard(int height);

        void onCloseKeyboard(int height);

        void onKeyboardHeightChange(int height);
    }
}