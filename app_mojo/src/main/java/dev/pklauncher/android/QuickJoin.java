/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import git.artdeell.mojo.Tools;
import git.artdeell.mojo.utils.DateUtils;

/**
 * One-tap server join. The launcher process writes the chosen address to a small file
 * (the game runs in a separate ":game" process, so SharedPreferences are not reliable),
 * and the game runner consumes it once when it builds the Minecraft arguments.
 *
 * Minecraft 1.20 (23w14a) and newer: --quickPlayMultiplayer host[:port]
 * Older versions: --server host --port N
 */
public final class QuickJoin {
    private static final String TAG = "PKQuickJoin";
    private static final long MAX_AGE_MS = 15 * 60 * 1000L;

    private QuickJoin() {}

    private static File file() {
        return new File(Tools.DIR_DATA, "pk_quickjoin.txt");
    }

    public static void request(String address) {
        try {
            String body = System.currentTimeMillis() + "\n" + address.trim();
            org.apache.commons.io.FileUtils.writeStringToFile(file(), body, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.w(TAG, "Could not store quick join request", e);
        }
    }

    public static void clear() {
        //noinspection ResultOfMethodCallIgnored
        file().delete();
    }

    /** Read and delete the pending request. */
    @Nullable
    public static String consume() {
        File f = file();
        if (!f.exists()) return null;
        try {
            String[] lines = org.apache.commons.io.FileUtils.readFileToString(f, StandardCharsets.UTF_8).split("\n");
            //noinspection ResultOfMethodCallIgnored
            f.delete();
            if (lines.length < 2) return null;
            long at = Long.parseLong(lines[0].trim());
            if (System.currentTimeMillis() - at > MAX_AGE_MS) return null;
            String address = lines[1].trim();
            return ServerPing.normalizeAddress(address);
        } catch (Exception e) {
            Log.w(TAG, "Bad quick join request", e);
            //noinspection ResultOfMethodCallIgnored
            f.delete();
            return null;
        }
    }

    /** Append the right join arguments for this Minecraft version. */
    public static void appendArgs(List<String> clientArgs, @Nullable Date releaseDate) {
        String address = consume();
        if (address == null) return;
        boolean modern = true;
        if (releaseDate != null) {
            // 23w14a (first version with --quickPlayMultiplayer) was released on 2023-04-05.
            modern = !DateUtils.dateBefore(releaseDate, 2023, 3, 5);
        }
        if (modern) {
            clientArgs.add("--quickPlayMultiplayer");
            clientArgs.add(address);
        } else {
            String host = address;
            String port = "25565";
            int idx = address.lastIndexOf(':');
            if (idx > 0 && !address.endsWith("]")) {
                host = address.substring(0, idx);
                port = address.substring(idx + 1);
            }
            clientArgs.add("--server");
            clientArgs.add(host);
            clientArgs.add("--port");
            clientArgs.add(port);
        }
        Log.i(TAG, "Quick joining " + address + (modern ? " (quickPlay)" : " (legacy --server)"));
    }
}
