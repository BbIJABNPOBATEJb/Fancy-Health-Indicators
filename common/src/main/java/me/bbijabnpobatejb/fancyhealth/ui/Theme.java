package me.bbijabnpobatejb.fancyhealth.ui;

import me.bbijabnpobatejb.fancyhealth.compat.Mc;
import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class Theme {

    public float debugNudge;

    public final int SIDEBAR = 0xF0161618;
    public final int PANEL = 0xF20B0B0D;
    public final int CARD = 0xFF1C1C1E;
    public final int CARD_HOVER = 0x14FFFFFF;
    public final int SEPARATOR = 0xFF2E2E31;
    public final int TEXT = 0xFFF5F5F7;
    public final int TEXT2 = 0xFF8E8E93;
    public final int TEXT3 = 0xFF5A5A5F;
    public final int BLUE = 0xFF0A84FF;
    public final int GREEN = 0xFF30D158;
    public final int RED = 0xFFFF453A;
    public final int ORANGE = 0xFFFF9F0A;
    public final int SWITCH_OFF = 0xFF39393D;
    public final int TRACK = 0xFF3A3A3C;
    public final int RESET_HIGHLIGHT = 0x3830D158;

    private final Style UI = Mc.font("fancyhealth:ui");
    private final Style BOLD = Mc.font("fancyhealth:ui_bold");

    public Font font() {
        return Minecraft.getInstance().font;
    }

    public MutableComponent ui(String s) {
        return Mc.literal(s).setStyle(UI);
    }

    public MutableComponent bold(String s) {
        return Mc.literal(s).setStyle(BOLD);
    }

    public MutableComponent tr(String key) {
        return ui(Mc.translatable(key).getString());
    }

    public int width(Component c) {
        return font().width(c);
    }

    public int width(String s) {
        return font().width(ui(s));
    }

    public void text(Canvas c, Component t, float x, float y, int color) {
        c.push();
        c.translate(x, y);
        c.text(font(), t, 0, 0, color, false);
        c.pop();
    }

    public void text(Canvas c, String t, float x, float y, int color) {
        text(c, ui(t), x, y, color);
    }

    public void scaled(Canvas c, Component t, float x, float y, float s, int color) {
        c.push();
        c.translate(x, y);
        c.scale(s);
        c.text(font(), t, 0, 0, color, false);
        c.pop();
    }

    public void small(Canvas c, Component t, float x, float y, int color) {
        scaled(c, t, x, y, 0.8f, color);
    }

    public void centered(Canvas c, Component t, float cx, float y, int color) {
        text(c, t, cx - width(t) / 2f, y, color);
    }

    public void fit(Canvas c, Component t, float x, float y, float maxW, int color, boolean hovered, float time, int align) {
        int w = width(t);
        if (w <= maxW) {
            float dx = align == 0 ? 0 : align == 1 ? (maxW - w) / 2f : maxW - w;
            text(c, t, x + dx, y, color);
            return;
        }
        float s = maxW / w;
        if (s >= 0.74f) {
            c.push();
            c.translate(x, y + (1 - s) * 4.5f);
            c.scale(s);
            c.text(font(), t, 0, 0, color, false);
            c.pop();
            return;
        }
        if (align == 0 && t.getString().contains(" ")) {
            List<String> two = wrap(t.getString(), (int) (maxW / 0.7f), 3);
            if (two.size() == 2 && width(two.get(0)) * 0.7f <= maxW && width(two.get(1)) * 0.7f <= maxW) {
                Style st = t.getStyle();
                scaled(c, Mc.literal(two.get(0)).setStyle(st), x, y - 2.6f, 0.7f, color);
                scaled(c, Mc.literal(two.get(1)).setStyle(st), x, y + 4.4f, 0.7f, color);
                return;
            }
        }
        float scale = 0.74f;
        float sw = w * scale;
        float over = sw - maxW;
        float offset = 0;
        if (hovered) {
            float period = 1.2f + over / 18f;
            float p = (time % (period * 2)) / period;
            float k = p < 1 ? p : 2 - p;
            k = Math.max(0, Math.min(1, (k - 0.15f) / 0.7f));
            offset = -over * k;
        }
        c.scissor((int) x, (int) y - 2, (int) Math.ceil(x + maxW), (int) y + 10);
        c.push();
        c.translate(x + offset, y + (1 - scale) * 4.5f);
        c.scale(scale);
        c.text(font(), t, 0, 0, color, false);
        c.pop();
        c.endScissor();
    }

    public String ellipsize(String s, int width) {
        if (width(s) <= width) return s;
        String dots = "…";
        int end = s.length();
        while (end > 0 && width(s.substring(0, end)) + width(dots) > width) end--;
        return s.substring(0, end).trim() + dots;
    }

    public List<String> wrap(String s, int width, int maxLines) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String w : s.split(" ")) {
            String next = line.isEmpty() ? w : line + " " + w;
            if (width(next) <= width || line.isEmpty()) {
                line = new StringBuilder(next);
            } else {
                out.add(line.toString());
                line = new StringBuilder(w);
            }
        }
        if (!line.isEmpty()) out.add(line.toString());
        if (out.size() > maxLines) {
            List<String> cut = new ArrayList<>(out.subList(0, maxLines));
            cut.set(maxLines - 1, ellipsize(cut.get(maxLines - 1) + " " + out.get(maxLines), width));
            return cut;
        }
        return out;
    }

    public final String ICONS = "fancyhealth:textures/gui/ui_icons.png";
    public final int ICON_COUNT = 11;
    public final int ICON_MAX_SCALE = 6;

    public float snap(float v) {
        int s = guiScale();
        return Math.round(v * s) / (float) s;
    }

    public int guiScale() {
        return Math.max(1, (int) Math.round(Minecraft.getInstance().getWindow().getGuiScale()));
    }

    public final String CORNERS = "fancyhealth:textures/gui/corners.png";
    private final int CORNER_MAX = 64;
    private final int CORNERS_W = 2208;
    private final int CORNERS_H = 264;
    private final int ICON_MIN_PX = 4;
    private final int ICON_MAX_PX = 64;

    public void rrect(Canvas c, float x0, float y0, float x1, float y1, float r, int color) {
        if ((color >>> 24) == 0 || x1 <= x0 || y1 <= y0) return;
        int s = guiScale();
        int X0 = Math.round(x0 * s), Y0 = Math.round(y0 * s);
        int W = Math.max(1, Math.round((x1 - x0) * s)), H = Math.max(1, Math.round((y1 - y0) * s));
        int R = Math.max(0, Math.min(CORNER_MAX, Math.min(Math.round(r * s), Math.min(W / 2, H / 2))));
        if (R > 0 && W - 2 * R == 1) W--;
        if (R > 0 && H - 2 * R == 1) H--;
        int X1 = X0 + W, Y1 = Y0 + H;
        c.push();
        c.scale(1f / s);
        if (R == 0) {
            c.fill(X0, Y0, X1, Y1, color);
        } else {
            int u = R * (R - 1) / 2 + 2 * (R - 1) + 1;
            int slot = CORNER_MAX + 2;
            c.sprite(CORNERS, X0, Y0, R, R, u, 1, CORNERS_W, CORNERS_H, color);
            c.sprite(CORNERS, X1 - R, Y0, R, R, u, slot + 1, CORNERS_W, CORNERS_H, color);
            c.sprite(CORNERS, X0, Y1 - R, R, R, u, slot * 2 + 1, CORNERS_W, CORNERS_H, color);
            c.sprite(CORNERS, X1 - R, Y1 - R, R, R, u, slot * 3 + 1, CORNERS_W, CORNERS_H, color);
            c.fill(X0 + R, Y0, X1 - R, Y0 + R, color);
            c.fill(X0 + R, Y1 - R, X1 - R, Y1, color);
            c.fill(X0, Y0 + R, X1, Y1 - R, color);
        }
        c.pop();
    }

    public void rrectBottom(Canvas c, float x0, float y0, float x1, float y1, float r, float band, int color) {
        if ((color >>> 24) == 0 || x1 <= x0 || y1 <= y0) return;
        int s = guiScale();
        int X0 = Math.round(x0 * s), Y0 = Math.round(y0 * s);
        int W = Math.max(1, Math.round((x1 - x0) * s)), H = Math.max(1, Math.round((y1 - y0) * s));
        int R = Math.max(0, Math.min(CORNER_MAX, Math.min(Math.round(r * s), Math.min(W / 2, H / 2))));
        if (R > 0 && W - 2 * R == 1) W--;
        if (R > 0 && H - 2 * R == 1) H--;
        int X1 = X0 + W, Y1 = Y0 + H;
        int B = Math.max(1, Math.min(H, Math.round(band * s)));
        c.push();
        c.scale(1f / s);
        if (R == 0) {
            c.fill(X0, Y1 - B, X1, Y1, color);
        } else {
            int u = R * (R - 1) / 2 + 2 * (R - 1) + 1;
            int slot = CORNER_MAX + 2;
            int k = Math.min(B, R);
            c.sprite(CORNERS, X0, Y1 - k, R, k, u, slot * 2 + 1 + R - k, CORNERS_W, CORNERS_H, color);
            c.sprite(CORNERS, X1 - R, Y1 - k, R, k, u, slot * 3 + 1 + R - k, CORNERS_W, CORNERS_H, color);
            c.fill(X0 + R, Y1 - k, X1 - R, Y1, color);
            if (B > R) c.fill(X0, Y1 - B, X1, Y1 - R, color);
        }
        c.pop();
    }

    public void icon(Canvas c, int index, float cx, float cy, float size, int color) {
        int s = guiScale();
        int target = Math.max(ICON_MIN_PX, Math.round(size * s));
        int src = Math.min(ICON_MAX_PX, target);
        int texW = ICON_COUNT * (ICON_MAX_PX + 2);
        int texH = 0;
        for (int px = ICON_MIN_PX; px <= ICON_MAX_PX; px++) texH += px + 2;
        int v = 0;
        for (int px = ICON_MIN_PX; px < src; px++) v += px + 2;
        int u = index * (src + 2) + 1;
        c.push();
        c.scale(1f / s);
        int X = Math.round(cx * s - target / 2f), Y = Math.round(cy * s - target / 2f);
        if (target == src) {
            c.sprite(ICONS, X, Y, src, src, u, v + 1, texW, texH, color);
        } else {
            c.translate(X, Y);
            c.scale(target / (float) src);
            c.sprite(ICONS, 0, 0, src, src, u, v + 1, texW, texH, color);
        }
        c.pop();
    }

    public void circle(Canvas c, float cx, float cy, float r, int color) {
        rrect(c, cx - r, cy - r, cx + r, cy + r, r, color);
    }

    public void shadow(Canvas c, float x0, float y0, float x1, float y1, float r, int alpha) {
        for (int i = 3; i >= 1; i--) {
            rrect(c, x0 - i * 0.5f, y0 - i * 0.5f + 0.5f, x1 + i * 0.5f, y1 + i * 0.5f + 0.8f, r + i * 0.5f, (alpha / (i + 1)) << 24);
        }
    }

    public void editField(Canvas c, float x0, float x1, float y0, float y1, String buffer, boolean selected, float time, float scale) {
        rrect(c, x0, y0, x1, y1, 5, 0xFF2C2C2E);
        Component t = ui(buffer);
        float tw = width(t) * scale;
        float avail = x1 - x0 - 8;
        float s = tw > avail ? scale * avail / tw : scale;
        tw = width(t) * s;
        float th = 9 * s;
        float tx = x1 - 5 - tw, ty = (y0 + y1) / 2f - th / 2f + 0.5f * s;
        if (selected && !buffer.isEmpty()) rrect(c, tx - 1.5f, ty - 1.5f, tx + tw + 1.5f, ty + th - 0.5f, 2, 0x800A84FF);
        scaled(c, t, tx, ty, s, TEXT);
        if (!selected && (time % 1f) < 0.55f) rrect(c, tx + tw + 0.5f, ty - 1, tx + tw + 1.5f, ty + th - 1, 0, BLUE);
        rrect(c, x0 + 3, y1 - 1, x1 - 3, y1, 0, BLUE);
    }

    public void resetIcon(Canvas c, float cx, float cy, float r, int color) {
        c.push();
        c.scale(0.25f);
        float k = 4f;
        int t = 4;
        for (int a = 50; a <= 335; a += 6) {
            double rad = Math.toRadians(a);
            int px = Math.round((cx + (float) Math.cos(rad) * r) * k), py = Math.round((cy + (float) Math.sin(rad) * r) * k);
            c.fill(px - t / 2, py - t / 2, px - t / 2 + t, py - t / 2 + t, color);
        }
        double end = Math.toRadians(335);
        float ex = cx + (float) Math.cos(end) * r, ey = cy + (float) Math.sin(end) * r;
        for (int i = 0; i <= 6; i++) {
            float d = i * 0.3f;
            int ax = Math.round((ex - d) * k), ay = Math.round((ey - 0.2f) * k);
            int bx = Math.round(ex * k), by = Math.round((ey + d) * k);
            c.fill(ax - 1, ay - 1, ax + 2, ay + 2, color);
            c.fill(bx - 1, by - 1, bx + 2, by + 2, color);
        }
        c.pop();
    }

    public boolean in(double mx, double my, float x0, float y0, float x1, float y1) {
        return mx >= x0 && mx < x1 && my >= y0 && my < y1;
    }

    public int mix(int a, int b, float t) {
        return me.bbijabnpobatejb.fancyhealth.render.Colors.mix(a, b, t);
    }
}
