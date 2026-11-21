/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Small helpers so the PK screens share one look (solid colours, soft radii, no gradients). */
public final class PkUi {
    private PkUi() {}

    public static int dp(Context c, float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics()));
    }

    public static GradientDrawable rounded(int color, float radiusPx) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radiusPx);
        return g;
    }

    public static GradientDrawable roundedStroke(int color, int stroke, int strokeWidth, float radiusPx) {
        GradientDrawable g = rounded(color, radiusPx);
        g.setStroke(strokeWidth, stroke);
        return g;
    }

    /** A scrolling page with a vertical column; returns {scroll, column}. */
    public static ScrollView page(Context c, PkTheme t, LinearLayout[] outColumn) {
        ScrollView sv = new ScrollView(c);
        sv.setBackgroundColor(t.bg);
        sv.setFillViewport(true);
        LinearLayout col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        int p = dp(c, 16);
        col.setPadding(p, p, p, dp(c, 32));
        sv.addView(col, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        outColumn[0] = col;
        return sv;
    }

    public static TextView title(Context c, PkTheme t, String s) {
        TextView tv = new TextView(c);
        tv.setText(s);
        tv.setTextColor(t.text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(0, 0, 0, dp(c, 4));
        return tv;
    }

    public static TextView subtitle(Context c, PkTheme t, String s) {
        TextView tv = text(c, t, s, 13, false);
        tv.setTextColor(t.muted);
        tv.setPadding(0, 0, 0, dp(c, 14));
        return tv;
    }

    public static TextView section(Context c, PkTheme t, String s) {
        TextView tv = text(c, t, s.toUpperCase(), 11, true);
        tv.setTextColor(t.muted);
        tv.setLetterSpacing(0.08f);
        tv.setPadding(dp(c, 2), dp(c, 18), 0, dp(c, 8));
        return tv;
    }

    public static TextView text(Context c, PkTheme t, String s, float sp, boolean bold) {
        TextView tv = new TextView(c);
        tv.setText(s);
        tv.setTextColor(t.text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        return tv;
    }

    public static LinearLayout card(Context c, PkTheme t) {
        LinearLayout card = new LinearLayout(c);
        card.setOrientation(LinearLayout.VERTICAL);
        int p = dp(c, 14);
        card.setPadding(p, p, p, p);
        card.setBackground(roundedStroke(t.surface, PkTheme.withAlpha(t.text, 14), dp(c, 1), dp(c, 14)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(c, 10);
        card.setLayoutParams(lp);
        return card;
    }

    public static void clickable(View v, PkTheme t, float radius) {
        android.graphics.drawable.Drawable base = v.getBackground();
        v.setBackground(new RippleDrawable(ColorStateList.valueOf(PkTheme.withAlpha(t.primary, 60)), base,
                rounded(0xFFFFFFFF, radius)));
        v.setClickable(true);
        v.setFocusable(true);
    }

    public static Button primaryButton(Context c, PkTheme t, String label) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(0xFFFFFFFF);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x40FFFFFF), rounded(t.primary, dp(c, 10)), null));
        b.setMinHeight(dp(c, 40));
        b.setMinimumHeight(dp(c, 40));
        b.setPadding(dp(c, 16), 0, dp(c, 16), 0);
        b.setStateListAnimator(null);
        return b;
    }

    public static Button ghostButton(Context c, PkTheme t, String label) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(t.text);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(PkTheme.withAlpha(t.primary, 60)),
                roundedStroke(t.surface2, PkTheme.withAlpha(t.text, 20), dp(c, 1), dp(c, 10)), null));
        b.setMinHeight(dp(c, 40));
        b.setMinimumHeight(dp(c, 40));
        b.setPadding(dp(c, 16), 0, dp(c, 16), 0);
        b.setStateListAnimator(null);
        return b;
    }

    public static EditText input(Context c, PkTheme t, String hint, boolean multiLine) {
        EditText e = new EditText(c);
        e.setHint(hint);
        e.setTextColor(t.text);
        e.setHintTextColor(t.muted);
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        int p = dp(c, 12);
        e.setPadding(p, p, p, p);
        e.setBackground(roundedStroke(t.surface2, PkTheme.withAlpha(t.text, 20), dp(c, 1), dp(c, 10)));
        if (multiLine) {
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            e.setMinLines(3);
            e.setGravity(Gravity.TOP | Gravity.START);
        } else {
            e.setSingleLine(true);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(c, 10);
        e.setLayoutParams(lp);
        return e;
    }

    public static LinearLayout row(Context c) {
        LinearLayout r = new LinearLayout(c);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        return r;
    }

    public static LinearLayout.LayoutParams weight(float w) {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, w);
    }

    /** Staggered fade/slide-in for the children of a column (skipped with reduced motion). */
    public static void enter(ViewGroup column) {
        if (PkTheme.reduceMotion(column.getContext())) return;
        int n = column.getChildCount();
        for (int i = 0; i < n; i++) {
            View v = column.getChildAt(i);
            v.setAlpha(0f);
            v.setTranslationY(dp(column.getContext(), 12));
            v.animate().alpha(1f).translationY(0f).setStartDelay(Math.min(i, 10) * 35L)
                    .setDuration(260).setInterpolator(new DecelerateInterpolator(1.6f)).start();
        }
    }

    public static void enterOne(View v) {
        if (PkTheme.reduceMotion(v.getContext())) return;
        v.setAlpha(0f);
        v.setTranslationY(dp(v.getContext(), 10));
        v.animate().alpha(1f).translationY(0f).setDuration(240).setInterpolator(new DecelerateInterpolator(1.6f)).start();
    }
}
