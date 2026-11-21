/*
 * PK Launcher for Android - LGPL-3.0 (see LICENSE). Fork of MojoLauncher / PojavLauncher.
 */
package dev.pklauncher.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.view.View;

import java.util.Random;

/**
 * Lightweight animated banner behind the PK home header. Original pixel art drawn in code
 * (no third-party art): Halloween (pixel moon, bats, jack-o'-lantern), Rust Edition forge
 * sparks, Taiga snow and Lush Caves glow berries. Runs at ~30 fps only while visible and
 * draws a still frame when "Reduce motion" is on.
 */
public class PkSceneView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final float[] seeds = new float[24];
    private PkTheme theme;
    private final boolean still;

    public PkSceneView(Context c, PkTheme theme) {
        super(c);
        this.theme = theme;
        this.still = PkTheme.reduceMotion(c);
        Random r = new Random(7);
        for (int i = 0; i < seeds.length; i++) seeds[i] = r.nextFloat();
    }

    public void setTheme(PkTheme t) {
        theme = t;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        float time = still ? 2.0f : (SystemClock.uptimeMillis() % 600000L) / 1000f;
        float px = Math.max(2f, h / 40f); // pixel size
        switch (theme.scene) {
            case "halloween": drawHalloween(canvas, w, h, time, px); break;
            case "forge": drawSparks(canvas, w, h, time, px, theme.primary); break;
            case "snow": drawSnow(canvas, w, h, time, px); break;
            case "lush": drawBerries(canvas, w, h, time, px); break;
            default: drawBlocks(canvas, w, h, time, px); break;
        }
        if (!still && isShown()) postInvalidateDelayed(33);
    }

    private void drawBlocks(Canvas c, float w, float h, float t, float px) {
        // Quiet floating squares in the theme's primary colour.
        for (int i = 0; i < 10; i++) {
            float s = px * (2 + (int) (seeds[i] * 4));
            float x = (seeds[i + 10] * w + t * 6 * (0.3f + seeds[i])) % (w + s) - s;
            float y = h * (0.15f + 0.7f * seeds[(i + 3) % seeds.length]) + (float) Math.sin(t + i) * px * 2;
            paint.setColor(PkTheme.withAlpha(i % 3 == 0 ? theme.accent : theme.primary, 40 + (int) (seeds[i] * 50)));
            c.drawRect(x, y, x + s, y + s, paint);
        }
    }

    private void drawHalloween(Canvas c, float w, float h, float t, float px) {
        // Pixel moon
        float mr = h * 0.32f, mx = w - mr * 1.6f, my = h * 0.42f;
        paint.setColor(0x14F7E7B4);
        c.drawCircle(mx, my, mr * 1.35f, paint);
        paint.setColor(0xFFF7E7B4);
        float step = px * 2;
        for (float y = -mr; y <= mr; y += step) {
            for (float x = -mr; x <= mr; x += step) {
                if (x * x + y * y <= mr * mr) c.drawRect(mx + x, my + y, mx + x + step, my + y + step, paint);
            }
        }
        paint.setColor(0xFFE2CF94);
        c.drawRect(mx - mr * 0.4f, my - mr * 0.2f, mx - mr * 0.4f + step * 2, my - mr * 0.2f + step * 2, paint);
        c.drawRect(mx + mr * 0.2f, my + mr * 0.3f, mx + mr * 0.2f + step * 3, my + mr * 0.3f + step * 2, paint);
        // Bats
        paint.setColor(0xFF0A0510);
        for (int i = 0; i < 4; i++) {
            float speed = 18 + seeds[i] * 22;
            float bx = ((seeds[i + 4] * w) + t * speed) % (w + 40) - 20;
            float by = h * (0.2f + 0.5f * seeds[i + 8]) + (float) Math.sin(t * 2 + i) * px * 3;
            float flap = (float) Math.sin(t * 14 + i * 2) * px * 2;
            float s = px * 4;
            path.reset();
            path.moveTo(bx - s * 2, by - flap);
            path.lineTo(bx - s * 0.6f, by + s * 0.6f);
            path.lineTo(bx, by);
            path.lineTo(bx + s * 0.6f, by + s * 0.6f);
            path.lineTo(bx + s * 2, by - flap);
            path.lineTo(bx + s, by + s * 0.9f);
            path.lineTo(bx, by + s * 0.5f);
            path.lineTo(bx - s, by + s * 0.9f);
            path.close();
            c.drawPath(path, paint);
        }
        // Jack-o'-lantern with a flickering glow
        float flicker = still ? 1f : 0.75f + 0.25f * (float) Math.abs(Math.sin(t * 7) * Math.cos(t * 3));
        float pw = px * 11, ph = px * 9, pxl = px * 3, py = h - ph - px * 2;
        paint.setColor(PkTheme.withAlpha(0xFF8A1F, (int) (70 * flicker)));
        c.drawCircle(pxl + pw / 2, py + ph / 2, pw * 0.95f, paint);
        paint.setColor(0xFFFF8A1F);
        c.drawRect(pxl, py, pxl + pw, py + ph, paint);
        paint.setColor(0xFF3B7A2A);
        c.drawRect(pxl + pw / 2 - px, py - px * 2, pxl + pw / 2 + px, py, paint);
        paint.setColor(PkTheme.withAlpha(0xFFE27A, (int) (255 * flicker)));
        c.drawRect(pxl + px * 2, py + px * 2, pxl + px * 4, py + px * 4, paint);
        c.drawRect(pxl + px * 7, py + px * 2, pxl + px * 9, py + px * 4, paint);
        c.drawRect(pxl + px * 3, py + px * 6, pxl + px * 8, py + px * 7, paint);
        // Fog
        paint.setColor(0x10FFFFFF);
        float fx = (t * 10) % w;
        c.drawRect(fx - w, h - px * 4, fx + w * 0.3f, h, paint);
    }

    private void drawSparks(Canvas c, float w, float h, float t, float px, int color) {
        for (int i = 0; i < 18; i++) {
            float life = 3.2f;
            float tt = (t + seeds[i] * life) % life;
            float x = seeds[(i + 5) % seeds.length] * w + (float) Math.sin(tt * 3 + i) * px * 2;
            float y = h - (tt / life) * h * 1.1f;
            int a = (int) (255 * (1 - tt / life));
            paint.setColor(PkTheme.withAlpha(i % 4 == 0 ? theme.accent : color, Math.max(0, a)));
            c.drawRect(x, y, x + px * 1.5f, y + px * 1.5f, paint);
        }
    }

    private void drawSnow(Canvas c, float w, float h, float t, float px) {
        paint.setColor(0xCCFFFFFF);
        for (int i = 0; i < 24; i++) {
            float speed = 10 + seeds[i] * 18;
            float x = (seeds[(i + 7) % seeds.length] * w + (float) Math.sin(t + i) * px * 3);
            float y = (seeds[i] * h + t * speed) % (h + px * 2) - px * 2;
            float s = px * (1 + (i % 3) * 0.5f);
            c.drawRect(x, y, x + s, y + s, paint);
        }
        // Spruce silhouettes
        paint.setColor(PkTheme.withAlpha(0x0E1A14, 255));
        for (int i = 0; i < 6; i++) {
            float bx = w * (0.05f + i * 0.17f), th = h * (0.35f + 0.2f * seeds[i]);
            path.reset();
            path.moveTo(bx, h);
            path.lineTo(bx + th * 0.3f, h - th);
            path.lineTo(bx + th * 0.6f, h);
            path.close();
            c.drawPath(path, paint);
        }
    }

    private void drawBerries(Canvas c, float w, float h, float t, float px) {
        for (int i = 0; i < 8; i++) {
            float x = w * (0.08f + 0.12f * i);
            float vine = h * (0.3f + 0.4f * seeds[i]);
            paint.setColor(0xFF2F5A34);
            c.drawRect(x, 0, x + px, vine, paint);
            float glow = still ? 0.8f : 0.5f + 0.5f * (float) Math.abs(Math.sin(t * 1.3 + i));
            paint.setColor(PkTheme.withAlpha(0xF2A93B, (int) (90 * glow)));
            c.drawCircle(x + px / 2, vine, px * 4, paint);
            paint.setColor(0xFFF2A93B);
            c.drawRect(x - px, vine - px, x + px * 2, vine + px * 2, paint);
        }
    }
}
