package me.bbijabnpobatejb.fancyhealth.ui;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.bbijabnpobatejb.fancyhealth.config.ConfigStore;
import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import me.bbijabnpobatejb.fancyhealth.render.Colors;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ColorPopup {

    private static final int[] PALETTE = {
            0xFFFF453A, 0xFFFF9F0A, 0xFFFFD60A, 0xFF30D158, 0xFF66D4CF, 0xFF64D2FF, 0xFF0A84FF, 0xFF5E5CE6,
            0xFFBF5AF2, 0xFFFF375F, 0xFFAC8E68, 0xFFFFFFFF, 0xFFC7C7CC, 0xFF8E8E93, 0xFF48484A, 0xFF000000,
            0xFF7A1E1E, 0xFF1E4F7A, 0xB0101014, 0x80000000, 0x40FFFFFF, 0xFF46D35C, 0xFFFFE27A, 0x00000000
    };

    public static final int W = 150;
    public static final int H = 104;

    final Rows.ColorRow row;
    final Rows.Host host;
    public float x;
    public float y;
    int dragBar = -1;
    float hue;
    float sat;
    float bri;
    float appear;

    public ColorPopup(Rows.ColorRow row, Rows.Host host, float x, float y) {
        this.row = row;
        this.host = host;
        this.x = x;
        this.y = y;
        syncHsv();
    }

    private int value() {
        return row.getter().apply(ConfigStore.get());
    }

    private void syncHsv() {
        float[] hsv = Colors.toHsv(value());
        hue = hsv[0];
        sat = hsv[1];
        bri = hsv[2];
    }

    private void set(int argb) {
        row.setter().accept(ConfigStore.get(), argb);
        host.changed(row);
    }

    public void render(Canvas c, double mx, double my, float dt) {
        appear = Math.min(1f, appear + dt * 9f);
        float s = appear >= 1f ? 1f : 0.92f + 0.08f * appear;
        c.push();
        if (s == 1f) {
            c.translate(Theme.snap(x), Theme.snap(y));
        } else {
            c.translate(x + W / 2f, y);
            c.scale(s);
            c.translate(-W / 2f, 0);
        }
        Theme.shadow(c, 0, 0, W, H, 12, (int) (140 * appear));
        Theme.rrect(c, 0, 0, W, H, 12, 0xF8222224);
        for (int i = 0; i < PALETTE.length; i++) {
            float px = 8 + (i % 12) * 11.2f, py = 8 + (i / 12) * 12;
            boolean over = Theme.in(mx, my, x + px, y + py, x + px + 10, y + py + 10);
            Rows.ColorRow.checker(c, px + 5, py + 5, 4.6f);
            Theme.circle(c, px + 5, py + 5, over ? 5.2f : 4.6f, PALETTE[i]);
        }
        int v = value();
        float[] marks = {hue, sat, bri, (v >>> 24) / 255f};
        String[] labels = {"H", "S", "B", "A"};
        for (int i = 0; i < 4; i++) {
            float by = 36 + i * 16;
            Theme.small(c, Theme.ui(labels[i]), 8, by - 0.5f, Theme.TEXT2);
            bar(c, i, by);
            float bx0 = 20, bx1 = W - 10;
            float m = bx0 + (bx1 - bx0) * marks[i];
            Theme.circle(c, m, by + 3.5f + 0.5f, 5.2f, 0x40000000);
            Theme.circle(c, m, by + 3.5f, 4.8f, 0xFFFFFFFF);
        }
        c.pop();
    }

    private void bar(Canvas c, int i, float by) {
        float bx0 = 20, bx1 = W - 10;
        int steps = 40;
        int v = value();
        if (i == 3) {
            for (int s = 0; s < 20; s++) {
                float sx0 = bx0 + (bx1 - bx0) * s / 20f;
                c.fill((int) sx0, (int) (by + 1), (int) (sx0 + (bx1 - bx0) / 20f), (int) (by + 6), s % 2 == 0 ? 0xFFBDBDBD : 0xFF808080);
            }
        }
        for (int s = 0; s < steps; s++) {
            float t = s / (float) (steps - 1);
            int col = switch (i) {
                case 0 -> Colors.hsv(t, 1f, 1f);
                case 1 -> Colors.hsv(hue, t, Math.max(bri, 0.15f));
                case 2 -> Colors.hsv(hue, sat, t);
                default -> Colors.withAlpha(v, Math.round(t * 255));
            };
            float sx0 = bx0 + (bx1 - bx0) * s / steps, sx1 = bx0 + (bx1 - bx0) * (s + 1) / steps;
            Theme.rrect(c, sx0, by + 1, sx1 + 0.5f, by + 6, s == 0 || s == steps - 1 ? 2.5f : 0, col);
        }
    }

    public boolean contains(double mx, double my) {
        return Theme.in(mx, my, x, y, x + W, y + H);
    }

    public boolean click(double mx, double my) {
        if (!contains(mx, my)) return false;
        for (int i = 0; i < PALETTE.length; i++) {
            float px = x + 8 + (i % 12) * 11.2f, py = y + 8 + (i / 12) * 12;
            if (Theme.in(mx, my, px, py, px + 10, py + 10)) {
                set(PALETTE[i]);
                syncHsv();
                Sounds.play("click", 1.2f);
                return true;
            }
        }
        for (int i = 0; i < 4; i++) {
            float by = y + 36 + i * 16;
            if (my >= by - 3 && my < by + 10) {
                dragBar = i;
                drag(mx, my);
                return true;
            }
        }
        return true;
    }

    public void drag(double mx, double my) {
        if (dragBar < 0) return;
        float bx0 = x + 20, bx1 = x + W - 10;
        float t = (float) Math.max(0, Math.min(1, (mx - bx0) / (bx1 - bx0)));
        int v = value();
        int a = v >>> 24;
        switch (dragBar) {
            case 0 -> hue = t;
            case 1 -> sat = t;
            case 2 -> bri = t;
            default -> a = Math.round(t * 255);
        }
        if (dragBar == 3) {
            set(Colors.withAlpha(v, a));
        } else {
            if (a == 0) a = 255;
            set(Colors.withAlpha(Colors.hsv(hue, sat, bri), a));
        }
        Sounds.play("tick", 0.8f + t * 0.6f);
    }

    public void release() {
        dragBar = -1;
    }
}
