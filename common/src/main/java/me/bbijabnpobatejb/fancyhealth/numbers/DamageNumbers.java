package me.bbijabnpobatejb.fancyhealth.numbers;

import me.bbijabnpobatejb.fancyhealth.compat.Mc;
import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import me.bbijabnpobatejb.fancyhealth.config.Animation;
import me.bbijabnpobatejb.fancyhealth.config.CritMark;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.config.NumberFormat;
import me.bbijabnpobatejb.fancyhealth.config.Occlusion;
import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import me.bbijabnpobatejb.fancyhealth.render.Colors;
import me.bbijabnpobatejb.fancyhealth.render.Projection;
import me.bbijabnpobatejb.fancyhealth.render.TextFx;
import me.bbijabnpobatejb.fancyhealth.track.DamageKind;
import me.bbijabnpobatejb.fancyhealth.track.EntityState;
import me.bbijabnpobatejb.fancyhealth.track.HealthTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@UtilityClass
public class DamageNumbers {

    private final List<DamageNumber> WORLD = new ArrayList<>();
    private final Random RANDOM = new Random();
    private final Animator.Frame FRAME = new Animator.Frame();
    private final float[] POS = new float[3];

    public void clear() {
        WORLD.clear();
    }

    public int count() {
        return WORLD.size();
    }

    public void tick() {
        FhConfig c = FancyHealth.config();
        while (WORLD.size() > c.numbers.maxCount) WORLD.remove(0);
    }

    public void spawn(LivingEntity e, float amount, float maxHp, DamageKind kind, boolean crit, boolean heal) {
        FhConfig c = FancyHealth.config();
        if (!c.general.enabled || !c.numbers.enabled || !me.bbijabnpobatejb.fancyhealth.api.FancyHealthApi.allowed(me.bbijabnpobatejb.fancyhealth.api.Restriction.NUMBERS)) return;
        if (!me.bbijabnpobatejb.fancyhealth.track.EntityFilter.acceptNumbers(e, c) && e != Minecraft.getInstance().player) return;
        DamageNumber n = create(WORLD, e.getId(), amount, maxHp, kind, crit, heal, c);
        if (n == null) return;
        double jitter = e.getBbWidth() * 0.5;
        n.x = e.getX() + (RANDOM.nextDouble() - 0.5) * jitter;
        n.y = e.getY() + e.getBbHeight() * (0.55 + RANDOM.nextDouble() * 0.25);
        n.z = e.getZ() + (RANDOM.nextDouble() - 0.5) * jitter;
    }

    public DamageNumber create(List<DamageNumber> list, int entityId, float amount, float maxHp, DamageKind kind, boolean crit, boolean heal, FhConfig c) {
        long now = System.currentTimeMillis();
        if (c.numbers.mergeMillis > 0) {
            for (int i = list.size() - 1; i >= 0; i--) {
                DamageNumber o = list.get(i);
                if (o.entityId == entityId && o.heal == heal && now - o.born <= c.numbers.mergeMillis) {
                    o.value += amount;
                    o.hits++;
                    o.crit |= crit;
                    o.bump = 1f;
                    o.born = now;
                    o.age = Math.min(o.age, 0.06f);
                    if (crit) o.animation = c.numbers.critAnimation;
                    return null;
                }
            }
        }
        DamageNumber n = new DamageNumber();
        n.entityId = entityId;
        n.value = amount;
        n.maxHp = maxHp;
        n.kind = kind;
        n.crit = crit;
        n.heal = heal;
        n.born = now;
        n.life = c.numbers.lifetime * (crit ? 1.15f : 1f);
        n.s1 = RANDOM.nextFloat() * 2 - 1;
        n.s2 = RANDOM.nextFloat() * 2 - 1;
        n.animation = crit ? c.numbers.critAnimation : c.numbers.animation;
        list.add(n);
        return n;
    }

