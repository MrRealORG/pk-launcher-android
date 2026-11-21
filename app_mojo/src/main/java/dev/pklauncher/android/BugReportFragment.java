/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import git.artdeell.mojo.BuildConfig;
import git.artdeell.mojo.R;

/**
 * Bug report that builds a prefilled GitHub "new issue" URL (same approach as the PK
 * companion app). Nothing is sent by the app: the user reviews and submits on GitHub.
 */
public class BugReportFragment extends PkFragment {
    public static final String TAG = "PK_BUG";
    static final String REPO = "MrRealORG/pk-launcher-android";
    private static final int MAX_URL = 7600; // GitHub rejects very long new-issue URLs

    private static final String[] AREAS = {"Launching the game", "Accounts / sign-in", "Servers / joining",
            "Mods / modpacks", "Controls / touch", "Performance / FPS", "Themes / UI", "Something else"};

    @Override
    protected void build(LinearLayout col) {
        Context c = requireContext();
        col.addView(PkUi.title(c, theme, getString(R.string.pk_bug_title)));
        col.addView(PkUi.subtitle(c, theme, getString(R.string.pk_bug_subtitle)));

        EditText title = PkUi.input(c, theme, getString(R.string.pk_bug_field_title), false);
        col.addView(title);
        Spinner area = new Spinner(c);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, AREAS);
        area.setAdapter(adapter);
        area.setBackground(PkUi.roundedStroke(theme.surface2, PkTheme.withAlpha(theme.text, 20), PkUi.dp(c, 1), PkUi.dp(c, 10)));
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, PkUi.dp(c, 46));
        alp.bottomMargin = PkUi.dp(c, 10);
        col.addView(area, alp);
        EditText what = PkUi.input(c, theme, getString(R.string.pk_bug_field_what), true);
        col.addView(what);
        EditText steps = PkUi.input(c, theme, getString(R.string.pk_bug_field_steps), true);
        col.addView(steps);
        EditText expected = PkUi.input(c, theme, getString(R.string.pk_bug_field_expected), true);
        col.addView(expected);

        TextView device = PkUi.text(c, theme, deviceInfo(), 12, false);
        device.setTextColor(theme.muted);
        device.setPadding(0, 0, 0, PkUi.dp(c, 12));
        col.addView(device);

        Button open = PkUi.primaryButton(c, theme, getString(R.string.pk_bug_open));
        col.addView(open, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, PkUi.dp(c, 46)));
        open.setOnClickListener(v -> openUrl(buildIssueUrl(
                title.getText().toString(), String.valueOf(area.getSelectedItem()),
                what.getText().toString(), steps.getText().toString(), expected.getText().toString())));
    }

    static String deviceInfo() {
        return "- App: PK Launcher for Android " + BuildConfig.VERSION_NAME + " (" + BuildConfig.APPLICATION_ID + ")\n"
                + "- Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n"
                + "- Device: " + Build.MANUFACTURER + " " + Build.MODEL + "\n"
                + "- ABI: " + (Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "?") + "\n"
                + "- SoC: " + (Build.VERSION.SDK_INT >= 31 ? Build.SOC_MODEL : Build.HARDWARE);
    }

    private static String section(String title, String v) {
        String t = v == null ? "" : v.trim();
        return "### " + title + "\n" + (t.isEmpty() ? "_No response_" : t) + "\n\n";
    }

    static String buildIssueUrl(String title, String area, String what, String steps, String expected) {
        String t = title == null ? "" : title.trim();
        if (t.isEmpty()) t = "Bug report";
        if (t.length() > 140) t = t.substring(0, 140);
        String body = section("Area", area) + section("What happened?", what) + section("Steps to reproduce", steps)
                + section("What did you expect?", expected) + "### Device\n" + deviceInfo() + "\n\n_Sent from PK Launcher for Android._";
        String url = make(t, body);
        while (url.length() > MAX_URL && body.length() > 200) {
            body = body.substring(0, (int) (body.length() * 0.85)) + "\n\n…(trimmed to fit GitHub's link limit)";
            url = make(t, body);
        }
        return url;
    }

    private static String make(String title, String body) {
        return Uri.parse("https://github.com/" + REPO + "/issues/new").buildUpon()
                .appendQueryParameter("title", "[Android] " + title)
                .appendQueryParameter("body", body)
                .appendQueryParameter("labels", "bug")
                .build().toString();
    }
}
