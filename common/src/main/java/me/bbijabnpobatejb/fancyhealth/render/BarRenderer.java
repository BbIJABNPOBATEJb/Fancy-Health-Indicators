package me.bbijabnpobatejb.fancyhealth.render;

import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import me.bbijabnpobatejb.fancyhealth.config.BarStyle;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.config.ValueText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Locale;

@UtilityClass
public class BarRenderer {

    public final Identifier ICONS = FancyHealth.id("textures/gui/icons.png");
    public final int ICONS_W = 64;
    public final int ICONS_H = 32;
    private final int GOLD = 0xFFFFD84A;

    public void icon(Canvas c, int index, int x, int y, int argb) {
        c.sprite(ICONS, x, y, 9, 9, (index % 7) * 9, (index / 7) * 9, ICONS_W, ICONS_H, argb);
    }

    public int color(FhConfig.BarLook l, BarModel m) {
        float pct = m.max <= 0 ? 0 : m.health / m.max;
        int col = switch (l.colorMode) {
            case HEALTH -> Colors.health(pct);
            case RELATION -> m.relation;
            case FIXED -> l.color;
            case TEAM -> m.team != 0 ? m.team : m.relation;
        };
        if (l.lowHpPulse && pct < 0.25f && pct > 0) {
            float p = 0.5f + 0.5f * (float) Math.sin(m.time * 9f);
            col = Colors.mix(col, 0xFFFFFFFF, 0.35f * p);
        }
        return col;
    }

    public String value(ValueText v, BarModel m, boolean absorption) {
        String hp = num(m.health);
        if (absorption && m.absorption > 0) hp = hp + "+" + num(m.absorption);
        return switch (v) {
            case NONE -> "";
            case CURRENT -> hp;
            case CURRENT_MAX -> hp + "/" + num(m.max);
            case PERCENT -> Math.round(m.max <= 0 ? 0 : m.health / m.max * 100f) + "%";
            case HEARTS -> num(m.health / 2f) + "❤";
        };
    }

    public String num(float v) {
        if (Math.abs(v - Math.round(v)) < 0.05f) return String.valueOf(Math.round(v));
        return String.format(Locale.ROOT, "%.1f", v);
    }

    public int plateHeight(FhConfig.BarLook l, BarModel m, boolean name) {
        int h = switch (l.style) {
            case TEXT -> 9;
            case HEARTS -> heartsHeight(m);
            default -> Math.max(1, l.height) + (l.border ? 2 : 0);
        };
        if (name && l.showName && m.name != null) h += Math.round(2 + 9 * nameScale(l));
        return h;
    }

    public void plate(Canvas c, FhConfig.BarLook l, BarModel m, float a, boolean name, boolean nameBelow) {
        int top;
        int bottom = 0;
        switch (l.style) {
            case TEXT -> {
                String s = "❤ " + value(l.valueText == ValueText.NONE ? ValueText.CURRENT_MAX : l.valueText, m, l.showAbsorption);
                Component t = TextFx.styled(s, l.font);
                TextFx.centered(c, t, 0, -9, Colors.alpha(color(l, m), a), l.textEffect, Colors.alpha(0xFF000000, a));
                top = -10;
            }
            case HEARTS -> top = -hearts(c, l, m, a) - 1;
            default -> {
                int w = Math.max(4, l.width), h = Math.max(1, l.height);
                int x0 = -w / 2, x1 = x0 + w, y0 = -h, y1 = 0;
                if (l.border) {
                    y0 -= 1;
                    y1 -= 1;
                }
                bar(c, l, m, x0, y0, x1, y1, a);
                valueText(c, l, m, x0, y0, x1, y1, a);
                if (l.showArmor && m.armor > 0) armor(c, l, m, x0 - (l.border ? 2 : 1), (y0 + y1) / 2, a);
                top = y0 - (l.border ? 1 : 0) - 1;
            }
        }
        if (name && l.showName && m.name != null) {
            Component nm = name(l, m);
            int nw = TextFx.width(nm);
            float ns = nameScale(l);
            c.push();
            c.translate(0, nameBelow ? bottom + 3 : top - 1);
            c.scale(ns);
            TextFx.draw(c, nm, -nw / 2, nameBelow ? 0 : -8, Colors.alpha(l.nameColor, a), l.nameEffect, Colors.alpha(0xFF000000, a));
            c.pop();
        }
    }

