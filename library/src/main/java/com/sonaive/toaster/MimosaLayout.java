package com.sonaive.toaster;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Mimosa is a sensitive plant, as long as its leaves gently touched, it folded up.
 * That's why i call it MimosaLayout. It contains two children, one is ListView or RecyclerView,
 * the other one is a input panel at the bottom of the container, overlaid above ListView or RecyclerView.
 * Created by liutao on 12/19/15.
 */
public class MimosaLayout extends ViewGroup {

    private View mContentView;
    private View mInputPanel;
    private Toaster mToaster;

    public MimosaLayout(Context context) {
        super(context);
    }

    public MimosaLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setToaster(Toaster toaster) {
        mToaster = toaster;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 2) {
            throw new IllegalArgumentException("MimosaLayout should only hold two children!");
        }
        mContentView = getChildAt(0);
        mInputPanel = getChildAt(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() != 2) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // 1. Setup initial constraints.
        int widthConstraints = getPaddingLeft() + getPaddingRight();
        int heightConstraints = getPaddingTop() + getPaddingBottom();
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // 2. Measure input panel
        measureChildWithMargins(
                mInputPanel,
                widthMeasureSpec,
                widthConstraints,
                heightMeasureSpec,
                heightConstraints
        );

        // 3. Update constraints
        heightConstraints += mInputPanel.getMeasuredHeight();

        // 2. Measure content view
        measureChildWithMargins(
                mContentView,
                widthMeasureSpec,
                widthConstraints,
                heightMeasureSpec,
                heightConstraints
        );
        // 4. Set measure dimension for view group.
        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mContentView == null || mInputPanel == null) {
            return;
        }
        int left = getPaddingLeft();
        int right = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int bottom = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int top = bottom - mInputPanel.getMeasuredHeight();
        mInputPanel.layout(left, top, right, bottom);

        bottom = top;
        top = getPaddingTop();
        mContentView.layout(left, top, right, bottom);
    }

    @Override
    protected void measureChildWithMargins(
            @NonNull View child,
            int parentWidthMeasureSpec,
            int widthUsed,
            int parentHeightMeasureSpec,
            int heightUsed) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();

        int childWidthMeasureSpec = getChildMeasureSpec(
                parentWidthMeasureSpec,
                widthUsed + lp.leftMargin + lp.rightMargin,
                lp.width);

        int childHeightMeasureSpec = getChildMeasureSpec(
                parentHeightMeasureSpec,
                heightUsed + lp.topMargin + lp.bottomMargin,
                lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN && mToaster != null && Toaster.isShown()) {
            mToaster.hideSoftKeyboard(mInputPanel);
            mToaster.hideView();
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MimosaLayout.LayoutParams(getContext(), attrs);
    }

}
