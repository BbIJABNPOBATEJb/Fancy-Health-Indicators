package me.bbijabnpobatejb.fancyhealth.render;

import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import me.bbijabnpobatejb.fancyhealth.config.BarStyle;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.config.ManyHearts;
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
            case HEARTS, GAME_HEARTS -> heartsHeight(l, m);
            case NAMETAG -> 10;
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
            case HEARTS -> top = -hearts(c, l, m, a, false) - 1;
            case GAME_HEARTS -> top = -hearts(c, l, m, a, true) - 1;
            case NAMETAG -> top = -nametag(c, l, m, a) - 1;
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

    private static final String[] LAYER_SPRITES = {"", "absorbing_", "poisoned_", "frozen_", "withered_"};
    private static final int[] LAYER_COLORS = {0xFFFF3B3B, 0xFFFFC21F, 0xFF57D33B, 0xFF5FD0FF, 0xFFB46CFF, 0xFFFF7AD9};

    private int heartCount(float hp) {
        return (int) Math.ceil(hp / 2f - 0.001f);
    }

    private ManyHearts heartMode(FhConfig.BarLook l, BarModel m) {
        int total = heartCount(m.max) + heartCount(m.absorption);
        if (total <= 10) return ManyHearts.ROWS;
        if (l.manyHearts == ManyHearts.ROWS && total > 100) return ManyHearts.LAYERS;
        return l.manyHearts;
    }

    private int rowStep(int rows) {
        return Math.max(3, 10 - (rows - 2));
    }

    private int heartsHeight(FhConfig.BarLook l, BarModel m) {
        return switch (heartMode(l, m)) {
            case NUMBER -> 9;
            case LAYERS -> m.absorption > 0 ? 9 + rowStep(2) : 9;
            case ROWS -> {
                int rows = Math.max(1, (heartCount(m.max) + heartCount(m.absorption) + 9) / 10);
                yield 9 + (rows - 1) * rowStep(rows);
            }
        };
    }

    private String gameType(BarModel m) {
        return switch (m.heartType) {
            case 1 -> "poisoned_";
            case 2 -> "withered_";
            case 3 -> "frozen_";
            default -> m.hardcore ? "hardcore_" : "";
        };
    }

    private void heart(Canvas c, boolean game, String kind, boolean half, int layer, int x, int y, int argb, BarModel m) {
        if (game) {
            String id = switch (kind) {
                case "container" -> "hud/heart/container";
                case "blink" -> "hud/heart/" + (half ? "half_blinking" : "full_blinking");
                case "absorbing" -> "hud/heart/absorbing_" + (half ? "half" : "full");
                default -> {
                    String type = layer > 0 ? LAYER_SPRITES[layer % LAYER_SPRITES.length] : gameType(m);
                    yield "hud/heart/" + type + (half ? "half" : "full");
                }
            };
            c.guiSprite(id, x, y, 9, 9, argb);
            return;
        }
        switch (kind) {
            case "container" -> icon(c, 0, x, y, argb);
            case "blink" -> icon(c, half ? 4 : 3, x, y, argb);
            case "absorbing" -> icon(c, half ? 6 : 5, x, y, argb);
            default -> {
                if (layer == 0) icon(c, half ? 2 : 1, x, y, argb);
                else icon(c, half ? 4 : 3, x, y, Colors.mul(argb, LAYER_COLORS[layer % LAYER_COLORS.length]));
            }
        }
    }

    public int hearts(Canvas c, FhConfig.BarLook l, BarModel m, float a, boolean game) {
        int white = Colors.alpha(0xFFFFFFFF, a);
        float shown = m.shown, trail = m.anim != null ? m.anim.trail(l.trailHold, l.trailSpeed) : m.trail;
        boolean blink = l.hitFlash && (m.anim != null ? m.anim.flash() : m.flash) > 0.4f;
        int maxHearts = heartCount(m.max), absHearts = heartCount(m.absorption);
        ManyHearts mode = heartMode(l, m);
        int height = heartsHeight(l, m);
        int textX;
        if (mode == ManyHearts.NUMBER) {
            heart(c, game, "container", false, 0, -4, -9, white, m);
            if (shown > 0.01f) heart(c, game, "full", shown < m.max * 0.5f && shown < 1.5f, 0, -4, -9, white, m);
            Component t = TextFx.styled(value(l.valueText == ValueText.NONE ? ValueText.CURRENT_MAX : l.valueText, m, l.showAbsorption), l.font);
            TextFx.draw(c, t, 7, -8, white, l.textEffect, Colors.alpha(0xFF000000, a));
            return height;
        }
        if (mode == ManyHearts.LAYERS) {
            int slots = Math.min(10, maxHearts);
            int top = Math.max(0, (int) Math.ceil(shown / 20f - 0.0001f) - 1);
            float rem = shown - top * 20f;
            float trailRem = trail - top * 20f;
            int rowW = slots * 8 + 1;
            int sx = -rowW / 2;
            for (int i = 0; i < slots; i++) {
                int x = sx + i * 8;
                heart(c, game, "container", false, 0, x, -9, white, m);
                if (top > 0) heart(c, game, "full", false, top - 1, x, -9, white, m);
                if (l.trail && trailRem - i * 2 > 0.01f) heart(c, game, "blink", trailRem - i * 2 < 1.5f, 0, x, -9, white, m);
                float v = rem - i * 2;
                if (v > 0.01f) heart(c, game, blink ? "blink" : "full", v < 1.5f, top, x, -9, white, m);
            }
            if (absHearts > 0) {
                int n = Math.min(10, absHearts);
                for (int i = 0; i < n; i++) {
                    float v = m.absorption - i * 2;
                    heart(c, game, "absorbing", v < 1.5f, 0, sx + i * 8, -9 - rowStep(2), white, m);
                }
            }
            textX = sx + rowW + 2;
            int layers = (int) Math.ceil(m.max / 20f - 0.0001f);
            if (layers > 1) {
                Component t = TextFx.styled("x" + (top + 1), l.font);
                c.push();
                c.translate(textX, -8);
                c.scale(0.75f);
                TextFx.draw(c, t, 0, 0, Colors.alpha(LAYER_COLORS[top % LAYER_COLORS.length] | 0xFF000000, a), me.bbijabnpobatejb.fancyhealth.config.TextEffect.SHADOW, Colors.alpha(0xFF000000, a));
                c.pop();
                textX += 12;
            }
        } else {
            int total = maxHearts + absHearts;
            int rows = Math.max(1, (total + 9) / 10);
            int step = rowStep(rows);
            int rowW = Math.min(total, 10) * 8 + 1;
            int sx = -rowW / 2;
            for (int i = 0; i < total; i++) {
                int x = sx + (i % 10) * 8;
                int y = -9 - (i / 10) * step;
                heart(c, game, "container", false, 0, x, y, white, m);
                if (i < maxHearts) {
                    float base = i * 2f;
                    if (l.trail && trail - base > 0.01f) heart(c, game, "blink", trail - base < 1.5f, 0, x, y, white, m);
                    float v = shown - base;
                    if (v > 0.01f) heart(c, game, blink ? "blink" : "full", v < 1.5f, 0, x, y, white, m);
                } else {
                    float v = m.absorption - (i - maxHearts) * 2f;
                    heart(c, game, "absorbing", v < 1.5f, 0, x, y, white, m);
                }
            }
            textX = sx + rowW + 2;
        }
        if (l.valueText != ValueText.NONE) {
            Component t = TextFx.styled(value(l.valueText, m, l.showAbsorption), l.font);
            c.push();
            c.translate(textX, -8);
            c.scale(0.75f);
            TextFx.draw(c, t, 0, 0, white, l.textEffect, Colors.alpha(0xFF000000, a));
            c.pop();
        }
        return height;
    }

    public int nametag(Canvas c, FhConfig.BarLook l, BarModel m, float a) {
        ValueText v = l.valueText == ValueText.NONE ? ValueText.CURRENT : l.valueText;
        String text = v == ValueText.CURRENT ? String.valueOf((int) Math.ceil(m.health - 0.001f)) : value(v, m, l.showAbsorption);
        Component score = TextFx.styled(text + " ", l.font);
        Component mark = Component.literal("❤");
        int sw = TextFx.width(score), w = sw + TextFx.width(mark);
        int x = -w / 2;
        c.fill(x - 1, -10, x + w + 1, -1, Colors.alpha(l.background, a));
        TextFx.draw(c, score, x, -9, Colors.alpha(0xFFFFFFFF, a), l.textEffect, Colors.alpha(0xFF000000, a));
        TextFx.draw(c, mark, x + sw, -9, Colors.alpha(color(l, m), a), l.textEffect, Colors.alpha(0xFF000000, a));
        return 10;
    }
}
