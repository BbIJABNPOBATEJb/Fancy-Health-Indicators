package me.bbijabnpobatejb.fancyhealth.hud;

import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.config.TextEffect;
import me.bbijabnpobatejb.fancyhealth.config.ValueText;
import me.bbijabnpobatejb.fancyhealth.render.BarModel;
import me.bbijabnpobatejb.fancyhealth.render.BarRenderer;
import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import me.bbijabnpobatejb.fancyhealth.render.Colors;
import me.bbijabnpobatejb.fancyhealth.render.TextFx;
import me.bbijabnpobatejb.fancyhealth.track.BarAnim;
import me.bbijabnpobatejb.fancyhealth.track.EntityState;
import me.bbijabnpobatejb.fancyhealth.track.HealthTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

@UtilityClass
public class TargetHud {

    private final BarModel MODEL = new BarModel();
    private LivingEntity shown;
    private float alpha;

    public void render(Canvas canvas, Minecraft mc, FhConfig c, float dt) {
        LivingEntity t = pick(mc, c);
        if (t != null) shown = t;
        alpha = t != null ? Math.min(1f, alpha + dt * 6f) : Math.max(0f, alpha - dt * 4f);
        if (shown == null || alpha <= 0.01f) return;
        if (shown.isRemoved()) {
            shown = null;
            return;
        }
        EntityState s = HealthTracker.state(shown);
        BarAnim anim = s != null ? s.anim : null;
        if (anim == null) {
            anim = new BarAnim();
            anim.reset(shown.getHealth());
        }
        if (s != null) MODEL.set(s);
        else MODEL.set(shown, anim);
        float dist = mc.player == null ? 0 : shown.distanceTo(mc.player);
        int w = width(c), h = height(c);
        float sc = c.target.scale;
        int sw = canvas.width(), sh = canvas.height();
        float x = switch (c.target.position) {
            case TOP_LEFT -> 4;
            case TOP_RIGHT -> sw - w * sc - 4;
            default -> (sw - w * sc) / 2f;
        };
        float y = switch (c.target.position) {
            case ABOVE_HOTBAR -> sh - 62 - h * sc - c.target.offsetY;
            default -> c.target.offsetY;
        };
        canvas.push();
        canvas.translate(x + c.target.offsetX, y);
        canvas.scale(sc);
        draw(canvas, c, MODEL, shown, dist, alpha);
        canvas.pop();
    }

    private LivingEntity pick(Minecraft mc, FhConfig c) {
        int linger = (int) (c.target.lingerSeconds * 20f);
        LivingEntity look = HealthTracker.lookTarget();
        boolean lookOk = look != null && look.isAlive() && !look.isInvisible() && HealthTracker.lookAge() <= Math.max(1, linger)
                && mc.player != null && look.distanceTo(mc.player) <= c.target.range;
        LivingEntity hit = HealthTracker.hitTarget();
        boolean hitOk = hit != null && !hit.isRemoved() && me.bbijabnpobatejb.fancyhealth.track.EntityFilter.accept(hit, c) && HealthTracker.hitAge() <= Math.max(20, linger * 2);
        return switch (c.target.source) {
            case LOOK -> lookOk ? look : null;
            case HIT -> hitOk ? hit : null;
            case LOOK_OR_HIT -> lookOk ? look : hitOk ? hit : null;
        };
    }

    public int width(FhConfig c) {
        int bw = Math.max(40, c.target.look.width);
        return switch (c.target.style) {
            case FRAME -> (c.target.portrait ? 42 : 6) + bw + 6;
            case BOSSBAR -> bw;
            case COMPACT -> bw + 66;
        };
    }

    public int height(FhConfig c) {
        return switch (c.target.style) {
            case FRAME -> 40;
            case BOSSBAR -> 11 + Math.max(2, c.target.look.height) + 2;
            case COMPACT -> 14;
        };
    }

