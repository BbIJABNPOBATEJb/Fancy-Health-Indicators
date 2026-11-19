package me.bbijabnpobatejb.fancyhealth.compat;

import lombok.experimental.UtilityClass;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntityType;
//? if >=1.21.9 {
import net.minecraft.network.chat.FontDescription;
//?}
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
//? if >=1.19.3 {
import net.minecraft.core.registries.BuiltInRegistries;
//?} else {
/*import net.minecraft.core.Registry;
*///?}

@UtilityClass
public class Mc {

    //? if >=1.21.11 {
    public Identifier id(String id) {
        return Identifier.parse(id);
    }
    //?} else if >=1.21 {
    /*public ResourceLocation id(String id) {
        return ResourceLocation.parse(id);
    }
    *///?} else {
    /*public ResourceLocation id(String id) {
        return new ResourceLocation(id);
    }
    *///?}

    public MutableComponent literal(String text) {
        //? if >=1.19 {
        return Component.literal(text);
        //?} else {
        /*return new net.minecraft.network.chat.TextComponent(text);
        *///?}
    }

    public MutableComponent translatable(String key, Object... args) {
        //? if >=1.19 {
        return Component.translatable(key, args);
        //?} else {
        /*return new net.minecraft.network.chat.TranslatableComponent(key, args);
        *///?}
    }

    public Style font(String id) {
        return withFont(Style.EMPTY, id);
    }

    public Style withFont(Style style, String id) {
        //? if >=1.21.9 {
        return style.withFont(new FontDescription.Resource(id(id)));
        //?} else {
        /*return style.withFont(id(id));
        *///?}
    }

    public String fontId(Style style) {
        //? if >=1.21.9 {
        return style.getFont() instanceof FontDescription.Resource r ? r.id().toString() : null;
        //?} else {
        /*return style.getFont() == null ? null : style.getFont().toString();
        *///?}
    }

    public EntityType<?> entityType(String id) {
        //? if >=1.21.2 {
        return BuiltInRegistries.ENTITY_TYPE.getValue(id(id));
        //?} else if >=1.19.3 {
        /*return BuiltInRegistries.ENTITY_TYPE.get(id(id));
        *///?} else {
        /*return Registry.ENTITY_TYPE.get(id(id));
        *///?}
    }

    public String entityTypeId(EntityType<?> type) {
        //? if >=1.19.3 {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        //?} else {
        /*return Registry.ENTITY_TYPE.getKey(type).toString();
        *///?}
    }

    public int[] legacyHeart(String id) {
        String name = id.substring(id.lastIndexOf('/') + 1);
        if (name.equals("container")) return new int[]{16, 0};
        boolean half = name.contains("half");
        boolean blink = name.endsWith("_blinking");
        int v = name.startsWith("hardcore_") ? 45 : 0;
        int u = 52;
        if (name.startsWith("poisoned_")) u = 88;
        else if (name.startsWith("withered_")) u = 124;
        else if (name.startsWith("absorbing_")) u = 160;
        else if (name.startsWith("frozen_")) u = 178;
        else if (blink) u = 70;
        return new int[]{u + (half ? 9 : 0), v};
    }

    public <T extends net.minecraft.world.entity.Entity> T create(EntityType<T> type, net.minecraft.world.level.Level level) {
        //? if >=1.21.2 {
        return type.create(level, net.minecraft.world.entity.EntitySpawnReason.LOAD);
        //?} else {
        /*return type.create(level);
        *///?}
    }

    public net.minecraft.world.level.Level level(net.minecraft.world.entity.Entity entity) {
        //? if >=1.20 {
        return entity.level();
        //?} else {
        /*return entity.level;
        *///?}
    }

    public net.minecraft.world.scores.Objective belowName(net.minecraft.world.scores.Scoreboard board) {
        //? if >=1.20.2 {
        return board.getDisplayObjective(net.minecraft.world.scores.DisplaySlot.BELOW_NAME);
        //?} else {
        /*return board.getDisplayObjective(2);
        *///?}
    }

    public float score(net.minecraft.world.scores.Scoreboard board, net.minecraft.world.entity.Entity entity, net.minecraft.world.scores.Objective objective) {
        //? if >=1.20.3 {
        net.minecraft.world.scores.ReadOnlyScoreInfo info = board.getPlayerScoreInfo(entity, objective);
        return info == null ? Float.NaN : info.value();
        //?} else {
        /*String name = entity.getScoreboardName();
        return board.hasPlayerScore(name, objective) ? board.getOrCreatePlayerScore(name, objective).getScore() : Float.NaN;
        *///?}
    }

    public net.minecraft.sounds.SoundEvent sound(String id) {
        //? if >=1.19.3 {
        return net.minecraft.sounds.SoundEvent.createVariableRangeEvent(id(id));
        //?} else {
        /*return new net.minecraft.sounds.SoundEvent(id(id));
        *///?}
    }

    public boolean removed(net.minecraft.world.entity.Entity entity) {
        //? if >=1.17 {
        return entity.isRemoved();
        //?} else {
        /*return entity.removed;
        *///?}
    }

    public net.minecraft.world.phys.Vec3 eye(net.minecraft.world.entity.Entity entity) {
        //? if >=1.17 {
        return entity.getEyePosition();
        //?} else {
        /*return entity.getEyePosition(1f);
        *///?}
    }
}
