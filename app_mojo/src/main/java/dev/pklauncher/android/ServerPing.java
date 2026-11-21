/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Real Minecraft Server List Ping from the phone (modern protocol: handshake, status
 * request, ping/pong). Latency is the measured ping/pong round trip from this device.
 * SRV records (_minecraft._tcp) are resolved over DNS-over-HTTPS because Android has no
 * SRV lookup in the standard library.
 */
public final class ServerPing {
    private static final int TIMEOUT_MS = 6000;
    private static final Pattern ADDRESS = Pattern.compile("^([a-z0-9.-]+|\\[[0-9a-f:]+\\])(?::(\\d{1,5}))?$");

    public static final class Result {
        public String address;
        public boolean online;
        public long latencyMs = -1;
        public String version;
        public int protocol = -1;
        public int playersOnline = -1;
        public int playersMax = -1;
        public String motd = "";
        @Nullable public Bitmap favicon;
        public String error;
    }

    private ServerPing() {}

    /** " Play.Hypixel.net:25565 " -> "play.hypixel.net". Returns null when invalid. */
    @Nullable
    public static String normalizeAddress(@Nullable String input) {
        if (input == null) return null;
        String a = input.trim().toLowerCase(Locale.ROOT);
        a = a.replaceFirst("^[a-z]+://", "").replaceFirst("/.*$", "");
        if (a.isEmpty()) return null;
        Matcher m = ADDRESS.matcher(a);
        if (!m.matches()) return null;
        String host = m.group(1);
        int port = -1;
        if (m.group(2) != null) {
            port = Integer.parseInt(m.group(2));
            if (port < 1 || port > 65535) return null;
        }
        if (!host.contains(".") && !host.startsWith("[") && !host.equals("localhost")) return null;
        return (port > 0 && port != 25565) ? host + ":" + port : host;
    }