    public void draw(Canvas c, FhConfig cfg, BarModel m, LivingEntity entity, float dist, float a) {
        FhConfig.BarLook l = cfg.target.look;
        int w = width(cfg), h = height(cfg);
        int white = Colors.alpha(0xFFFFFFFF, a);
        int dim = Colors.alpha(0xFFB4B8C4, a);
        int bh = Math.max(2, l.height);
        Component name = l.showName && m.name != null ? BarRenderer.name(l, m) : null;
        int nameColor = Colors.alpha(l.nameColor, a);
        switch (cfg.target.style) {
            case FRAME -> {
                c.fill(0, 0, w, h, Colors.alpha(0xC8101218, a));
                c.fill(0, 0, w, 1, Colors.alpha(0x30FFFFFF, a));
                c.fill(0, 0, 2, h, Colors.alpha(m.relation, a));
                int bx = 6;
                if (cfg.target.portrait) {
                    c.fill(5, 4, 37, 36, Colors.alpha(0xFF07080B, a));
                    c.fill(5, 35, 37, 36, Colors.alpha(Colors.darken(m.relation, 0.3f), a));
                    if (entity != null && a > 0.6f) {
                        float bbh = Math.max(entity.getBbHeight(), entity.getBbWidth());
                        int size = (int) Math.max(4, 46f / Math.max(bbh, 0.4f));
                        c.scissor(5, 4, 37, 36);
                        c.entity(entity, 5, 4, 37, 36, size, bbh * 0.3f, 21f, 14f);
                        c.endScissor();
                    }
                    bx = 42;
                }
                int bw = w - bx - 6;
                if (name != null) {
                    c.push();
                    c.translate(bx, 13);
                    c.scale(BarRenderer.nameScale(l));
                    TextFx.draw(c, name, 0, -8, nameColor, l.nameEffect, 0xFF000000);
                    c.pop();
                }
                if (cfg.target.showDistance) {
                    Component d = Component.literal(BarRenderer.num(Math.round(dist * 10f) / 10f) + "m");
                    c.push();
                    c.translate(w - 5, 6);
                    c.scale(0.75f);
                    TextFx.draw(c, d, -TextFx.width(d), 0, dim, TextEffect.SHADOW, 0xFF000000);
                    c.pop();
                }
                int by0 = 17, by1 = by0 + Math.min(bh, 12);
                BarRenderer.bar(c, l, m, bx, by0, bx + bw, by1, a);
                BarRenderer.valueText(c, l, m, bx, by0, bx + bw, by1, a);
                int ry = by1 + 5;
                int rx = bx;
                if (l.showArmor && m.armor > 0) {
                    BarRenderer.icon(c, 7, rx, ry - 1, white);
                    small(c, Component.literal(String.valueOf(m.armor)), rx + 10, ry, dim, false);
                    rx += 26;
                }
                if (m.absorption > 0) {
                    BarRenderer.icon(c, 5, rx, ry - 1, white);
                    small(c, Component.literal(BarRenderer.num(m.absorption)), rx + 10, ry, dim, false);
                }
                if (l.valueText == ValueText.NONE || bh < 7) {
                    small(c, TextFx.styled(BarRenderer.value(ValueText.CURRENT_MAX, m, false), l.font), bx + bw, ry, white, true);
                }
            }
            case BOSSBAR -> {
                if (name != null) {
                    c.push();
                    c.translate(w / 2f, 8);
                    c.scale(BarRenderer.nameScale(l));
                    TextFx.centered(c, name, 0, -8, nameColor, l.nameEffect, 0xFF000000);
                    c.pop();
                }
                int by0 = 11, by1 = by0 + bh;
                BarRenderer.bar(c, l, m, 0, by0, w, by1, a);
                BarRenderer.valueText(c, l, m, 0, by0, w, by1, a);
            }
            case COMPACT -> {
                c.fill(0, 0, w, h, Colors.alpha(0xA8101218, a));
                c.fill(0, 0, 2, h, Colors.alpha(m.relation, a));
                if (name != null) {
                    c.push();
                    c.translate(5, 10);
                    c.scale(BarRenderer.nameScale(l));
                    small(c, name, 0, -6, nameColor, false);
                    c.pop();
                }
                int bx = 62, bw = w - bx - 4;
                int bhh = Math.min(bh, 8);
                int by0 = (h - bhh) / 2;
                BarRenderer.bar(c, l, m, bx, by0, bx + bw, by0 + bhh, a);
                BarRenderer.valueText(c, l, m, bx, by0, bx + bw, by0 + bhh, a);
            }
        }
    }

    private void small(Canvas c, Component t, int x, int y, int color, boolean right) {
        c.push();
        c.translate(x, y);
        c.scale(0.75f);
        TextFx.draw(c, t, right ? -TextFx.width(t) : 0, 0, color, TextEffect.SHADOW, 0xFF000000);
        c.pop();
    }
}
