package me.bbijabnpobatejb.fancyhealth.hud;

import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.render.BarModel;
import me.bbijabnpobatejb.fancyhealth.render.BarRenderer;
import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import me.bbijabnpobatejb.fancyhealth.track.BarAnim;
import me.bbijabnpobatejb.fancyhealth.track.EntityState;
import me.bbijabnpobatejb.fancyhealth.track.HealthTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

@UtilityClass
public class CrosshairHud {

    private final BarModel MODEL = new BarModel();
    private LivingEntity shown;
    private float alpha;

    public void render(Canvas canvas, Minecraft mc, FhConfig c, float dt) {
        LivingEntity look = HealthTracker.lookTarget();
        boolean fresh = look != null && HealthTracker.lookAge() <= Math.max(1, c.crosshair.lingerSeconds * 20f)
                && look.isAlive() && look.distanceTo(mc.player) <= c.crosshair.range + 1;
        if (fresh) shown = look;
        alpha = fresh ? Math.min(1f, alpha + dt * 8f) : Math.max(0f, alpha - dt * 5f);
        if (shown == null || alpha <= 0.01f) return;
        EntityState s = HealthTracker.state(shown);
        BarAnim anim = s != null ? s.anim : null;
        if (anim == null) {
            anim = new BarAnim();
            anim.reset(shown.getHealth());
        }
        if (s != null) MODEL.set(s);
        else MODEL.set(shown, anim);
        draw(canvas, c, MODEL, alpha, canvas.width() / 2f, canvas.height() / 2f);
    }

    public void draw(Canvas canvas, FhConfig c, BarModel m, float a, float cx, float cy) {
        FhConfig.BarLook l = c.crosshair.look;
        int ph = BarRenderer.plateHeight(l, m, false);
        canvas.push();
        canvas.translate(cx, cy + c.crosshair.offsetY);
        canvas.scale(c.crosshair.scale);
        canvas.translate(0, ph);
        BarRenderer.plate(canvas, l, m, a, true, true);
        canvas.pop();
    }
}
