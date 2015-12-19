package com.sonaive.toaster;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

/**
 * Created by liutao on 12/19/15.
 */
public class Toaster {
    private static final int ANIMATION_DURATION = 350;
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

    private static boolean isShown;

    private final ViewGroup mTargetParent;
    private final Context mContext;
    private final ToasterLayout mView;

    private Toaster(ViewGroup parent) {
        mTargetParent = parent;
        mContext = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        mView = (ToasterLayout) inflater.inflate(R.layout.layout_toaster, mTargetParent, false);
    }

    @NonNull
    public static Toaster make(@NonNull View view) {
        return new Toaster(findSuitableParent(view));
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                // We've found a CoordinatorLayout, use it
                return (ViewGroup) view;
            } else if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    // If we've hit the decor content view, then we didn't find a CoL in the
                    // hierarchy, so use it.
                    return (ViewGroup) view;
                } else {
                    // It's not the content view but we'll use it as our fallback
                    fallback = (ViewGroup) view;
                }
            }

            if (view != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
        return fallback;
    }

    public static boolean isShown() {
        return isShown;
    }

    public final void showView() {
        if (mView.getParent() == null) {
            mTargetParent.addView(mView);
        }
        isShown = true;

        if (ViewCompat.isLaidOut(mView)) {
            // If the view is already laid out, animate it now
            animateViewIn();
        } else {
            // Otherwise, add one of our layout change listeners and animate it in when laid out
            mView.setOnLayoutChangeListener(new ToasterLayout.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    animateViewIn();
                    mView.setOnLayoutChangeListener(null);
                }
            });
        }
    }

    private void animateViewIn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewCompat.setTranslationY(mView, mView.getHeight());
            ViewCompat.animate(mView)
                    .translationY(0f)
                    .setInterpolator(LINEAR_INTERPOLATOR)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                        }
                    }).start();
        } else {
            Animation anim = AnimationUtils.loadAnimation(mView.getContext(),
                    R.anim.toaster_in);
            anim.setInterpolator(LINEAR_INTERPOLATOR);
            anim.setDuration(ANIMATION_DURATION);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            mView.startAnimation(anim);
        }
    }

    private void animateViewOut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // It's wired that you should add status bar height for correction, or else there is ugly blank region left in the bottom.
            ViewCompat.animate(mView)
                    .translationY(mView.getHeight() + getStatusBarHeight())
                    .setInterpolator(LINEAR_INTERPOLATOR)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            onViewHidden();
                        }
                    }).start();
        } else {
            Animation anim = AnimationUtils.loadAnimation(mView.getContext(), R.anim.toaster_out);
            anim.setInterpolator(LINEAR_INTERPOLATOR);
            anim.setDuration(ANIMATION_DURATION);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    onViewHidden();
                }

                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            mView.startAnimation(anim);
        }
    }

    public final void hideView() {
        if (mView.getVisibility() != View.VISIBLE) {
            onViewHidden();
        } else {
            animateViewOut();
        }
        isShown = false;
    }

    private void onViewHidden() {
        // First remove the view from the parent (if attached)
        final ViewParent parent = mView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mView);
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static class ToasterLayout extends FrameLayout {

        private FrameLayout mEmoticonPanel;
        private FrameLayout mFeaturePanel;

        interface OnLayoutChangeListener {
            void onLayoutChange(View view, int left, int top, int right, int bottom);
        }

        private OnLayoutChangeListener mOnLayoutChangeListener;

        public ToasterLayout(Context context) {
            super(context);
        }

        public ToasterLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            // Now inflate our content. We need to do this manually rather than using an <include>
            // in the layout since older versions of the Android do not inflate includes with
            // the correct Context.
            LayoutInflater.from(context).inflate(R.layout.layout_toaster_include, this);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            mEmoticonPanel = (FrameLayout) findViewById(R.id.emoticon_panel);
            mFeaturePanel = (FrameLayout) findViewById(R.id.feature_panel);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (changed && mOnLayoutChangeListener != null) {
                mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }
        }

        void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
            mOnLayoutChangeListener = onLayoutChangeListener;
        }
    }

    public void hideSoftKeyboard(View view) {
        if (view != null && view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showSoftKeyboard(View view) {
        if (view != null && view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInputFromInputMethod(view.getWindowToken(), 0);
        }
    }
}
