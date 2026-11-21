/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import git.artdeell.mojo.Tools;

/** Base for the PK screens: a themed scrolling column built in code. */
public abstract class PkFragment extends Fragment {
    protected PkTheme theme;
    protected LinearLayout column;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        theme = PkTheme.current(requireContext());
        LinearLayout[] col = new LinearLayout[1];
        ScrollView sv = PkUi.page(requireContext(), theme, col);
        column = col[0];
        build(column);
        PkUi.enter(column);
        return sv;
    }

    protected abstract void build(LinearLayout column);

    /**
     * PK network work (pings, Modrinth, news) runs on its own small pool so it never queues
     * behind MojoLauncher's download/launch tasks on the shared executor.
     */
    private static final java.util.concurrent.ExecutorService PK_EXECUTOR =
            java.util.concurrent.Executors.newFixedThreadPool(4);

    /** Run work off the UI thread. */
    protected void background(Runnable r) {
        PK_EXECUTOR.execute(r);
    }

    /** Post to the UI thread only while this screen is still attached. */
    protected void ui(Runnable r) {
        Tools.runOnUiThread(() -> {
            if (isAdded() && getView() != null) r.run();
        });
    }

    protected void openUrl(String url) {
        Activity a = getActivity();
        if (a != null) Tools.openURL(a, url);
    }
}
