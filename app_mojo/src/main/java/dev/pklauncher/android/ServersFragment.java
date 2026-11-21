/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import git.artdeell.mojo.R;
import git.artdeell.mojo.Tools;
import git.artdeell.mojo.authenticator.accounts.Account;
import git.artdeell.mojo.authenticator.accounts.Accounts;
import git.artdeell.mojo.extra.ExtraConstants;
import git.artdeell.mojo.extra.ExtraCore;

/** Server list with a real ping from the phone and one-tap join (quickPlay). */
public class ServersFragment extends PkFragment {
    public static final String TAG = "PK_SERVERS";

    private static final String[][] FEATURED = {
            {"mc.hypixel.net", "Hypixel", "Minigames · SkyBlock"},
            {"donutsmp.net", "DonutSMP", "Survival · SMP"},
            {"play.pika-network.net", "PikaNetwork", "Bedwars · Skywars"},
            {"minemen.club", "Minemen Club", "PvP · Practice"},
            {"play.cubecraft.net", "CubeCraft", "Minigames"},
            {"2b2t.org", "2b2t", "Anarchy"},
    };

    private LinearLayout savedList;
    private LinearLayout featuredList;

    private SharedPreferences prefs() {
        return requireContext().getApplicationContext().getSharedPreferences("pk_launcher", Context.MODE_PRIVATE);
    }

    private List<String> saved() {
        String raw = prefs().getString("servers", "");
        List<String> out = new ArrayList<>();
        for (String s : raw.split("\n")) if (!s.trim().isEmpty()) out.add(s.trim());
        return out;
    }

