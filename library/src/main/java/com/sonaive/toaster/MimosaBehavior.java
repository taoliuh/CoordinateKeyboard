package com.sonaive.toaster;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

public class MimosaBehavior extends CoordinatorLayout.Behavior<MimosaLayout> {

    public MimosaBehavior(Context context, AttributeSet attrs) {
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, MimosaLayout child, View dependency) {
        return dependency instanceof Toaster.ToasterLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, MimosaLayout child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }
}