    public static Result ping(String rawAddress) {
        Result r = new Result();
        String address = normalizeAddress(rawAddress);
        r.address = address == null ? rawAddress : address;
        if (address == null) {
            r.error = "Invalid address";
            return r;
        }
        String host = address;
        int port = 25565;
        boolean explicitPort = false;
        int idx = address.lastIndexOf(':');
        if (idx > 0 && !address.endsWith("]")) {
            host = address.substring(0, idx);
            port = Integer.parseInt(address.substring(idx + 1));
            explicitPort = true;
        }
        String connectHost = host;
        int connectPort = port;
        if (!explicitPort) {
            String[] srv = resolveSrv(host);
            if (srv != null) {
                connectHost = srv[0];
                connectPort = Integer.parseInt(srv[1]);
            }
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(connectHost, connectPort), TIMEOUT_MS);
            socket.setSoTimeout(TIMEOUT_MS);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // Handshake: id 0x00, protocol -1 (status), host, port, next state 1
            ByteArrayOutputStream hs = new ByteArrayOutputStream();
            DataOutputStream h = new DataOutputStream(hs);
            writeVarInt(h, 0x00);
            writeVarInt(h, -1);
            writeString(h, host);
            h.writeShort(port);
            writeVarInt(h, 1);
            writePacket(out, hs.toByteArray());
            // Status request
            writePacket(out, new byte[]{0x00});

            readVarInt(in); // packet length
            int id = readVarInt(in);
            if (id != 0x00) throw new IOException("Unexpected packet " + id);
            int len = readVarInt(in);
            if (len <= 0 || len > 2_000_000) throw new IOException("Bad status length");
            byte[] json = new byte[len];
            in.readFully(json);
            parseStatus(r, new String(json, StandardCharsets.UTF_8));

            // Ping / pong for real latency
            long t0 = System.nanoTime();
            ByteArrayOutputStream ps = new ByteArrayOutputStream();
            DataOutputStream p = new DataOutputStream(ps);
            writeVarInt(p, 0x01);
            p.writeLong(t0);
            writePacket(out, ps.toByteArray());
            try {
                readVarInt(in);
                int pongId = readVarInt(in);
                if (pongId == 0x01) {
                    in.readLong();
                    r.latencyMs = (System.nanoTime() - t0) / 1_000_000L;
                }
            } catch (IOException ignored) {
                // Some proxies close after status; keep the status result.
            }
            r.online = true;
        } catch (Exception e) {
            r.online = false;
            r.error = e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : "");
        }
        return r;
    }

    private static void parseStatus(Result r, String json) throws Exception {
        JSONObject o = new JSONObject(json);
        JSONObject v = o.optJSONObject("version");
        if (v != null) {
            r.version = v.optString("name", null);
            r.protocol = v.optInt("protocol", -1);
        }
        JSONObject pl = o.optJSONObject("players");
        if (pl != null) {
            r.playersOnline = pl.optInt("online", -1);
            r.playersMax = pl.optInt("max", -1);
        }
        Object desc = o.opt("description");
        r.motd = flattenChat(desc);
        String fav = o.optString("favicon", "");
        if (fav.startsWith("data:image/png;base64,")) {
            try {
                byte[] png = Base64.decode(fav.substring("data:image/png;base64,".length()).replace("\n", ""), Base64.DEFAULT);
                r.favicon = BitmapFactory.decodeByteArray(png, 0, png.length);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    /** Flatten a chat component (string or JSON) into legacy "§" formatted text. */
    static String flattenChat(Object c) {
        if (c == null) return "";
        if (c instanceof String) return (String) c;
        StringBuilder sb = new StringBuilder();
        if (c instanceof JSONObject) {
            JSONObject o = (JSONObject) c;
            String color = o.optString("color", "");
            String code = colorCode(color);
            if (code != null) sb.append('§').append(code);
            if (o.optBoolean("bold")) sb.append("§l");
            if (o.optBoolean("italic")) sb.append("§o");
            sb.append(o.optString("text", ""));
            JSONArray extra = o.optJSONArray("extra");
            if (extra != null) for (int i = 0; i < extra.length(); i++) sb.append(flattenChat(extra.opt(i)));
        } else if (c instanceof JSONArray) {
            JSONArray a = (JSONArray) c;
            for (int i = 0; i < a.length(); i++) sb.append(flattenChat(a.opt(i)));
        }
        return sb.toString();
    }

    @Nullable
    private static String colorCode(String name) {
        String[] names = {"black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray",
                "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"};
        String codes = "0123456789abcdef";
        for (int i = 0; i < names.length; i++) if (names[i].equals(name)) return String.valueOf(codes.charAt(i));
        return null;
    }

    /** @return {target, port} or null. */
    @Nullable
    private static String[] resolveSrv(String host) {
        if (host.startsWith("[") || host.matches("^[0-9.]+$")) return null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://dns.google/resolve?type=SRV&name=" + URLEncoder.encode("_minecraft._tcp." + host, "UTF-8"));
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setRequestProperty("Accept", "application/dns-json");
            if (conn.getResponseCode() != 200) return null;
            String body = readAll(conn.getInputStream());
            JSONArray answers = new JSONObject(body).optJSONArray("Answer");
            if (answers == null) return null;
            for (int i = 0; i < answers.length(); i++) {
                JSONObject a = answers.getJSONObject(i);
                if (a.optInt("type") != 33) continue;
                String[] parts = a.optString("data", "").trim().split("\\s+");
                if (parts.length < 4) continue;
                String target = parts[3].endsWith(".") ? parts[3].substring(0, parts[3].length() - 1) : parts[3];
                return new String[]{target, parts[2]};
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
        in.close();
        return new String(bo.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void writePacket(DataOutputStream out, byte[] data) throws IOException {
        writeVarInt(out, data.length);
        out.write(data);
        out.flush();
    }

    private static void writeString(DataOutputStream out, String s) throws IOException {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, b.length);
        out.write(b);
    }

    private static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.writeByte(value);
                return;
            }
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int value = 0, position = 0;
        while (true) {
            byte b = in.readByte();
            value |= (b & 0x7F) << position;
            if ((b & 0x80) == 0) return value;
            position += 7;
            if (position >= 32) throw new IOException("VarInt too big");
        }
    }
}
