package me.bbijabnpobatejb.fancyhealth.config;

import lombok.experimental.UtilityClass;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@UtilityClass
public class Presets {

    private final Map<String, Consumer<FhConfig>> PRESETS = new LinkedHashMap<>();

    static {
        PRESETS.put("classic", c -> {
        });

        PRESETS.put("mmo", c -> {
            c.numbers.showHeal = true;
            FhConfig.BarLook l = c.overhead.look;
            l.style = BarStyle.MMO;
            l.width = 54;
            l.height = 8;
            l.colorMode = ColorMode.RELATION;
            l.valueText = ValueText.CURRENT_MAX;
            l.font = FontChoice.ROUND;
            l.textEffect = TextEffect.OUTLINE;
            l.trailColor = 0xFFFFFFFF;
            c.overhead.anchor = Anchor.ABOVE_HEAD;
            c.overhead.showWhen = ShowWhen.ALWAYS;
            c.target.enabled = true;
            c.target.style = TargetStyle.FRAME;
            c.numbers.animation = Animation.RISE;
            c.numbers.critAnimation = Animation.ZOOM;
            c.numbers.font = FontChoice.ROUND;
            c.numbers.effect = TextEffect.OUTLINE;
            c.numbers.scale = 1.3f;
            c.numbers.critMark = CritMark.LABEL;
        });

        PRESETS.put("hearts", c -> {
            c.numbers.showHeal = true;
            c.numbers.mergeMillis = 200;
            FhConfig.BarLook l = c.overhead.look;
            l.style = BarStyle.HEARTS;
            l.showName = false;
            l.valueText = ValueText.NONE;
            c.overhead.anchor = Anchor.ABOVE_NAME;
            c.overhead.showWhen = ShowWhen.DAMAGED;
            c.numbers.animation = Animation.FOUNTAIN;
            c.numbers.critAnimation = Animation.FOUNTAIN;
            c.numbers.format = NumberFormat.HEARTS;
            c.numbers.font = FontChoice.CHUNKY;
            c.numbers.effect = TextEffect.SHADOW;
            c.numbers.scale = 1.1f;
            c.numbers.critMark = CritMark.NONE;
        });

        PRESETS.put("holo", c -> {
            c.numbers.showHeal = true;
            c.numbers.mergeMillis = 200;
            FhConfig.BarLook l = c.overhead.look;
            l.style = BarStyle.NEON;
            l.width = 46;
            l.height = 3;
            l.colorMode = ColorMode.FIXED;
            l.color = 0xFF3FE6FF;
            l.trailColor = 0xFFFF4FD8;
            l.font = FontChoice.DIGITAL;
            l.textEffect = TextEffect.GLOW;
            l.valueText = ValueText.CURRENT;
            c.overhead.anchor = Anchor.ABOVE_HEAD;
            c.numbers.animation = Animation.FOUNTAIN;
            c.numbers.critAnimation = Animation.SHAKE;
            c.numbers.font = FontChoice.DIGITAL;
            c.numbers.effect = TextEffect.GLOW;
            c.numbers.colorByType = false;
            c.numbers.damageColor = 0xFF56F0FF;
            c.numbers.critColor = 0xFFFF5CE1;
            c.numbers.format = NumberFormat.DECIMAL;
            c.numbers.critMark = CritMark.STAR;
            c.numbers.scale = 1.8f;
        });

        PRESETS.put("arcade", c -> {
            c.numbers.showHeal = true;
            c.numbers.mergeMillis = 200;
            FhConfig.BarLook l = c.overhead.look;
            l.style = BarStyle.SKEWED;
            l.width = 50;
            l.height = 6;
            l.font = FontChoice.CHUNKY;
            l.textEffect = TextEffect.THICK;
            l.valueText = ValueText.PERCENT;
            l.trailColor = 0xFFFFFFFF;
            c.crosshair.enabled = true;
            c.crosshair.look.style = BarStyle.SKEWED;
            c.crosshair.look.font = FontChoice.CHUNKY;
            c.numbers.animation = Animation.BOUNCE;
            c.numbers.critAnimation = Animation.ZOOM;
            c.numbers.font = FontChoice.CHUNKY;
            c.numbers.effect = TextEffect.THICK;
            c.numbers.scale = 1.7f;
            c.numbers.critScale = 1.6f;
            c.numbers.critMark = CritMark.EXCLAIM;
            c.numbers.format = NumberFormat.INTEGER;
        });

        PRESETS.put("minimal", c -> {
            c.numbers.showHeal = true;
            c.numbers.mergeMillis = 200;
            FhConfig.BarLook l = c.overhead.look;
            l.style = BarStyle.LINE;
            l.width = 30;
            l.height = 1;
            l.showName = false;
            l.valueText = ValueText.NONE;
            l.border = false;
            c.overhead.anchor = Anchor.BELOW_NAME;
            c.overhead.showWhen = ShowWhen.RECENT;
            c.numbers.animation = Animation.RISE;
            c.numbers.critAnimation = Animation.RISE;
            c.numbers.effect = TextEffect.SHADOW;
            c.numbers.scale = 1.0f;
            c.numbers.critMark = CritMark.NONE;
            c.numbers.colorByType = false;
            c.numbers.damageColor = 0xFFFFFFFF;
        });

        PRESETS.put("pvp", c -> {
            c.numbers.showHeal = true;
            c.numbers.mergeMillis = 200;
            FhConfig.BarLook l = c.overhead.look;
            l.style = BarStyle.SEGMENTED;
            l.width = 44;
            l.height = 5;
            l.segments = 10;
            l.showName = false;
            l.valueText = ValueText.CURRENT;
            l.showArmor = true;
            l.font = FontChoice.SLIM;
            l.textEffect = TextEffect.OUTLINE;
            c.overhead.anchor = Anchor.ABOVE_NAME;
            c.crosshair.enabled = true;
            c.crosshair.look.valueText = ValueText.CURRENT_MAX;
            c.crosshair.look.showArmor = true;
            c.numbers.animation = Animation.STACK;
            c.numbers.critAnimation = Animation.STACK;
            c.numbers.format = NumberFormat.DECIMAL;
            c.numbers.font = FontChoice.SLIM;
            c.numbers.scale = 1.1f;
            c.numbers.mergeMillis = 0;
            c.numbers.onlyMine = true;
        });

        PRESETS.put("boss", c -> {
            c.numbers.showHeal = true;
            c.numbers.mergeMillis = 200;
            c.overhead.enabled = false;
            c.target.enabled = true;
            c.target.style = TargetStyle.BOSSBAR;
            c.target.look.style = BarStyle.GLOSS;
            c.target.look.width = 182;
            c.target.look.height = 7;
            c.target.look.colorMode = ColorMode.HEALTH;
            c.target.look.valueText = ValueText.CURRENT_MAX;
            c.crosshair.enabled = true;
            c.crosshair.look.style = BarStyle.LINE;
            c.crosshair.look.height = 2;
            c.crosshair.look.showName = false;
            c.crosshair.look.valueText = ValueText.NONE;
            c.numbers.animation = Animation.SLIDE;
            c.numbers.font = FontChoice.ROUND;
            c.numbers.critAnimation = Animation.ZOOM;
        });

        PRESETS.put("comic", c -> {
            c.numbers.showHeal = true;
            c.numbers.mergeMillis = 200;
            FhConfig.BarLook l = c.overhead.look;
            l.style = BarStyle.ROUNDED;
            l.width = 44;
            l.height = 6;
            l.border = true;
            l.borderColor = 0xFF1A1A1A;
            l.font = FontChoice.COMIC;
            l.textEffect = TextEffect.THICK;
            l.valueText = ValueText.CURRENT;
            c.numbers.animation = Animation.SPIRAL;
            c.numbers.critAnimation = Animation.SHAKE;
            c.numbers.font = FontChoice.COMIC;
            c.numbers.effect = TextEffect.THICK;
            c.numbers.scale = 1.6f;
            c.numbers.critMark = CritMark.STAR;
        });
    }

    public List<String> ids() {
        return List.copyOf(PRESETS.keySet());
    }

    public FhConfig create(String id) {
        FhConfig c = new FhConfig();
        Consumer<FhConfig> p = PRESETS.get(id);
        if (p != null) p.accept(c);
        c.preset = id;
        c.base = id;
        return c;
    }

    public FhConfig apply(FhConfig current, String id) {
        FhConfig c = create(id);
        c.general = current.general;
        c.filters = current.filters;
        return c;
    }
}
