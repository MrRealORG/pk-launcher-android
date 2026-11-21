/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import git.artdeell.mojo.JVersionList;
import git.artdeell.mojo.R;
import git.artdeell.mojo.Tools;
import git.artdeell.mojo.fragments.SearchModFragment;
import git.artdeell.mojo.instances.Instance;
import git.artdeell.mojo.instances.Instances;

/**
 * Modrinth mod browser that installs mods (and their required dependencies) straight into
 * the selected instance's mods folder, matched to the instance's Minecraft version and
 * mod loader. Downloads are verified with Modrinth's SHA-1. Modpacks keep using
 * MojoLauncher's existing modpack installer (Modrinth .mrpack / CurseForge).
 */
public class ModsFragment extends PkFragment {
    public static final String TAG = "PK_MODS";
    private static final String API = "https://api.modrinth.com/v2";
    private static final String UA = "MrRealORG/pk-launcher-android (PK Launcher for Android)";

    private LinearLayout results;
    private TextView instanceLine;
    @Nullable private String mcVersion;
    @Nullable private String loader;
    @Nullable private File modsDir;

    @Override
    protected void build(LinearLayout col) {
        Context c = requireContext();
        col.addView(PkUi.title(c, theme, getString(R.string.pk_mods_title)));
        col.addView(PkUi.subtitle(c, theme, getString(R.string.pk_mods_subtitle)));

        instanceLine = PkUi.text(c, theme, "", 13, true);
        instanceLine.setPadding(0, 0, 0, PkUi.dp(c, 10));
        col.addView(instanceLine);

        LinearLayout searchRow = PkUi.row(c);
        EditText q = PkUi.input(c, theme, getString(R.string.pk_mods_search_hint), false);
        q.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        LinearLayout.LayoutParams qlp = PkUi.weight(1);
        qlp.rightMargin = PkUi.dp(c, 8);
        searchRow.addView(q, qlp);
        Button go = PkUi.primaryButton(c, theme, getString(R.string.pk_mods_search));
        searchRow.addView(go);
        col.addView(searchRow);

        Button modpacks = PkUi.ghostButton(c, theme, getString(R.string.pk_mods_modpacks));
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mlp.bottomMargin = PkUi.dp(c, 6);
        col.addView(modpacks, mlp);
        modpacks.setOnClickListener(v -> Tools.swapFragment(requireActivity(), SearchModFragment.class, SearchModFragment.TAG, null));

        results = new LinearLayout(c);
        results.setOrientation(LinearLayout.VERTICAL);
        col.addView(results);

        go.setOnClickListener(v -> search(q.getText().toString()));
        q.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                search(q.getText().toString());
                return true;
            }
            return false;
        });

        detectInstance();
        search("");
    }

    private void detectInstance() {
        Instance instance = Instances.loadSelectedInstance();
        if (instance == null || instance.versionId == null) {
            instanceLine.setText(R.string.pk_mods_no_instance);
            return;
        }
        String id = instance.versionId;
        String lower = id.toLowerCase(Locale.ROOT);
        if (lower.contains("neoforge")) loader = "neoforge";
        else if (lower.contains("forge")) loader = "forge";
        else if (lower.contains("quilt")) loader = "quilt";
        else if (lower.contains("fabric")) loader = "fabric";
        String base = null;
        try {
            JVersionList.Version v = Tools.getVersionInfo(id, true);
            if (v != null) base = v.inheritsFrom != null ? v.inheritsFrom : v.id;
        } catch (Throwable ignored) {
        }
        if (base == null || base.toLowerCase(Locale.ROOT).contains("loader")) {
            // Forge ids put the Minecraft version first ("1.20.1-forge-47.3.0"), Fabric/Quilt last.
            String regex = "forge".equals(loader) ? "(\\d+\\.\\d+(?:\\.\\d+)?)" : "(\\d+\\.\\d+(?:\\.\\d+)?)(?!.*\\d+\\.\\d+)";
            Matcher m = Pattern.compile(regex).matcher(id);
            if (m.find()) base = m.group(1);
        }
        if (Instance.VERSION_LATEST_RELEASE.equals(id) || Instance.VERSION_LATEST_SNAPSHOT.equals(id)) base = null;
        mcVersion = base;
        modsDir = new File(instance.getGameDirectory(), "mods");
        String name = instance.name != null ? instance.name : id;
        if (loader == null) {
            instanceLine.setText(getString(R.string.pk_mods_instance_vanilla, name));
            instanceLine.setTextColor(0xFFF5B544);
        } else {
            instanceLine.setText(getString(R.string.pk_mods_instance, name, loader, mcVersion == null ? "?" : mcVersion));
            instanceLine.setTextColor(theme.accent);
        }
    }

    private void search(String query) {
        results.removeAllViews();
        TextView loading = PkUi.text(requireContext(), theme, getString(R.string.pk_loading), 13, false);
        loading.setTextColor(theme.muted);
        results.addView(loading);
        final String fMc = mcVersion, fLoader = loader;
        background(() -> {
            try {
                StringBuilder facets = new StringBuilder("[[\"project_type:mod\"]");
                if (fMc != null) facets.append(",[\"versions:").append(fMc).append("\"]");
                if (fLoader != null) facets.append(",[\"categories:").append(fLoader).append("\"]");
                facets.append("]");
                String url = API + "/search?limit=25&index=" + (query.trim().isEmpty() ? "downloads" : "relevance")
                        + "&query=" + URLEncoder.encode(query.trim(), "UTF-8")
                        + "&facets=" + URLEncoder.encode(facets.toString(), "UTF-8");
                JSONArray hits = new JSONObject(get(url)).getJSONArray("hits");
                ui(() -> showResults(hits));
            } catch (Exception e) {
                ui(() -> {
                    results.removeAllViews();
                    TextView err = PkUi.text(requireContext(), theme, getString(R.string.pk_mods_error, e.getMessage()), 13, false);
                    err.setTextColor(0xFFFF8A8F);
                    results.addView(err);
                });
            }
        });
    }

    private void showResults(JSONArray hits) {
        results.removeAllViews();
        Context c = requireContext();
        if (hits.length() == 0) {
            TextView none = PkUi.text(c, theme, getString(R.string.pk_mods_none), 13, false);
            none.setTextColor(theme.muted);
            results.addView(none);
            return;
        }
        for (int i = 0; i < hits.length(); i++) {
            JSONObject h = hits.optJSONObject(i);
            if (h == null) continue;
            LinearLayout card = PkUi.card(c, theme);
            LinearLayout row = PkUi.row(c);
            ImageView icon = new ImageView(c);
            int is = PkUi.dp(c, 40);
            icon.setBackground(PkUi.rounded(theme.surface2, PkUi.dp(c, 8)));
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(is, is);
            ilp.rightMargin = PkUi.dp(c, 12);
            row.addView(icon, ilp);
            LinearLayout info = new LinearLayout(c);
            info.setOrientation(LinearLayout.VERTICAL);
            info.addView(PkUi.text(c, theme, h.optString("title"), 15, true));
            TextView by = PkUi.text(c, theme, getString(R.string.pk_mods_by, h.optString("author"), formatCount(h.optLong("downloads"))), 12, false);
            by.setTextColor(theme.muted);
            info.addView(by);
            row.addView(info, PkUi.weight(1));
            Button install = PkUi.primaryButton(c, theme, getString(R.string.pk_mods_install));
            row.addView(install);
            card.addView(row);
            TextView desc = PkUi.text(c, theme, h.optString("description"), 13, false);
            desc.setTextColor(theme.muted);
            desc.setPadding(0, PkUi.dp(c, 8), 0, 0);
            card.addView(desc);
            results.addView(card);
            PkUi.enterOne(card);

            String projectId = h.optString("project_id");
            String title = h.optString("title");
            install.setOnClickListener(v -> install(projectId, title, install));
            String iconUrl = h.optString("icon_url", "");
            if (!iconUrl.isEmpty()) loadIcon(iconUrl, icon);
        }
    }

    private void loadIcon(String url, ImageView into) {
        background(() -> {
            try {
                HttpURLConnection conn = open(url);
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inSampleSize = 2;
                Bitmap b;
                try (InputStream in = conn.getInputStream()) {
                    b = BitmapFactory.decodeStream(in, null, o);
                }
                if (b != null) ui(() -> into.setImageBitmap(b));
            } catch (Exception ignored) {
            }
        });
    }

    private void install(String projectId, String title, Button button) {
        Context c = requireContext();
        if (loader == null || mcVersion == null || modsDir == null) {
            Toast.makeText(c, R.string.pk_mods_need_loader, Toast.LENGTH_LONG).show();
            return;
        }
        button.setEnabled(false);
        button.setText(R.string.pk_mods_installing);
        final String fMc = mcVersion, fLoader = loader;
        final File fDir = modsDir;
        background(() -> {
            try {
                Set<String> done = new HashSet<>();
                int count = installRecursive(projectId, fMc, fLoader, fDir, done, 0);
                ui(() -> {
                    button.setText(R.string.pk_mods_installed);
                    Toast.makeText(c, getString(R.string.pk_mods_installed_toast, title, count), Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                ui(() -> {
                    button.setEnabled(true);
                    button.setText(R.string.pk_mods_install);
                    Toast.makeText(c, getString(R.string.pk_mods_error, e.getMessage()), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /** Install the newest compatible version of a project plus its required dependencies. Returns files written. */
    private int installRecursive(String projectId, String mc, String loader, File dir, Set<String> done, int depth) throws Exception {
        if (depth > 4 || !done.add(projectId)) return 0;
        String loaders = "[\"" + loader + "\"]";
        String versions = "[\"" + mc + "\"]";
        String url = API + "/project/" + URLEncoder.encode(projectId, "UTF-8") + "/version?loaders="
                + URLEncoder.encode(loaders, "UTF-8") + "&game_versions=" + URLEncoder.encode(versions, "UTF-8");
        JSONArray list = new JSONArray(get(url));
        if (list.length() == 0) {
            if (depth == 0) throw new IOException(getStringSafe(R.string.pk_mods_no_version));
            return 0;
        }
        JSONObject version = list.getJSONObject(0);
        JSONArray files = version.getJSONArray("files");
        JSONObject file = files.getJSONObject(0);
        for (int i = 0; i < files.length(); i++) {
            if (files.getJSONObject(i).optBoolean("primary")) { file = files.getJSONObject(i); break; }
        }
        String fileName = new File(file.getString("filename")).getName();
        if (!fileName.endsWith(".jar")) throw new IOException("Unexpected file " + fileName);
        String sha1 = file.optJSONObject("hashes") != null ? file.getJSONObject("hashes").optString("sha1", null) : null;
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Cannot create " + dir);
        File target = new File(dir, fileName);
        int written = 0;
        if (!target.exists() || (sha1 != null && !sha1.equalsIgnoreCase(sha1(target)))) {
            File tmp = new File(dir, fileName + ".part");
            download(file.getString("url"), tmp);
            if (sha1 != null && !sha1.equalsIgnoreCase(sha1(tmp))) {
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
                throw new IOException("Checksum mismatch for " + fileName);
            }
            if (!tmp.renameTo(target)) throw new IOException("Cannot write " + target);
            written = 1;
        }
        JSONArray deps = version.optJSONArray("dependencies");
        if (deps != null) {
            for (int i = 0; i < deps.length(); i++) {
                JSONObject d = deps.getJSONObject(i);
                if (!"required".equals(d.optString("dependency_type"))) continue;
                String depProject = d.optString("project_id", "");
                if (depProject.isEmpty() || "null".equals(depProject)) continue;
                written += installRecursive(depProject, mc, loader, dir, done, depth + 1);
            }
        }
        return written;
    }

    private String getStringSafe(int res) {
        Context c = getContext();
        return c != null ? c.getString(res) : "No compatible version";
    }

    private static HttpURLConnection open(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", UA);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(20000);
        return conn;
    }

    private static String get(String url) throws IOException {
        HttpURLConnection conn = open(url);
        conn.setRequestProperty("Accept", "application/json");
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) throw new IOException("HTTP " + code);
        return ServerPing.readAll(conn.getInputStream());
    }

    private static void download(String url, File out) throws IOException {
        if (!url.startsWith("https://")) throw new IOException("Refusing non-HTTPS download");
        HttpURLConnection conn = open(url);
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) throw new IOException("HTTP " + code);
        try (InputStream in = conn.getInputStream(); OutputStream os = new FileOutputStream(out)) {
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
        }
    }

    private static String sha1(File f) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        try (InputStream in = new java.io.FileInputStream(f)) {
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : md.digest()) sb.append(String.format(Locale.ROOT, "%02x", b));
        return sb.toString();
    }

    private static String formatCount(long n) {
        if (n >= 1_000_000) return String.format(Locale.ROOT, "%.1fM", n / 1_000_000f);
        if (n >= 1_000) return String.format(Locale.ROOT, "%.1fK", n / 1_000f);
        return String.valueOf(n);
    }
}
