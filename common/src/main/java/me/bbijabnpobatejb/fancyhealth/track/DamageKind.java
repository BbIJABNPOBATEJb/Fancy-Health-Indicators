package me.bbijabnpobatejb.fancyhealth.track;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

public enum DamageKind {
    PHYSICAL(0xFFFF4B3E),
    PROJECTILE(0xFFFF7062),
    FIRE(0xFFFF8A1F),
    MAGIC(0xFFC86BFF),
    WITHER(0xFF9C9C9C),
    FALL(0xFFC7C7C7),
    EXPLOSION(0xFFFFB347),
    FREEZE(0xFF8FE3FF),
    DROWN(0xFF4F8DFF),
    LIGHTNING(0xFFFFF35C),
    THORNS(0xFF3FCF6E),
    VOID(0xFFA05CFF);

    public final int color;

    DamageKind(int color) {
        this.color = color;
    }

    public static DamageKind of(DamageSource s) {
        if (s.is(DamageTypeTags.IS_FIRE)) return FIRE;
        if (s.is(DamageTypeTags.IS_EXPLOSION)) return EXPLOSION;
        if (s.is(DamageTypeTags.IS_FALL)) return FALL;
        if (s.is(DamageTypeTags.IS_FREEZING)) return FREEZE;
        if (s.is(DamageTypeTags.IS_DROWNING)) return DROWN;
        if (s.is(DamageTypeTags.IS_LIGHTNING)) return LIGHTNING;
        if (s.is(DamageTypes.WITHER) || s.is(DamageTypes.WITHER_SKULL)) return WITHER;
        if (s.is(DamageTypes.THORNS) || s.is(DamageTypes.CACTUS) || s.is(DamageTypes.SWEET_BERRY_BUSH)) return THORNS;
        if (s.is(DamageTypes.FELL_OUT_OF_WORLD) || s.is(DamageTypes.OUTSIDE_BORDER)) return VOID;
        if (s.is(DamageTypes.MAGIC) || s.is(DamageTypes.INDIRECT_MAGIC) || s.is(DamageTypes.SONIC_BOOM) || s.is(DamageTypes.DRAGON_BREATH)) return MAGIC;
        if (s.is(DamageTypeTags.IS_PROJECTILE)) return PROJECTILE;
        return PHYSICAL;
    }
}
