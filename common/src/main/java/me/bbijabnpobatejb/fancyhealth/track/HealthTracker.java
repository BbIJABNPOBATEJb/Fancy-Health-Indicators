package me.bbijabnpobatejb.fancyhealth.track;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.config.ShowWhen;
import me.bbijabnpobatejb.fancyhealth.numbers.DamageNumbers;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class HealthTracker {

    private final Int2ObjectOpenHashMap<EntityState> STATES = new Int2ObjectOpenHashMap<>();
    private final List<EntityState> ACTIVE = new ArrayList<>();
    private int tick;
    private LivingEntity look;
    private int lookTick = -100000;
    private LivingEntity hit;
    private int hitTick = -100000;

    public int now() {
        return tick;
    }

    public Collection<EntityState> active() {
        return ACTIVE;
    }

    public EntityState state(LivingEntity e) {
        return STATES.get(e.getId());
    }

    public LivingEntity lookTarget() {
        return look;
    }

    public int lookAge() {
        return tick - lookTick;
    }

    public LivingEntity hitTarget() {
        return hit;
    }

    public int hitAge() {
        return tick - hitTick;
    }

    public void clear() {
        STATES.clear();
        ACTIVE.clear();
        look = null;
        hit = null;
    }

    public void tick(Minecraft mc) {
        tick++;
        if (mc.level == null || mc.player == null) {
            clear();
            DamageNumbers.clear();
            return;
        }
        FhConfig c = FancyHealth.config();
        double range = Math.max(Math.max(c.overhead.maxDistance, c.numbers.maxDistance), Math.max(c.target.range, c.crosshair.range)) + 4;
        Vec3 cam = FancyHealth.platform().cameraPos();
        ACTIVE.clear();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity e)) continue;
            if (e.distanceToSqr(cam) > range * range) continue;
            EntityState s = STATES.get(e.getId());
            if (s == null || s.entity != e) {
                s = new EntityState();
                s.entity = e;
                s.visible = canSee(mc, cam, e);
                STATES.put(e.getId(), s);
            }
            s.seenTick = tick;
            update(mc, c, e, s);
            if (((tick + e.getId()) & 1) == 0) {
                s.visible = canSee(mc, cam, e);
            }
            ACTIVE.add(s);
        }
        if (tick % 40 == 0) STATES.values().removeIf(s -> tick - s.seenTick > 40);
        updateLook(mc, c);
        if (hit != null && (hit.isRemoved() || tick - hitTick > 20 * 60)) hit = null;
    }

    private void update(Minecraft mc, FhConfig c, LivingEntity e, EntityState s) {
        boolean wasScore = s.fromScore;
        float hp = health(e, c, s);
        float abs = e.getAbsorptionAmount();
        s.max = e.getMaxHealth();
        if (!s.init || wasScore != s.fromScore) {
            s.init = true;
            s.health = hp;
            s.absorption = abs;
            s.anim.reset(hp);
            return;
        }
        float delta = (hp + abs) - (s.health + s.absorption);
        s.health = hp;
        s.absorption = abs;
        s.anim.set(hp);
        if (Math.abs(delta) < 0.01f) return;
        s.changeTick = tick;
        boolean self = e == mc.player;
        if (self && !c.numbers.showSelf) return;
        if (Math.sqrt(e.distanceToSqr(FancyHealth.platform().cameraPos())) > c.numbers.maxDistance) return;
        boolean mine = tick - s.mineTick <= 6;
        if (delta < 0) {
            if (c.numbers.onlyMine && !mine && !self) return;
            DamageKind kind = tick - s.kindTick <= 6 ? s.kind : DamageKind.PHYSICAL;
            DamageNumbers.spawn(e, -delta, s.max, kind, tick - s.critTick <= 6, false);
        } else if (c.numbers.showHeal && delta >= c.numbers.minHeal) {
            if (c.numbers.onlyMine && !self && tick - s.mineTick > 200) return;
            DamageNumbers.spawn(e, delta, s.max, DamageKind.PHYSICAL, false, true);
        }
    }

    public float health(LivingEntity e, FhConfig c, EntityState s) {
        float hp = e.getHealth();
        if (!c.general.tabHealth || !(e instanceof Player) || e == Minecraft.getInstance().player) {
            s.fromScore = false;
            return hp;
        }
        float score = NameTags.scoreHealth(e);
        if (Float.isNaN(score)) {
            s.fromScore = false;
            return hp;
        }
        if (Math.abs(score - hp) > 1.01f) s.fromScore = true;
        if (s.fromScore) hp = Math.min(score, e.getMaxHealth());
        return hp;
    }

    public void onDamageEvent(LivingEntity e, DamageSource source) {
        EntityState s = STATES.get(e.getId());
        if (s == null) return;
        s.kind = DamageKind.of(source);
        s.kindTick = tick;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (source.getEntity() == mc.player || source.getDirectEntity() == mc.player)) {
            s.mineTick = tick;
            hit = e;
            hitTick = tick;
        }
    }

    public void onCrit(Entity e) {
        EntityState s = STATES.get(e.getId());
        if (s != null) s.critTick = tick;
    }

    public void onLocalAttack(Entity e) {
        if (!(e instanceof LivingEntity le)) return;
        EntityState s = STATES.get(le.getId());
        if (s != null) s.mineTick = tick;
        hit = le;
        hitTick = tick;
    }

    private boolean canSee(Minecraft mc, Vec3 cam, LivingEntity e) {
        Vec3 eye = e.getEyePosition();
        if (clear(mc, cam, eye)) return true;
        return clear(mc, cam, new Vec3(e.getX(), e.getY() + e.getBbHeight() + 0.3, e.getZ()));
    }

    private boolean clear(Minecraft mc, Vec3 from, Vec3 to) {
        return mc.level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, mc.player)).getType() == HitResult.Type.MISS;
    }

    private void updateLook(Minecraft mc, FhConfig c) {
        double range = Math.max(c.crosshair.range, c.target.range);
        Entity camEntity = mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player;
        Vec3 eye = camEntity.getEyePosition();
        Vec3 dir = camEntity.getViewVector(1f);
        Vec3 end = eye.add(dir.scale(range));
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (EntityState s : ACTIVE) {
            LivingEntity e = s.entity;
            if (e == camEntity || !e.isAlive() || e.isSpectator()) continue;
            if (!EntityFilter.accept(e, c)) continue;
            AABB box = e.getBoundingBox().inflate(0.15);
            Optional<Vec3> p = box.clip(eye, end);
            if (box.contains(eye)) p = Optional.of(eye);
            if (p.isEmpty()) continue;
            double d = eye.distanceToSqr(p.get());
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        if (best != null) {
            Vec3 hitPos = eye.add(dir.scale(Math.sqrt(bestDist)));
            if (!clear(mc, eye, hitPos)) best = null;
        }
        if (best != null) {
            look = best;
            lookTick = tick;
            EntityState s = STATES.get(best.getId());
            if (s != null) s.lookTick = tick;
        } else if (look != null && (look.isRemoved() || tick - lookTick > 200)) {
            look = null;
        }
    }

    public void frame(float dt) {
        FhConfig c = FancyHealth.config();
        float linger = c.overhead.lingerSeconds * 20f;
        for (EntityState s : ACTIVE) {
            s.anim.update(dt);
            float occTarget = s.visible ? 1f : c.general.occlusion == me.bbijabnpobatejb.fancyhealth.config.Occlusion.HIDE || !me.bbijabnpobatejb.fancyhealth.api.FancyHealthApi.allowed(me.bbijabnpobatejb.fancyhealth.api.Restriction.WALLS)
                    || !NameTags.seenThroughWalls(s.entity, 1f) ? 0f : 0.35f;
            if (s.occl < 0) s.occl = occTarget;
            float step = dt * 2f;
            s.occl = s.occl < occTarget ? Math.min(occTarget, s.occl + step) : Math.max(occTarget, s.occl - step);
            LivingEntity e = s.entity;
            boolean show = switch (c.overhead.showWhen) {
                case ALWAYS -> true;
                case DAMAGED -> s.health < s.max - 0.01f || tick - s.changeTick < linger;
                case RECENT -> tick - s.changeTick < linger || tick - s.lookTick < 10;
                case LOOKING -> tick - s.lookTick < 6;
            };
            if (c.overhead.showWhen == ShowWhen.DAMAGED && s.health >= s.max - 0.01f && s.anim.trail <= s.anim.shown + 0.01f && tick - s.changeTick >= 40) {
                show = false;
            }
            if (e.isRemoved() || e.deathTime > 0) show = false;
            float target = show ? 1f : 0f;
            float speed = c.general.fadeSpeed * dt;
            if (e.deathTime > 0) speed *= 0.6f;
            s.alpha = s.alpha < target ? Math.min(target, s.alpha + speed) : Math.max(target, s.alpha - speed);
        }
    }
}