    private void storeSaved(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s).append('\n');
        prefs().edit().putString("servers", sb.toString()).apply();
    }

    @Override
    protected void build(LinearLayout col) {
        Context c = requireContext();
        col.addView(PkUi.title(c, theme, getString(R.string.pk_servers_title)));
        col.addView(PkUi.subtitle(c, theme, getString(R.string.pk_servers_subtitle)));

        LinearLayout addRow = PkUi.row(c);
        EditText input = PkUi.input(c, theme, getString(R.string.pk_servers_add_hint), false);
        LinearLayout.LayoutParams ilp = PkUi.weight(1);
        ilp.rightMargin = PkUi.dp(c, 8);
        addRow.addView(input, ilp);
        Button add = PkUi.primaryButton(c, theme, getString(R.string.pk_servers_add));
        addRow.addView(add);
        col.addView(addRow);
        add.setOnClickListener(v -> {
            String a = ServerPing.normalizeAddress(input.getText().toString());
            if (a == null) {
                Toast.makeText(c, R.string.pk_servers_bad_address, Toast.LENGTH_SHORT).show();
                return;
            }
            List<String> list = saved();
            if (!list.contains(a)) list.add(0, a);
            storeSaved(list);
            input.setText("");
            renderSaved();
        });

        col.addView(PkUi.section(c, theme, getString(R.string.pk_servers_mine)));
        savedList = new LinearLayout(c);
        savedList.setOrientation(LinearLayout.VERTICAL);
        col.addView(savedList);

        col.addView(PkUi.section(c, theme, getString(R.string.pk_servers_featured)));
        featuredList = new LinearLayout(c);
        featuredList.setOrientation(LinearLayout.VERTICAL);
        col.addView(featuredList);

        TextView note = PkUi.text(c, theme, getString(R.string.pk_servers_note), 12, false);
        note.setTextColor(theme.muted);
        note.setPadding(0, PkUi.dp(c, 8), 0, 0);
        col.addView(note);

        renderSaved();
        for (String[] f : FEATURED) featuredList.addView(serverCard(f[0], f[1], f[2], false));
    }

    private void renderSaved() {
        savedList.removeAllViews();
        List<String> list = saved();
        if (list.isEmpty()) {
            TextView empty = PkUi.text(requireContext(), theme, getString(R.string.pk_servers_empty), 13, false);
            empty.setTextColor(theme.muted);
            savedList.addView(empty);
            return;
        }
        for (String a : list) savedList.addView(serverCard(a, a, null, true));
    }

    private View serverCard(String address, String name, String tags, boolean removable) {
        Context c = requireContext();
        LinearLayout card = PkUi.card(c, theme);
        LinearLayout top = PkUi.row(c);
        ImageView icon = new ImageView(c);
        int is = PkUi.dp(c, 44);
        icon.setBackground(PkUi.rounded(theme.surface2, PkUi.dp(c, 8)));
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(is, is);
        ilp.rightMargin = PkUi.dp(c, 12);
        top.addView(icon, ilp);

        LinearLayout info = new LinearLayout(c);
        info.setOrientation(LinearLayout.VERTICAL);
        info.addView(PkUi.text(c, theme, name, 15, true));
        TextView addr = PkUi.text(c, theme, tags != null ? address + "  ·  " + tags : address, 12, false);
        addr.setTextColor(theme.muted);
        info.addView(addr);
        TextView status = PkUi.text(c, theme, getString(R.string.pk_servers_pinging), 12, false);
        status.setTextColor(theme.muted);
        info.addView(status);
        top.addView(info, PkUi.weight(1));

        Button join = PkUi.primaryButton(c, theme, getString(R.string.pk_servers_join));
        top.addView(join);
        card.addView(top);

        TextView motd = PkUi.text(c, theme, "", 12, false);
        motd.setTypeface(Typeface.MONOSPACE);
        motd.setPadding(PkUi.dp(c, 10), PkUi.dp(c, 8), PkUi.dp(c, 10), PkUi.dp(c, 8));
        motd.setBackground(PkUi.rounded(0xFF121417, PkUi.dp(c, 8)));
        motd.setVisibility(View.GONE);
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mlp.topMargin = PkUi.dp(c, 10);
        card.addView(motd, mlp);

        if (removable) {
            card.setOnLongClickListener(v -> {
                new AlertDialog.Builder(c)
                        .setMessage(getString(R.string.pk_servers_remove_q, address))
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.global_delete, (d, w) -> {
                            List<String> list = saved();
                            list.remove(address);
                            storeSaved(list);
                            renderSaved();
                        }).show();
                return true;
            });
        }
        join.setOnClickListener(v -> joinServer(address));
        card.setOnClickListener(v -> refresh(address, icon, status, motd));
        refresh(address, icon, status, motd);
        return card;
    }

    private void refresh(String address, ImageView icon, TextView status, TextView motd) {
        status.setText(R.string.pk_servers_pinging);
        status.setTextColor(theme.muted);
        background(() -> {
            ServerPing.Result r = ServerPing.ping(address);
            ui(() -> {
                if (r.online) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(r.latencyMs >= 0 ? r.latencyMs + " ms" : getString(R.string.pk_servers_online));
                    if (r.playersOnline >= 0) sb.append("  ·  ").append(r.playersOnline).append('/').append(r.playersMax).append(' ').append(getString(R.string.pk_servers_players));
                    if (r.version != null) sb.append("  ·  ").append(stripCodes(r.version));
                    status.setText(sb.toString());
                    status.setTextColor(theme.accent);
                    if (r.favicon != null) icon.setImageBitmap(r.favicon);
                    if (r.motd != null && !r.motd.trim().isEmpty()) {
                        motd.setText(formatMotd(r.motd));
                        motd.setVisibility(View.VISIBLE);
                    }
                } else {
                    status.setText(R.string.pk_servers_offline);
                    status.setTextColor(0xFFFF8A8F);
                }
            });
        });
    }

    private void joinServer(String address) {
        Context c = requireContext();
        background(() -> {
            Account current = Accounts.getCurrent();
            int deny = OwnershipGate.checkCanPlay(current);
            ui(() -> {
                if (deny != 0) {
                    new AlertDialog.Builder(c)
                            .setTitle(R.string.pk_gate_title)
                            .setMessage(deny)
                            .setPositiveButton(R.string.pk_gate_sign_in, (d, w) -> {
                                Tools.backToMainMenu(requireActivity());
                                ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true);
                            })
                            .setNegativeButton(android.R.string.cancel, null).show();
                    return;
                }
                QuickJoin.request(address);
                Toast.makeText(c, getString(R.string.pk_servers_joining, address), Toast.LENGTH_SHORT).show();
                Tools.backToMainMenu(requireActivity());
                ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
            });
        });
    }

    static String stripCodes(String s) {
        return s.replaceAll("§.", "");
    }

    private static final int[] MC_COLORS = {0xFF000000, 0xFF0000AA, 0xFF00AA00, 0xFF00AAAA, 0xFFAA0000, 0xFFAA00AA, 0xFFFFAA00,
            0xFFAAAAAA, 0xFF555555, 0xFF5555FF, 0xFF55FF55, 0xFF55FFFF, 0xFFFF5555, 0xFFFF55FF, 0xFFFFFF55, 0xFFFFFFFF};

    /** Render "§" colour/format codes as spans (MOTD). */
    static CharSequence formatMotd(String raw) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int color = 0xFFAAAAAA;
        boolean bold = false, italic = false;
        String codes = "0123456789abcdef";
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch == '§' && i + 1 < raw.length()) {
                char code = Character.toLowerCase(raw.charAt(++i));
                int idx = codes.indexOf(code);
                if (idx >= 0) { color = MC_COLORS[idx]; bold = false; italic = false; }
                else if (code == 'l') bold = true;
                else if (code == 'o') italic = true;
                else if (code == 'r') { color = 0xFFAAAAAA; bold = false; italic = false; }
                continue;
            }
            int start = sb.length();
            sb.append(ch);
            sb.setSpan(new ForegroundColorSpan(color), start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (bold || italic) {
                sb.setSpan(new StyleSpan(bold && italic ? Typeface.BOLD_ITALIC : bold ? Typeface.BOLD : Typeface.ITALIC),
                        start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        // Trim each line's leading spaces used for centring on desktop
        return sb;
    }
}
