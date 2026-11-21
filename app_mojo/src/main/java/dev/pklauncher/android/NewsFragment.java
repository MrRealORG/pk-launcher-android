/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import git.artdeell.mojo.R;

/**
 * PK news: PK Launcher releases (public update channel MrRealORG/pk-updates) and
 * Minecraft: Java Edition patch notes (Mojang's launcher content feed), newest first,
 * cached for offline viewing. Same sources as the PK companion app.
 */
public class NewsFragment extends PkFragment {
    public static final String TAG = "PK_NEWS";
    private static final String PK_RELEASES = "https://api.github.com/repos/MrRealORG/pk-updates/releases?per_page=10";
    private static final String MC_NOTES = "https://launchercontent.mojang.com/v2/javaPatchNotes.json";

    private LinearLayout list;

    static final class Item {
        String tag, title, date, summary, url;
    }

    @Override
    protected void build(LinearLayout col) {
        Context c = requireContext();
        col.addView(PkUi.title(c, theme, getString(R.string.pk_news_title)));
        col.addView(PkUi.subtitle(c, theme, getString(R.string.pk_news_subtitle)));
        list = new LinearLayout(c);
        list.setOrientation(LinearLayout.VERTICAL);
        col.addView(list);
        TextView loading = PkUi.text(c, theme, getString(R.string.pk_loading), 13, false);
        loading.setTextColor(theme.muted);
        list.addView(loading);
        load();
    }

    private SharedPreferences cache() {
        return requireContext().getApplicationContext().getSharedPreferences("pk_launcher", Context.MODE_PRIVATE);
    }

    private void load() {
        final SharedPreferences prefs = cache();
        background(() -> {
            List<Item> items = new ArrayList<>();
            boolean anyOk = false;
            try {
                String body = get(PK_RELEASES, "application/vnd.github+json");
                prefs.edit().putString("news_pk", body).apply();
                parsePk(body, items);
                anyOk = true;
            } catch (Exception e) {
                try { parsePk(prefs.getString("news_pk", "[]"), items); } catch (Exception ignored) {}
            }
            try {
                String body = get(MC_NOTES, "application/json");
                // keep the cache small: only the entries list head
                JSONObject o = new JSONObject(body);
                JSONArray entries = o.getJSONArray("entries");
                JSONArray head = new JSONArray();
                for (int i = 0; i < Math.min(12, entries.length()); i++) head.put(entries.get(i));
                prefs.edit().putString("news_mc", head.toString()).apply();
                parseMc(head.toString(), items);
                anyOk = true;
            } catch (Exception e) {
                try { parseMc(prefs.getString("news_mc", "[]"), items); } catch (Exception ignored) {}
            }
            Collections.sort(items, (a, b) -> String.valueOf(b.date).compareTo(String.valueOf(a.date)));
            final boolean offline = !anyOk;
            ui(() -> render(items, offline));
        });
    }

    private static void parsePk(String body, List<Item> out) throws Exception {
        JSONArray arr = new JSONArray(body);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject r = arr.getJSONObject(i);
            Item it = new Item();
            it.tag = r.optBoolean("prerelease") ? "PK BETA" : "PK UPDATE";
            it.title = r.optString("name", r.optString("tag_name"));
            if (it.title.isEmpty()) it.title = r.optString("tag_name");
            it.date = r.optString("published_at", "");
            it.summary = firstLine(r.optString("body", ""));
            it.url = r.optString("html_url");
            out.add(it);
        }
    }

    private static void parseMc(String body, List<Item> out) throws Exception {
        JSONArray arr = new JSONArray(body);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject e = arr.getJSONObject(i);
            Item it = new Item();
            it.tag = "snapshot".equals(e.optString("type")) ? "SNAPSHOT" : "MINECRAFT";
            it.title = e.optString("title");
            it.date = e.optString("date", "");
            it.summary = e.optString("shortText", "");
            String v = e.optString("version", it.title);
            it.url = "https://minecraft.wiki/?search=" + URLEncoder.encode("Java Edition " + v, "UTF-8");
            out.add(it);
        }
    }

    private static String firstLine(String md) {
        for (String l : md.split("\n")) {
            String s = l.replaceAll("^[#>*\\-\\s]+", "").trim();
            if (s.length() > 3) return s;
        }
        return "";
    }

    private void render(List<Item> items, boolean offline) {
        Context c = requireContext();
        list.removeAllViews();
        if (offline) {
            TextView o = PkUi.text(c, theme, getString(R.string.pk_news_offline), 12, false);
            o.setTextColor(0xFFF5B544);
            o.setPadding(0, 0, 0, PkUi.dp(c, 8));
            list.addView(o);
        }
        if (items.isEmpty()) {
            TextView e = PkUi.text(c, theme, getString(R.string.pk_news_empty), 13, false);
            e.setTextColor(theme.muted);
            list.addView(e);
            return;
        }
        for (Item it : items) {
            LinearLayout card = PkUi.card(c, theme);
            TextView tag = PkUi.text(c, theme, it.tag + (it.date.length() >= 10 ? "  ·  " + it.date.substring(0, 10) : ""), 11, true);
            tag.setTextColor(it.tag.startsWith("PK") ? theme.primary : theme.accent);
            tag.setLetterSpacing(0.06f);
            card.addView(tag);
            TextView t = PkUi.text(c, theme, it.title, 16, true);
            t.setPadding(0, PkUi.dp(c, 4), 0, PkUi.dp(c, 4));
            card.addView(t);
            if (it.summary != null && !it.summary.isEmpty()) {
                TextView s = PkUi.text(c, theme, it.summary, 13, false);
                s.setTextColor(theme.muted);
                card.addView(s);
            }
            PkUi.clickable(card, theme, PkUi.dp(c, 14));
            final String url = it.url;
            card.setOnClickListener(v -> openUrl(url));
            list.addView(card);
            PkUi.enterOne(card);
        }
    }

    private static String get(String url, String accept) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Accept", accept);
        conn.setRequestProperty("User-Agent", "MrRealORG/pk-launcher-android");
        conn.setConnectTimeout(9000);
        conn.setReadTimeout(12000);
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) throw new Exception("HTTP " + code);
        return ServerPing.readAll(conn.getInputStream());
    }
}
