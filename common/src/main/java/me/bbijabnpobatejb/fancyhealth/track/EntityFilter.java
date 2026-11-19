package me.bbijabnpobatejb.fancyhealth.track;

import me.bbijabnpobatejb.fancyhealth.compat.Mc;
import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

@UtilityClass
public class EntityFilter {

    public static final int PLAYER = 0xFF4FA3FF;
    public static final int ALLY = 0xFF5BE37A;
    public static final int HOSTILE = 0xFFFF4545;
    public static final int NEUTRAL = 0xFFFFC23D;
    public static final int PASSIVE = 0xFF55E06A;
    public static final int BOSS = 0xFFB65CFF;

    public boolean isBoss(LivingEntity e) {
        return e instanceof EnderDragon || e instanceof WitherBoss || e instanceof ElderGuardian || Mc.entityTypeId(e.getType()).equals("minecraft:warden");
    }

    public boolean accept(LivingEntity e, FhConfig c) {
        Minecraft mc = Minecraft.getInstance();
        if (e instanceof ArmorStand) return c.filters.armorStands;
        if (e.isInvisible() || e.isSpectator()) return false;
        if (!c.filters.blacklist.isEmpty() && c.filters.blacklist.contains(Mc.entityTypeId(e.getType()))) return false;
        if (e == mc.player) return c.general.selfInThirdPerson && !mc.options.getCameraType().isFirstPerson();
        if (isNpc(e)) return c.filters.npcs;
        if (e instanceof Player) return c.filters.players && me.bbijabnpobatejb.fancyhealth.api.FancyHealthApi.allowed(me.bbijabnpobatejb.fancyhealth.api.Restriction.PLAYERS);
        if (isBoss(e)) return c.filters.bosses;
        if (e instanceof Enemy) return c.filters.hostile;
        if (e instanceof NeutralMob) return c.filters.neutral;
        return c.filters.passive;
    }

    public boolean acceptNumbers(LivingEntity e, FhConfig c) {
        if (!e.isInvisible() && !e.isSpectator() && e != Minecraft.getInstance().player && isNpc(e)) return c.filters.npcNumbers;
        return accept(e, c);
    }

    public boolean isNpc(LivingEntity e) {
        if (Mc.entityTypeId(e.getType()).equals("minecraft:mannequin")) return true;
        if (!(e instanceof Player) || e == Minecraft.getInstance().player) return false;
        if (e.getUUID().version() == 2) return true;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return false;
        PlayerInfo info = connection.getPlayerInfo(e.getUUID());
        if (info == null) return true;
        //? if >=1.19.3 {
        for (PlayerInfo listed : connection.getListedOnlinePlayers()) {
        //?} else {
        /*for (PlayerInfo listed : connection.getOnlinePlayers()) {
        *///?}
            if (listed == info) return false;
        }
        return true;
    }

    public int relation(LivingEntity e) {
        Minecraft mc = Minecraft.getInstance();
        if (e instanceof Player) {
            Team team = e.getTeam();
            if (mc.player != null && team != null && team.isAlliedTo(mc.player.getTeam())) return ALLY;
            return PLAYER;
        }
        if (isBoss(e)) return BOSS;
        if (e instanceof Enemy) return HOSTILE;
        if (e instanceof NeutralMob) return NEUTRAL;
        return PASSIVE;
    }

    public int team(LivingEntity e) {
        return e.getTeam() != null ? 0xFF000000 | e.getTeamColor() : 0;
    }
}
