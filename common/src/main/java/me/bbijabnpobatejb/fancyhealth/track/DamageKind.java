package me.bbijabnpobatejb.fancyhealth.track;

import net.minecraft.world.damagesource.DamageSource;
//? if >=1.19.4 {
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
//?}

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

    //? if >=1.19.4 {
    public static DamageKind of(DamageSource s) {
        if (s.is(DamageTypeTags.IS_FIRE)) return FIRE;
        if (s.is(DamageTypeTags.IS_EXPLOSION)) return EXPLOSION;
        if (s.is(DamageTypeTags.IS_FALL)) return FALL;
        if (s.is(DamageTypeTags.IS_FREEZING)) return FREEZE;
        if (s.is(DamageTypeTags.IS_DROWNING)) return DROWN;
        if (s.is(DamageTypeTags.IS_LIGHTNING)) return LIGHTNING;
        if (s.is(DamageTypes.WITHER) || s.is(DamageTypes.WITHER_SKULL)) return WITHER;
        if (s.is(DamageTypes.THORNS) || s.is(DamageTypes.CACTUS) || s.is(DamageTypes.SWEET_BERRY_BUSH)) return THORNS;
        if (s.is(DamageTypes.MAGIC) || s.is(DamageTypes.INDIRECT_MAGIC) || s.is(DamageTypes.SONIC_BOOM) || s.is(DamageTypes.DRAGON_BREATH)) return MAGIC;
        if (s.is(DamageTypeTags.IS_PROJECTILE)) return PROJECTILE;
        return byId(s.getMsgId());
    }
    //?} else {
    /*public static DamageKind of(DamageSource s) {
        if (s.isFire()) return FIRE;
        if (s.isExplosion()) return EXPLOSION;
        if (s.isMagic()) return MAGIC;
        if (s.isProjectile()) return PROJECTILE;
        return byId(s.getMsgId());
    }
    *///?}

    public static DamageKind ofEvent(byte id) {
        switch (id) {
            case 2:
                return PHYSICAL;
            case 33:
            case 44:
                return THORNS;
            case 36:
                return DROWN;
            case 37:
                return FIRE;
            case 57:
                return FREEZE;
            default:
                return null;
        }
    }

    private static DamageKind byId(String id) {
        if (id == null) return PHYSICAL;
        switch (id) {
            case "fall":
            case "flyIntoWall":
                return FALL;
            case "freeze":
                return FREEZE;
            case "drown":
                return DROWN;
            case "lightningBolt":
                return LIGHTNING;
            case "wither":
            case "witherSkull":
                return WITHER;
            case "thorns":
            case "cactus":
            case "sweetBerryBush":
                return THORNS;
            case "outOfWorld":
            case "outsideBorder":
                return VOID;
            case "magic":
            case "indirectMagic":
            case "dragonBreath":
            case "sonic_boom":
                return MAGIC;
            default:
                return PHYSICAL;
        }
    }
}
