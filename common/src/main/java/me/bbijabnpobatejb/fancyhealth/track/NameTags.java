package me.bbijabnpobatejb.fancyhealth.track;

import me.bbijabnpobatejb.fancyhealth.compat.Mc;
import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.Locale;
//? if >=1.19.4 {
import net.minecraft.world.entity.Display;
//?}

@UtilityClass
public class NameTags {

    private final double NAME_BOTTOM = 0.275;
    private final double NAME_TOP = 0.5;
    private final double SCORE_SHIFT = 9 * 1.15 * 0.025;

    public static final class Layout {
        public boolean shown;
        public boolean server;
        public boolean seeThrough;
        public double bottom;
        public double top;
    }

    public Layout layout(LivingEntity e, float pt, Layout out) {
        out.shown = false;
        out.server = false;
        out.seeThrough = false;
        double base = e.getPosition(pt).y + e.getBbHeight();
        out.bottom = out.top = base;
        FhConfig c = FancyHealth.config();
        if (e == Minecraft.getInstance().player) return out;
        if (c.general.serverNametags && serverStack(e, pt, out)) {
            out.shown = true;
            out.server = true;
            return out;
        }
        if (!vanillaVisible(e)) return out;
        out.shown = true;
        out.seeThrough = !e.isDiscrete();
        out.bottom = base + NAME_BOTTOM;
        out.top = base + NAME_TOP + (belowName(e) != null ? SCORE_SHIFT : 0);
        return out;
    }

    //? if >=1.20 {
    private boolean serverStack(LivingEntity e, float pt, Layout out) {
        double top = Double.NEGATIVE_INFINITY, bottom = Double.POSITIVE_INFINITY;
        for (Entity p : e.getIndirectPassengers()) {
            if (!(p instanceof Display.TextDisplay td)) continue;
            Display.TextDisplay.TextRenderState text = td.textRenderState();
            Display.RenderState render = td.renderState();
            if (text == null || render == null) continue;
            int opacity = text.textOpacity().get(pt) & 0xFF;
            String s = text.text().getString();
            byte flags = text.flags();
            com.mojang.math.Transformation transform = render.transformation().get(pt);
            if (opacity != 0 && opacity < 26) continue;
            if (s.isBlank()) continue;
            int lines = 1;
            for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '\n') lines++;
            if ((flags & Display.TextDisplay.FLAG_SEE_THROUGH) != 0) out.seeThrough = true;
            float[] t = FancyHealth.platform().displayOffset(transform);
            double y = p.getPosition(pt).y + t[0];
            top = Math.max(top, y + lines * 10 * 0.025 * t[1] + 0.02);
            bottom = Math.min(bottom, y);
        }
        if (top == Double.NEGATIVE_INFINITY) return false;
        out.top = top;
        out.bottom = bottom;
        return true;
    }
    //?} else if >=1.19.4 {
    /*private boolean serverStack(LivingEntity e, float pt, Layout out) {
        double top = Double.NEGATIVE_INFINITY, bottom = Double.POSITIVE_INFINITY;
        for (Entity p : e.getIndirectPassengers()) {
            if (!(p instanceof Display.TextDisplay td)) continue;
            int opacity = td.getTextOpacity(pt) & 0xFF;
            String s = td.getText().getString();
            byte flags = td.getFlags();
            com.mojang.math.Transformation transform = td.transformation(pt);
            if (opacity != 0 && opacity < 26) continue;
            if (s.isBlank()) continue;
            int lines = 1;
            for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '\n') lines++;
            if ((flags & Display.TextDisplay.FLAG_SEE_THROUGH) != 0) out.seeThrough = true;
            float[] t = FancyHealth.platform().displayOffset(transform);
            double y = p.getPosition(pt).y + t[0];
            top = Math.max(top, y + lines * 10 * 0.025 * t[1] + 0.02);
            bottom = Math.min(bottom, y);
        }
        if (top == Double.NEGATIVE_INFINITY) return false;
        out.top = top;
        out.bottom = bottom;
        return true;
    }
    *///?} else {
    /*private boolean serverStack(LivingEntity e, float pt, Layout out) {
        return false;
    }
    *///?}

    public boolean seenThroughWalls(LivingEntity e, float pt) {
        return layout(e, pt, new Layout()).seeThrough;
    }

    public boolean vanillaVisible(LivingEntity e) {
        Minecraft mc = Minecraft.getInstance();
        if (e instanceof Player) {
            if (e == mc.player || e.isInvisible()) return false;
            Team team = e.getTeam();
            if (team == null || mc.player == null) return true;
            return switch (team.getNameTagVisibility()) {
                case NEVER -> false;
                case HIDE_FOR_OTHER_TEAMS -> team.isAlliedTo(mc.player.getTeam());
                case HIDE_FOR_OWN_TEAM -> !team.isAlliedTo(mc.player.getTeam());
                default -> true;
            };
        }
        return e.hasCustomName() && e.isCustomNameVisible();
    }

    public Objective belowName(LivingEntity e) {
        if (!(e instanceof Player) || Mc.level(e) == null) return null;
        return Mc.belowName(Mc.level(e).getScoreboard());
    }

    public float scoreHealth(LivingEntity e) {
        Objective o = belowName(e);
        if (o == null || !looksLikeHealth(o)) return Float.NaN;
        return Mc.score(Mc.level(e).getScoreboard(), e, o);
    }

    private boolean looksLikeHealth(Objective o) {
        if (o.getCriteria() == ObjectiveCriteria.HEALTH || o.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) return true;
        String t = (o.getDisplayName().getString() + " " + o.getName()).toLowerCase(Locale.ROOT);
        return t.contains("health") || t.contains("hp") || t.contains("❤") || t.contains("здоров");
    }
}
