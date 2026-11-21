/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import git.artdeell.mojo.R;
import git.artdeell.mojo.Tools;

/** PK theme packs (PK Default, Halloween Night, Rust Edition, ...) with live previews. */
public class ThemesFragment extends PkFragment {
    public static final String TAG = "PK_THEMES";

    @Override
    protected void build(LinearLayout col) {
        Context c = requireContext();
        col.addView(PkUi.title(c, theme, getString(R.string.pk_themes_title)));
        col.addView(PkUi.subtitle(c, theme, getString(R.string.pk_themes_subtitle)));
        if (PkTheme.isHalloweenSeason() && !"pk-halloween".equals(theme.id)) {
            TextView tip = PkUi.text(c, theme, getString(R.string.pk_themes_halloween_tip), 13, true);
            tip.setTextColor(0xFFFF8A1F);
            tip.setPadding(0, 0, 0, PkUi.dp(c, 10));
            col.addView(tip);
        }

        for (PkTheme t : PkTheme.ALL) {
            LinearLayout card = PkUi.card(c, theme);
            boolean active = t.id.equals(theme.id);
            if (active) card.setBackground(PkUi.roundedStroke(theme.surface, theme.primary, PkUi.dp(c, 2), PkUi.dp(c, 14)));

            PkSceneView preview = new PkSceneView(c, t);
            preview.setBackground(PkUi.rounded(t.bg, PkUi.dp(c, 10)));
            preview.setClipToOutline(true);
            card.addView(preview, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, PkUi.dp(c, 72)));

            LinearLayout row = PkUi.row(c);
            row.setPadding(0, PkUi.dp(c, 10), 0, 0);
            LinearLayout swatches = PkUi.row(c);
            int[] colors = {t.bg, t.surface2, t.primary, t.accent};
            for (int col1 : colors) {
                View s = new View(c);
                s.setBackground(PkUi.roundedStroke(col1, 0x33FFFFFF, PkUi.dp(c, 1), PkUi.dp(c, 6)));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(PkUi.dp(c, 18), PkUi.dp(c, 18));
                lp.rightMargin = PkUi.dp(c, 4);
                swatches.addView(s, lp);
            }
            row.addView(swatches);
            LinearLayout info = new LinearLayout(c);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(PkUi.dp(c, 10), 0, 0, 0);
            info.addView(PkUi.text(c, theme, t.name + (active ? "  ✓" : ""), 15, true));
            TextView d = PkUi.text(c, theme, t.desc, 12, false);
            d.setTextColor(theme.muted);
            info.addView(d);
            row.addView(info, PkUi.weight(1));
            card.addView(row);
            PkUi.clickable(card, theme, PkUi.dp(c, 14));
            card.setOnClickListener(v -> apply(t));
            col.addView(card);
        }

        CheckBox motion = new CheckBox(c);
        motion.setText(R.string.pk_themes_reduce_motion);
        motion.setTextColor(theme.text);
        motion.setChecked(PkTheme.reduceMotion(c));
        motion.setOnCheckedChangeListener((b, checked) -> PkTheme.setReduceMotion(c, checked));
        col.addView(motion);
    }

    private void apply(PkTheme t) {
        FragmentActivity a = requireActivity();
        PkTheme.set(a, t);
        PkTheme.applyToActivity(a);
        // Re-open this screen so every card picks up the new colours.
        Tools.removeCurrentFragment(a);
        Tools.swapFragment(a, ThemesFragment.class, TAG, null);
    }
}