    public void render(Canvas canvas, float pt, float dt) {
        FhConfig c = FancyHealth.config();
        if (!c.numbers.enabled || WORLD.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        Vec3 cam = Projection.camera();
        stackIndices(WORLD);
        for (int i = 0; i < WORLD.size(); i++) {
            DamageNumber n = WORLD.get(i);
            n.age += dt;
            n.bump = Math.max(0, n.bump - dt * 5f);
            if (n.age > n.life) {
                WORLD.remove(i--);
                continue;
            }
            double x = n.x, y = n.y, z = n.z;
            float occl = 1f;
            if (n.entityId >= 0 && mc.level != null) {
                Entity e = mc.level.getEntity(n.entityId);
                if (n.animation == Animation.STACK && e != null) {
                    Vec3 p = e.getPosition(pt);
                    x = p.x;
                    y = p.y + e.getBbHeight() * 0.85 + 0.1;
                    z = p.z;
                }
                if (e instanceof LivingEntity le && true) {
                    EntityState s = HealthTracker.state(le);
                    if (s != null && s.occl >= 0) occl = s.occl;
                }
            }
            if (occl <= 0.01f) continue;
            if (!Projection.project(x, y, z, canvas.width(), canvas.height(), POS)) continue;
            double dist = Math.sqrt(cam.distanceToSqr(x, y, z));
            float near = Projection.nearness(dist, 6.0, 0.35f, 3.2f);
            float factor = 1f + (near - 1f) * c.numbers.distanceScaling;
            draw(canvas, n, POS[0], POS[1], factor, occl, c);
        }
    }

    public void stackIndices(List<DamageNumber> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            DamageNumber n = list.get(i);
            if (n.animation != Animation.STACK) continue;
            int idx = 0;
            for (int j = list.size() - 1; j > i; j--) {
                DamageNumber o = list.get(j);
                if (o.animation == Animation.STACK && o.entityId == n.entityId) idx++;
            }
            n.stackIndex = idx;
        }
    }

    public void draw(Canvas canvas, DamageNumber n, float sx, float sy, float factor, float alphaMul, FhConfig c) {
        Animator.compute(n.animation, n.age, n.life, n.s1, n.s2, n.stackIndex, FRAME);
        float alpha = FRAME.alpha * alphaMul;
        if (alpha <= 0.02f) return;
        float scale = c.numbers.scale * FRAME.scale * factor * (1f + 0.35f * n.bump);
        if (n.crit) scale *= c.numbers.critScale;
        if (c.numbers.scaleByDamage && !n.heal && n.maxHp > 0) {
            scale *= 0.85f + 0.5f * Math.min(1f, n.value / n.maxHp);
        }
        int color = n.heal ? c.numbers.healColor : n.crit ? c.numbers.critColor : c.numbers.colorByType ? n.kind.color : c.numbers.damageColor;
        color = Colors.alpha(color, alpha);
        Component text = text(n, c);
        int w = TextFx.width(text);
        canvas.push();
        canvas.translate(sx + FRAME.dx * factor, sy + FRAME.dy * factor);
        if (FRAME.rot != 0) canvas.rotate(FRAME.rot);
        canvas.scale(scale);
        TextFx.draw(canvas, text, -w / 2, -4, color, c.numbers.effect, c.numbers.outlineColor);
        if (n.crit && c.numbers.critMark == CritMark.LABEL) {
            canvas.push();
            canvas.translate(0, -7);
            canvas.scale(0.55f);
            Component label = TextFx.styled(Mc.translatable("fancyhealth.crit").getString(), c.numbers.font);
            TextFx.draw(canvas, label, -TextFx.width(label) / 2, -4, color, c.numbers.effect, c.numbers.outlineColor);
            canvas.pop();
        }
        if (n.hits >= 3) {
            canvas.push();
            canvas.translate(w / 2f + 1, 1);
            canvas.scale(0.55f);
            TextFx.draw(canvas, TextFx.styled("x" + n.hits, c.numbers.font), 0, 0, Colors.alpha(0xFFFFFFFF, alpha), c.numbers.effect, c.numbers.outlineColor);
            canvas.pop();
        }
        canvas.pop();
    }

    public Component text(DamageNumber n, FhConfig c) {
        String s = format(n.value, n.maxHp, c.numbers.format);
        if (n.heal) s = "+" + s;
        if (n.crit && !n.heal) {
            if (c.numbers.critMark == CritMark.EXCLAIM) s = s + "!";
            else if (c.numbers.critMark == CritMark.STAR) s = "✦" + s;
        }
        MutableComponent comp = TextFx.styled(s, c.numbers.font);
        return comp;
    }

    public String format(float v, float max, NumberFormat f) {
        return switch (f) {
            case INTEGER -> String.valueOf(Math.max(1, Math.round(v)));
            case DECIMAL -> trim(String.format(java.util.Locale.ROOT, "%.1f", v));
            case HEARTS -> trim(String.format(java.util.Locale.ROOT, "%.1f", v / 2f)) + "❤";
            case PERCENT -> Math.max(1, Math.round(v / Math.max(max, 0.001f) * 100f)) + "%";
            case SMART -> v >= 10 || Math.abs(v - Math.round(v)) < 0.05f ? String.valueOf(Math.max(1, Math.round(v))) : trim(String.format(java.util.Locale.ROOT, "%.1f", v));
        };
    }

    private String trim(String s) {
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }
}
