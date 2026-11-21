/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import git.artdeell.mojo.R;

/**
 * Rust Edition (Horizon) status. Horizon (playhorizon.dev, LGPL-3.0-or-later, Horizon Team)
 * publishes desktop builds only (Windows, macOS, Linux). This screen says so honestly and
 * links to the roadmap instead of pretending to launch it.
 */
public class HorizonFragment extends PkFragment {
    public static final String TAG = "PK_HORIZON";

    @Override
    protected void build(LinearLayout col) {
        Context c = requireContext();
        PkSceneView banner = new PkSceneView(c, PkTheme.byId("pk-rust"));
        banner.setBackground(PkUi.rounded(0xFF141110, PkUi.dp(c, 14)));
        banner.setClipToOutline(true);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, PkUi.dp(c, 96));
        blp.bottomMargin = PkUi.dp(c, 14);
        col.addView(banner, blp);
        col.addView(PkUi.title(c, theme, getString(R.string.pk_horizon_title)));
        TextView body = PkUi.text(c, theme, getString(R.string.pk_horizon_body), 14, false);
        body.setTextColor(theme.muted);
        body.setPadding(0, 0, 0, PkUi.dp(c, 16));
        col.addView(body);
        Button roadmap = PkUi.ghostButton(c, theme, getString(R.string.pk_horizon_roadmap));
        col.addView(roadmap, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, PkUi.dp(c, 46)));
        roadmap.setOnClickListener(v -> openUrl("https://github.com/horizon-rust/horizon/blob/main/ROADMAP.md"));
    }
}
