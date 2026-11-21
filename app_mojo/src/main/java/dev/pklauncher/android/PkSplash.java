/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import git.artdeell.mojo.R;

/**
 * Animated PK splash + loading overlay shown over the launcher on a cold start:
 * the PK mark pops in on its dark tile, a bolt sweep runs over it, a thin loading bar
 * fills, then everything fades away. About 1.1 s; shorter with "Reduce motion".
 */
public final class PkSplash {
    private PkSplash() {}

    public static void show(Activity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) return;
        Context c = activity;
        PkTheme t = PkTheme.current(c);
        boolean reduce = PkTheme.reduceMotion(c);

        FrameLayout overlay = new FrameLayout(c);
        overlay.setBackgroundColor(t.bg);
        overlay.setClickable(true); // swallow taps while visible

        LinearLayout centre = new LinearLayout(c);
        centre.setOrientation(LinearLayout.VERTICAL);
        centre.setGravity(Gravity.CENTER_HORIZONTAL);

        FrameLayout tile = new FrameLayout(c);
        int ts = PkUi.dp(c, 112);
        tile.setBackground(PkUi.rounded(0xFF121418, PkUi.dp(c, 30)));
        tile.setElevation(PkUi.dp(c, 10));
        ImageView mark = new ImageView(c);
        mark.setImageResource(R.drawable.pk_mark);
        int ms = PkUi.dp(c, 80);
        FrameLayout.LayoutParams mlp = new FrameLayout.LayoutParams(ms, ms, Gravity.CENTER);
        tile.addView(mark, mlp);
        View sweep = new View(c);
        sweep.setBackgroundColor(0x33FFFFFF);
        sweep.setRotation(20f);
        tile.addView(sweep, new FrameLayout.LayoutParams(PkUi.dp(c, 18), ts * 2, Gravity.CENTER_VERTICAL));
        tile.setClipToOutline(true);
        centre.addView(tile, new LinearLayout.LayoutParams(ts, ts));

        TextView name = new TextView(c);
        name.setText(R.string.app_short_name);
        name.setTextColor(t.text);
        name.setTextSize(22);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setPadding(0, PkUi.dp(c, 18), 0, PkUi.dp(c, 2));
        centre.addView(name);
        TextView sub = new TextView(c);
        sub.setText(R.string.pk_splash_sub);
        sub.setTextColor(t.muted);
        sub.setTextSize(13);
        centre.addView(sub);

        FrameLayout track = new FrameLayout(c);
        track.setBackground(PkUi.rounded(t.surface2, PkUi.dp(c, 2)));
        View fill = new View(c);
        fill.setBackground(PkUi.rounded(t.primary, PkUi.dp(c, 2)));
        int tw = PkUi.dp(c, 140);
        track.addView(fill, new FrameLayout.LayoutParams(tw, ViewGroup.LayoutParams.MATCH_PARENT));
        fill.setPivotX(0);
        fill.setScaleX(0f);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(tw, PkUi.dp(c, 4));
        tlp.topMargin = PkUi.dp(c, 22);
        centre.addView(track, tlp);

        overlay.addView(centre, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        content.addView(overlay, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        long total = reduce ? 350 : 1100;
        if (!reduce) {
            tile.setScaleX(0.6f);
            tile.setScaleY(0.6f);
            tile.setAlpha(0f);
            tile.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(420).setInterpolator(new OvershootInterpolator(1.6f)).start();
            name.setAlpha(0f);
            name.setTranslationY(PkUi.dp(c, 8));
            name.animate().alpha(1f).translationY(0).setStartDelay(180).setDuration(320).setInterpolator(new DecelerateInterpolator()).start();
            sub.setAlpha(0f);
            sub.animate().alpha(1f).setStartDelay(280).setDuration(320).start();
            ObjectAnimator sw = ObjectAnimator.ofFloat(sweep, View.TRANSLATION_X, -ts, ts * 1.4f);
            sw.setStartDelay(350);
            sw.setDuration(520);
            sw.setInterpolator(new AccelerateInterpolator());
            sw.start();
        } else {
            sweep.setVisibility(View.GONE);
        }
        ValueAnimator bar = ValueAnimator.ofFloat(0f, 1f);
        bar.setDuration(total - (reduce ? 50 : 150));
        bar.setInterpolator(reduce ? new LinearInterpolator() : new DecelerateInterpolator(1.4f));
        bar.addUpdateListener(a -> fill.setScaleX((float) a.getAnimatedValue()));
        bar.start();

        overlay.postDelayed(() -> overlay.animate().alpha(0f).setDuration(reduce ? 120 : 260)
                .withEndAction(() -> {
                    ViewGroup p = (ViewGroup) overlay.getParent();
                    if (p != null) p.removeView(overlay);
                }).start(), total);
    }
}
