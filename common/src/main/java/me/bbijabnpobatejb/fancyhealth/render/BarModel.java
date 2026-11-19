package me.bbijabnpobatejb.fancyhealth.render;

import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import me.bbijabnpobatejb.fancyhealth.track.BarAnim;
import me.bbijabnpobatejb.fancyhealth.track.EntityFilter;
import me.bbijabnpobatejb.fancyhealth.track.EntityState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class BarModel {

    public float health;
    public float max;
    public float shown;
    public float trail;
    public float absorption;
    public int armor;
    public float flash;
    public Component name;
    public int relation = EntityFilter.HOSTILE;
    public int team;
    public float time;
    public BarAnim anim;
    public int heartType;
    public boolean hardcore;

    public BarModel set(LivingEntity e, BarAnim anim) {
        health = e.getHealth();
        max = e.getMaxHealth();
        absorption = e.getAbsorptionAmount();
        armor = e.getArmorValue();
        name = e.getDisplayName();
        relation = EntityFilter.relation(e);
        team = EntityFilter.team(e);
        shown = anim.shown;
        trail = anim.trail;
        flash = anim.flash;
        this.anim = anim;
        time = (System.currentTimeMillis() % 100000L) / 1000f;
        heartType = FancyHealth.platform().heartType(e);
        hardcore = e.level().getLevelData().isHardcore();
        return this;
    }

    public BarModel set(EntityState s) {
        set(s.entity, s.anim);
        health = s.health;
        return this;
    }
}
