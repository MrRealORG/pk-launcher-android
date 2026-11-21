/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;

import java.util.Calendar;

/**
 * PK theme packs, the same colour tokens as PK Launcher desktop and the PK companion app.
 * Applying one recolours the PK screens, the launcher background and the system bars.
 */
public final class PkTheme {
    public final String id;
    public final String name;
    public final String desc;
    public final String scene; // "none", "halloween", "forge", "snow", "lush"
    public final int bg, surface, surface2, text, muted, primary, accent;

    private PkTheme(String id, String name, String scene, String desc,
                    String bg, String surface, String surface2, String text, String muted, String primary, String accent) {
        this.id = id;
        this.name = name;
        this.scene = scene;
        this.desc = desc;
        this.bg = Color.parseColor(bg);
        this.surface = Color.parseColor(surface);
        this.surface2 = Color.parseColor(surface2);
        this.text = Color.parseColor(text);
        this.muted = Color.parseColor(muted);
        this.primary = Color.parseColor(primary);
        this.accent = Color.parseColor(accent);
    }

    public static final PkTheme[] ALL = {
            new PkTheme("pk-default", "PK Default", "none", "The signature PK look: soft graphite, PK red, calm and solid.",
                    "#1B1D21", "#212429", "#2A2E35", "#E8EAF0", "#9BA1AB", "#F2333F", "#38BDF8"),
            new PkTheme("pk-halloween", "Halloween Night", "halloween", "Bats crossing a big pixel moon and a flickering jack-o'-lantern.",
                    "#120A1A", "#1A1124", "#251932", "#F1EAF8", "#B3A6C6", "#FF8A1F", "#B98CFF"),
            new PkTheme("pk-rust", "Rust Edition", "forge", "Obsidian and copper engine mode from the Rust Edition look.",
                    "#141110", "#1C1816", "#26201D", "#F3ECE6", "#AFA39A", "#F0712C", "#F2B13B"),
            new PkTheme("pk-lush-caves", "Lush Caves", "lush", "Glow berries breathing in a mossy cave.",
                    "#0B110E", "#111915", "#18231D", "#E7F1EA", "#9DB3A5", "#F2A93B", "#6FD08C"),
            new PkTheme("pk-deep-ocean", "Deep Ocean", "none", "Deep blue water, calm and cool.",
                    "#06101A", "#0C1823", "#12212F", "#E4F1FA", "#93AFC4", "#33C6E8", "#7BE0C3"),
            new PkTheme("pk-neon-rain", "Neon Rain", "none", "A night city in the rain, neon signs humming.",
                    "#0A0B12", "#12131D", "#1A1C29", "#ECEBFA", "#A2A1BF", "#E04BFF", "#38E1FF"),
            new PkTheme("pk-pixel-dusk", "Pixel Dusk", "none", "A warm pixel sunset.",
                    "#0F0C14", "#17131D", "#211B29", "#F3EAF0", "#B5A4B3", "#FF7A59", "#FFC861"),
            new PkTheme("pk-aurora", "Aurora", "none", "Northern lights over a frozen ridge.",
                    "#081014", "#0F181D", "#162229", "#E5F3EF", "#94B2AA", "#3EE6A8", "#7C9CFF"),
            new PkTheme("pk-sakura", "Sakura Evening", "none", "Cherry blossom pink on a dark evening sky.",
                    "#140E13", "#1C141A", "#271C24", "#F6E9F0", "#BFA3B2", "#FF7EB3", "#FFC2DA"),
            new PkTheme("pk-taiga", "Taiga Snow", "snow", "Quiet snowfall over a spruce taiga at night.",
                    "#0B1014", "#12191F", "#1A232B", "#E8F0F6", "#9DADBA", "#7CC4FF", "#B8E1FF"),
    };

    private static final String PREFS = "pk_launcher";
    private static final String KEY = "theme";
    private static final String KEY_MOTION = "reduce_motion";

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static PkTheme byId(String id) {
        for (PkTheme t : ALL) if (t.id.equals(id)) return t;
        return ALL[0];
    }

    public static PkTheme current(Context c) {
        return byId(prefs(c).getString(KEY, ALL[0].id));
    }

    public static void set(Context c, PkTheme t) {
        prefs(c).edit().putString(KEY, t.id).apply();
    }

    /** Halloween season (Oct 15 - Nov 2): the app suggests the Halloween pack, never forces it. */
    public static boolean isHalloweenSeason() {
        Calendar cal = Calendar.getInstance();
        int m = cal.get(Calendar.MONTH), d = cal.get(Calendar.DAY_OF_MONTH);
        return (m == Calendar.OCTOBER && d >= 15) || (m == Calendar.NOVEMBER && d <= 2);
    }

    public static boolean reduceMotion(Context c) {
        if (prefs(c).getBoolean(KEY_MOTION, false)) return true;
        try {
            float scale = android.provider.Settings.Global.getFloat(c.getContentResolver(),
                    android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
            return scale == 0f;
        } catch (Exception e) {
            return false;
        }
    }

    public static void setReduceMotion(Context c, boolean v) {
        prefs(c).edit().putBoolean(KEY_MOTION, v).apply();
    }

    /** Recolour the window and system bars for this theme. */
    public static void applyToActivity(Activity a) {
        PkTheme t = current(a);
        Window w = a.getWindow();
        if (w == null) return;
        if (Build.VERSION.SDK_INT >= 21) {
            w.setStatusBarColor(t.surface);
            w.setNavigationBarColor(t.bg);
        }
        View root = a.findViewById(android.R.id.content);
        if (root != null) root.setBackgroundColor(t.bg);
    }

    /** primary with alpha (0-255). */
    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
