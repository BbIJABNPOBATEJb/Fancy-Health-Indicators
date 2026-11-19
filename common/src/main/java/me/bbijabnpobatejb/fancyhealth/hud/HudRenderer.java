package me.bbijabnpobatejb.fancyhealth.hud;

import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import me.bbijabnpobatejb.fancyhealth.config.Anchor;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.config.Occlusion;
import me.bbijabnpobatejb.fancyhealth.config.SizeMode;
import me.bbijabnpobatejb.fancyhealth.numbers.DamageNumbers;
import me.bbijabnpobatejb.fancyhealth.render.BarModel;
import me.bbijabnpobatejb.fancyhealth.render.BarRenderer;
import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import me.bbijabnpobatejb.fancyhealth.render.Projection;
import me.bbijabnpobatejb.fancyhealth.track.EntityFilter;
import me.bbijabnpobatejb.fancyhealth.track.EntityState;
import me.bbijabnpobatejb.fancyhealth.track.HealthTracker;
import me.bbijabnpobatejb.fancyhealth.track.NameTags;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@UtilityClass
public class HudRenderer {

    private long lastNanos;
    private float dt;
    private final BarModel MODEL = new BarModel();
    private final List<Entry> ENTRIES = new ArrayList<>();
    private final Vector3f POS = new Vector3f();

    private final NameTags.Layout LAYOUT = new NameTags.Layout();

    private record Entry(EntityState state, float x, float y, float depth, float dist, boolean nameShown, boolean below, boolean seeThrough) {
    }

    public float dt() {
        return dt;
    }

    private boolean active(Minecraft mc, FhConfig c) {
        if (!c.general.enabled || mc.level == null || mc.player == null) return false;
        return !(c.general.hideWithF1 && FancyHealth.platform().hudHidden());
    }

    public void renderWorld(Canvas canvas, float pt) {
        long now = System.nanoTime();
        dt = lastNanos == 0 ? 0 : Math.min(0.1f, (now - lastNanos) / 1e9f);
        lastNanos = now;
        Minecraft mc = Minecraft.getInstance();
        FhConfig c = FancyHealth.config();
        if (!active(mc, c) || !Projection.valid()) return;
        HealthTracker.frame(dt);
        canvas.push();
        canvas.identity();
        if (c.overhead.enabled) overhead(canvas, mc, c, pt);
        DamageNumbers.render(canvas, pt, dt);
        canvas.pop();
    }

    public void renderOverlay(Canvas canvas, float pt) {
        Minecraft mc = Minecraft.getInstance();
        FhConfig c = FancyHealth.config();
        if (!active(mc, c) || FancyHealth.platform().screen() instanceof me.bbijabnpobatejb.fancyhealth.ui.FhScreenBase) return;
        canvas.push();
        canvas.identity();
        if (c.crosshair.enabled) CrosshairHud.render(canvas, mc, c, dt);
        if (c.target.enabled) TargetHud.render(canvas, mc, c, dt);
        canvas.pop();
    }

    public double anchorY(LivingEntity e, double feet, Anchor anchor, NameTags.Layout name) {
        double head = feet + e.getBbHeight();
        return switch (anchor) {
            case ABOVE_NAME -> name.shown ? name.top + 0.06 : head + 0.14;
            case BELOW_NAME -> name.shown ? name.bottom - 0.03 : head + 0.14;
            case ABOVE_HEAD -> head + 0.14;
            case CHEST -> feet + e.getBbHeight() * 0.62;
        };
    }

    private void overhead(Canvas canvas, Minecraft mc, FhConfig c, float pt) {
        ENTRIES.clear();
        Vec3 cam = Projection.camera();
        float w = canvas.width(), h = canvas.height();
        for (EntityState s : HealthTracker.active()) {
            if (s.alpha <= 0.01f) continue;
            LivingEntity e = s.entity;
            if (!EntityFilter.accept(e, c)) continue;
            Vec3 p = e.getPosition(pt);
            NameTags.layout(e, pt, LAYOUT);
            double ay = anchorY(e, p.y, c.overhead.anchor, LAYOUT) + c.overhead.yOffset;
            boolean below = c.overhead.anchor == Anchor.BELOW_NAME && LAYOUT.shown;
            double dist = Math.sqrt(cam.distanceToSqr(p.x, ay, p.z));
            if (dist > c.overhead.maxDistance) continue;
            if (!Projection.project(p.x, ay, p.z, w, h, POS)) continue;
            if (POS.x < -80 || POS.x > w + 80 || POS.y < -60 || POS.y > h + 60) continue;
            ENTRIES.add(new Entry(s, POS.x, POS.y, POS.z, (float) dist, LAYOUT.shown, below, LAYOUT.seeThrough));
        }
        ENTRIES.sort(Comparator.comparingDouble(Entry::depth).reversed());
        for (Entry en : ENTRIES) {
            EntityState s = en.state;
            float alpha = s.alpha;
            float fadeStart = c.overhead.maxDistance * 0.8f;
            if (en.dist > fadeStart) alpha *= 1f - (en.dist - fadeStart) / (c.overhead.maxDistance - fadeStart);
            alpha *= Math.max(0f, s.occl);
            if (alpha <= 0.02f) continue;
            float f = c.overhead.scale;
            if (c.overhead.sizeMode == SizeMode.WORLD) f *= Projection.nearness(en.dist, 7.0, 0.3f, 3.2f);
            MODEL.set(s);
            boolean drawName = !en.nameShown;
            canvas.push();
            canvas.translate(en.x, en.y);
            canvas.scale(f);
            if (en.below) canvas.translate(0, BarRenderer.plateHeight(c.overhead.look, MODEL, drawName) + 1);
            BarRenderer.plate(canvas, c.overhead.look, MODEL, alpha, drawName, false);
            canvas.pop();
        }
    }
}