    public Component name(FhConfig.BarLook l, BarModel m) {
        return TextFx.styled(m.name.getString(), l.nameFont);
    }

    public float nameScale(FhConfig.BarLook l) {
        return Math.max(0.4f, Math.min(3f, l.nameScale));
    }

    private void armor(Canvas c, FhConfig.BarLook l, BarModel m, int right, int cy, float a) {
        Component t = TextFx.styled(String.valueOf(m.armor), l.font);
        int tw = TextFx.width(t);
        int x = right - tw - 10;
        icon(c, 7, x, cy - 4, Colors.alpha(0xFFFFFFFF, a));
        TextFx.draw(c, t, x + 10, cy - 4, Colors.alpha(0xFFE6E6E6, a), me.bbijabnpobatejb.fancyhealth.config.TextEffect.SHADOW, 0xFF000000);
    }

    public void valueText(Canvas c, FhConfig.BarLook l, BarModel m, int x0, int y0, int x1, int y1, float a) {
        if (l.valueText == ValueText.NONE) return;
        Component t = TextFx.styled(value(l.valueText, m, l.showAbsorption), l.font);
        int tw = TextFx.width(t);
        int h = y1 - y0;
        int col = Colors.alpha(0xFFFFFFFF, a);
        int out = Colors.alpha(0xFF000000, a);
        if (h >= 7) {
            float s = Math.min(1f, (h + 1) / 9f);
            c.push();
            c.translate((x0 + x1) / 2f, (y0 + y1) / 2f);
            c.scale(s);
            TextFx.draw(c, t, -tw / 2, -4, col, l.textEffect, out);
            c.pop();
        } else {
            c.push();
            c.translate(x1 + 3 + (l.border ? 1 : 0), (y0 + y1) / 2f);
            float s = 0.75f;
            c.scale(s);
            TextFx.draw(c, t, 0, -4, col, l.textEffect, out);
            c.pop();
        }
    }

