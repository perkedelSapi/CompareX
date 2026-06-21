package com.al.comparex.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages shimmer animation across all skeleton Views.
 *
 * Usage:
 *   SkeletonManager sm = new SkeletonManager(rootView);
 *   sm.start();   // begin shimmer
 *   sm.stop();    // stop and restore alpha
 *
 * Finds every View with tag="skeleton" and applies a repeating
 * translateX + alpha pulse animation to simulate a shimmer sweep.
 */
public class SkeletonManager {

    private static final long DURATION_MS    = 1200L;
    private static final long STAGGER_MS     = 80L;
    private static final float ALPHA_DIM     = 0.45f;
    private static final float ALPHA_BRIGHT  = 1.0f;

    private final List<View> skeletonViews = new ArrayList<>();
    private final List<Animator> animators  = new ArrayList<>();
    private boolean running = false;

    public SkeletonManager(View root) {
        collectSkeletonViews(root, skeletonViews);
    }

    /** Recursively collect Views tagged "skeleton". */
    private static void collectSkeletonViews(View v, List<View> out) {
        if ("skeleton".equals(v.getTag())) out.add(v);
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++)
                collectSkeletonViews(vg.getChildAt(i), out);
        }
    }

    /** Start shimmer. Safe to call multiple times — won't double-start. */
    public void start() {
        if (running) return;
        running = true;
        animators.clear();

        long stagger = 0;
        for (View v : skeletonViews) {
            v.setAlpha(ALPHA_DIM);

            // Alpha pulse: dim → bright → dim
            ValueAnimator alpha = ValueAnimator.ofFloat(ALPHA_DIM, ALPHA_BRIGHT, ALPHA_DIM);
            alpha.setDuration(DURATION_MS);
            alpha.setStartDelay(stagger);
            alpha.setRepeatCount(ValueAnimator.INFINITE);
            alpha.setRepeatMode(ValueAnimator.RESTART);
            alpha.setInterpolator(new LinearInterpolator());
            alpha.addUpdateListener(anim -> v.setAlpha((float) anim.getAnimatedValue()));

            alpha.start();
            animators.add(alpha);
            stagger = (stagger + STAGGER_MS) % (DURATION_MS / 2);
        }
    }

    /** Stop shimmer and restore alpha to 1. */
    public void stop() {
        running = false;
        for (Animator a : animators) a.cancel();
        animators.clear();
        for (View v : skeletonViews) v.setAlpha(1f);
    }

    public boolean isRunning() { return running; }
}
