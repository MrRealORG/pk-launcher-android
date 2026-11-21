/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import git.artdeell.mojo.BuildConfig;
import git.artdeell.mojo.R;

/** About PK Launcher for Android, with the credits and licences the LGPL requires us to keep. */
public class AboutFragment extends PkFragment {
    public static final String TAG = "PK_ABOUT";

    @Override
    protected void build(LinearLayout col) {
        Context c = requireContext();
        LinearLayout head = PkUi.row(c);
        ImageView mark = new ImageView(c);
        mark.setImageResource(R.drawable.pk_mark);
        head.addView(mark, new LinearLayout.LayoutParams(PkUi.dp(c, 56), PkUi.dp(c, 56)));
        LinearLayout titles = new LinearLayout(c);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(PkUi.dp(c, 12), 0, 0, 0);
        titles.addView(PkUi.title(c, theme, getString(R.string.app_short_name)));
        TextView ver = PkUi.text(c, theme, getString(R.string.pk_about_version, BuildConfig.VERSION_NAME), 12, false);
        ver.setTextColor(theme.muted);
        titles.addView(ver);
        head.addView(titles);
        head.setPadding(0, 0, 0, PkUi.dp(c, 14));
        col.addView(head);

        LinearLayout credit = PkUi.card(c, theme);
        credit.setBackground(PkUi.roundedStroke(theme.surface, theme.primary, PkUi.dp(c, 1), PkUi.dp(c, 14)));
        credit.addView(PkUi.text(c, theme, getString(R.string.pk_about_based_on), 15, true));
        TextView cb = PkUi.text(c, theme, getString(R.string.pk_about_based_on_body), 13, false);
        cb.setTextColor(theme.muted);
        cb.setPadding(0, PkUi.dp(c, 6), 0, 0);
        credit.addView(cb);
        col.addView(credit);

        col.addView(PkUi.section(c, theme, getString(R.string.pk_about_links)));
        link(col, "PK Launcher for Android (source code)", "https://github.com/MrRealORG/pk-launcher-android");
        link(col, "MojoLauncher", "https://github.com/MojoLauncher/MojoLauncher");
        link(col, "PojavLauncher", "https://github.com/PojavLauncherTeam/PojavLauncher");
        link(col, "GNU LGPL v3.0", "https://www.gnu.org/licenses/lgpl-3.0.html");

        col.addView(PkUi.section(c, theme, getString(R.string.pk_about_third_party)));
        LinearLayout lic = PkUi.card(c, theme);
        TextView lt = PkUi.text(c, theme, thirdPartyList(c), 12, false);
        lt.setTextColor(theme.muted);
        lic.addView(lt);
        col.addView(lic);

        TextView legal = PkUi.text(c, theme, getString(R.string.pk_about_legal), 12, false);
        legal.setTextColor(theme.muted);
        legal.setGravity(Gravity.START);
        legal.setPadding(0, PkUi.dp(c, 12), 0, 0);
        col.addView(legal);
    }

    private void link(LinearLayout col, String label, String url) {
        Context c = requireContext();
        LinearLayout card = PkUi.card(c, theme);
        card.addView(PkUi.text(c, theme, label, 14, true));
        TextView u = PkUi.text(c, theme, url, 12, false);
        u.setTextColor(theme.accent);
        card.addView(u);
        PkUi.clickable(card, theme, PkUi.dp(c, 14));
        card.setOnClickListener(v -> openUrl(url));
        col.addView(card);
    }

    private static String thirdPartyList(Context c) {
        StringBuilder sb = new StringBuilder();
        sb.append("Full licence texts ship inside the app (assets/licenses) and in the source repository.\n\n");
        try {
            String[] files = c.getAssets().list("licenses");
            if (files != null) for (String f : files) sb.append("• ").append(f.replace("LICENSE-", "")).append('\n');
        } catch (IOException ignored) {
        }
        sb.append("• Noto Sans font (SIL Open Font License)\n");
        sb.append("• authlib-injector, Caciocavallo, LWJGL, OpenJDK runtimes, GLFW/SDL bindings and other components credited in README.md");
        return sb.toString();
    }
}