    public void bar(Canvas c, FhConfig.BarLook l, BarModel m, int x0, int y0, int x1, int y1, float a) {
        float max = Math.max(m.max, 0.0001f);
        float trailV = m.anim != null ? m.anim.trail(l.trailHold, l.trailSpeed) : m.trail;
        float flashV = m.anim != null ? m.anim.flash() : m.flash;
        float p = clamp(m.shown / max), tr = clamp(trailV / max), hp = clamp(m.health / max), ab = clamp(m.absorption / max);
        int w = x1 - x0, h = y1 - y0;
        int fw = Math.round(w * p), tw = Math.round(w * tr), hw = Math.round(w * hp);
        int col = color(l, m);
        int bg = Colors.alpha(l.background, a);
        int border = Colors.alpha(l.borderColor, a);
        int fill = Colors.alpha(col, a);
        boolean showTrail = l.trail || l.hitFlash && flashV > 0.01f;
        int trailBase = l.trail ? l.trailColor : Colors.withAlpha(l.flashColor, 0);
        int trail = Colors.alpha(l.hitFlash ? Colors.mix(trailBase, l.flashColor, flashV) : trailBase, a);
        switch (l.style) {
            case FLAT, SEGMENTED, LINE -> {
                if (l.border) c.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, border);
                if (l.style != BarStyle.LINE || l.border) c.fill(x0, y0, x1, y1, bg);
                else c.fill(x0, y0, x1, y1, Colors.alpha(0x50000000, a));
                if (showTrail && tw > fw) c.fill(x0 + fw, y0, x0 + tw, y1, trail);
                if (hw > fw) c.fill(x0 + fw, y0, x0 + hw, y1, Colors.alpha(Colors.lighten(col, 0.5f), a * 0.7f));
                c.fill(x0, y0, x0 + fw, y1, fill);
                if (l.style == BarStyle.SEGMENTED && l.segments > 1) {
                    int sep = Colors.alpha(0xA0000000, a);
                    for (int i = 1; i < l.segments; i++) {
                        int sx = x0 + Math.round(w * i / (float) l.segments);
                        c.fill(sx, y0, sx + 1, y1, sep);
                    }
                }
            }
            case GLOSS, SKEWED -> {
                if (l.style == BarStyle.SKEWED) {
                    c.push();
                    c.translate(0, (y0 + y1) / 2f);
                    c.shear(-0.7f);
                    c.translate(0, -(y0 + y1) / 2f);
                }
                if (l.border) c.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, border);
                c.fill(x0, y0, x1, y1, bg);
                if (showTrail && tw > fw) c.fill(x0 + fw, y0, x0 + tw, y1, trail);
                if (hw > fw) c.fill(x0 + fw, y0, x0 + hw, y1, Colors.alpha(Colors.lighten(col, 0.5f), a * 0.7f));
                c.gradient(x0, y0, x0 + fw, y1, Colors.alpha(Colors.lighten(col, 0.3f), a), Colors.alpha(Colors.darken(col, 0.25f), a));
                if (h >= 3) c.fill(x0, y0, x0 + fw, y0 + Math.max(1, h / 3), Colors.alpha(0x40FFFFFF, a));
                if (l.style == BarStyle.SKEWED) {
                    absorption(c, x0, y0, w, h, ab, a);
                    c.pop();
                    return;
                }
            }
            case ROUNDED -> {
                if (l.border) round(c, x0 - 1, y0 - 1, x1 + 1, y1 + 1, border);
                round(c, x0, y0, x1, y1, bg);
                if (showTrail && tw > fw) round(c, x0, y0, x0 + tw, y1, trail);
                if (fw > 0) {
                    round(c, x0, y0, x0 + fw, y1, fill);
                    if (h >= 3) c.fill(x0 + 1, y0 + 1, x0 + fw - 1, y0 + 1 + Math.max(1, h / 3), Colors.alpha(0x50FFFFFF, a));
                }
            }
            case NEON -> {
                c.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, Colors.alpha(0x70000000, a));
                if (showTrail && tw > fw) c.fill(x0 + fw, y0, x0 + tw, y1, Colors.alpha(trail, 0.85f));
                if (fw > 0) {
                    c.fill(x0 - 2, y0 - 2, x0 + fw + 2, y1 + 2, Colors.alpha(col, a * 0.16f));
                    c.fill(x0 - 1, y0 - 1, x0 + fw + 1, y1 + 1, Colors.alpha(col, a * 0.3f));
                    c.fill(x0, y0, x0 + fw, y1, fill);
                    int mid = y0 + h / 2;
                    if (h >= 3) c.fill(x0, mid, x0 + fw, mid + 1, Colors.alpha(0xB0FFFFFF, a));
                    c.fill(x0 + fw - 1, y0 - 1, x0 + fw, y1 + 1, Colors.alpha(0xFFFFFFFF, a));
                }
            }
            case MMO -> {
                int frame = Colors.alpha(Colors.darken(m.relation, 0.45f), a);
                if (l.border) c.fill(x0 - 2, y0 - 2, x1 + 2, y1 + 2, border);
                c.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, frame);
                c.fill(x0, y0, x1, y1, Colors.mix(bg, Colors.alpha(0xFF1A1214, a), 0.35f));
                if (showTrail && tw > fw) c.fill(x0 + fw, y0, x0 + tw, y1, trail);
                c.gradient(x0, y0, x0 + fw, y1, Colors.alpha(Colors.lighten(col, 0.2f), a), Colors.alpha(Colors.darken(col, 0.35f), a));
                if (fw > 0) c.fill(x0, y0, x0 + fw, y0 + 1, Colors.alpha(0x55FFFFFF, a));
            }
            default -> {
            }
        }
        absorption(c, x0, y0, w, h, ab, a);
    }

    private void absorption(Canvas c, int x0, int y0, int w, int h, float ab, float a) {
        if (ab <= 0) return;
        int aw = Math.max(1, Math.round(w * ab));
        c.fill(x0, y0, x0 + aw, y0 + Math.max(1, h / 3), Colors.alpha(GOLD, a));
    }

    private void round(Canvas c, int x0, int y0, int x1, int y1, int col) {
        if (x1 - x0 < 3 || y1 - y0 < 3) {
            c.fill(x0, y0, x1, y1, col);
            return;
        }
        c.fill(x0 + 1, y0, x1 - 1, y1, col);
        c.fill(x0, y0 + 1, x0 + 1, y1 - 1, col);
        c.fill(x1 - 1, y0 + 1, x1, y1 - 1, col);
    }

    private float clamp(float v) {
        return v < 0 ? 0 : Math.min(1, v);
    }

    private int heartsHeight(BarModel m) {
        int total = heartCount(m.max) + heartCount(m.absorption);
        int rows = Math.max(1, (Math.min(total, 30) + 9) / 10);
        return 9 + (rows - 1) * rowStep(rows);
    }

    private int heartCount(float hp) {
        return (int) Math.ceil(hp / 2f - 0.001f);
    }

    private int rowStep(int rows) {
        return Math.max(4, 10 - (rows - 2));
    }

    public int hearts(Canvas c, FhConfig.BarLook l, BarModel m, float a) {
        int maxHearts = heartCount(m.max);
        int absHearts = heartCount(m.absorption);
        float shown = m.shown, trail = m.anim != null ? m.anim.trail(l.trailHold, l.trailSpeed) : m.trail;
        float unit = 2f;
        boolean compress = maxHearts + absHearts > 30;
        if (compress) {
            unit = Math.max(m.max, 1f) / 10f;
            maxHearts = 10;
            absHearts = Math.min(10, (int) Math.ceil(m.absorption / unit - 0.001f));
        }
        int total = maxHearts + absHearts;
        int perRow = 10;
        int rows = Math.max(1, (total + perRow - 1) / perRow);
        int step = rowStep(rows);
        int rowW = Math.min(total, perRow) * 8 + 1;
        int sx = -rowW / 2;
        int white = Colors.alpha(0xFFFFFFFF, a);
        boolean blink = l.hitFlash && (m.anim != null ? m.anim.flash() : m.flash) > 0.4f;
        for (int i = 0; i < total; i++) {
            int x = sx + (i % perRow) * 8;
            int y = -9 - (i / perRow) * step;
            icon(c, 0, x, y, white);
            if (i < maxHearts) {
                float base = i * unit;
                if (l.trail && trail - base > 0.01f) {
                    icon(c, trail - base >= unit * 0.75f ? 3 : 4, x, y, white);
                }
                float v = shown - base;
                if (v >= unit * 0.75f) icon(c, blink ? 3 : 1, x, y, white);
                else if (v > 0.01f) icon(c, blink ? 4 : 2, x, y, white);
            } else {
                float v = m.absorption - (i - maxHearts) * unit;
                icon(c, v >= unit * 0.75f ? 5 : 6, x, y, white);
            }
        }
        int height = 9 + (rows - 1) * step;
        if (compress) {
            Component t = TextFx.styled("x" + num(unit / 2f), l.font);
            c.push();
            c.translate(sx + rowW + 2, -8);
            c.scale(0.75f);
            TextFx.draw(c, t, 0, 0, white, me.bbijabnpobatejb.fancyhealth.config.TextEffect.SHADOW, 0xFF000000);
            c.pop();
        }
        if (l.valueText != ValueText.NONE) {
            Component t = TextFx.styled(value(l.valueText, m, l.showAbsorption), l.font);
            c.push();
            c.translate(sx + rowW + 2, -8 - (compress ? 8 : 0));
            c.scale(0.75f);
            TextFx.draw(c, t, 0, 0, white, l.textEffect, Colors.alpha(0xFF000000, a));
            c.pop();
        }
        return height;
    }
}
