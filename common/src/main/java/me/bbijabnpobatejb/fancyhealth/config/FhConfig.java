package me.bbijabnpobatejb.fancyhealth.config;

import java.util.ArrayList;
import java.util.List;

public class FhConfig {

    public int version = 1;
    public String preset = "classic";
    public String base = "classic";
    public General general = new General();
    public Filters filters = new Filters();
    public Overhead overhead = new Overhead();
    public Crosshair crosshair = new Crosshair();
    public Target target = new Target();
    public Numbers numbers = new Numbers();

    public static class General {
        public boolean enabled = true;
        public boolean hideWithF1 = true;
        public Occlusion occlusion = Occlusion.DIM;
        public boolean selfInThirdPerson = false;
        public float fadeSpeed = 6f;
        public boolean serverNametags = true;
        public boolean tabHealth = true;
        public boolean uiSounds = true;
        public float uiVolume = 0.6f;
    }

    public static class Filters {
        public boolean players = true;
        public boolean hostile = true;
        public boolean neutral = true;
        public boolean passive = true;
        public boolean bosses = true;
        public boolean armorStands = false;
        public boolean npcs = false;
        public boolean npcNumbers = true;
        public List<String> blacklist = new ArrayList<>();
    }

    public static class BarLook {
        public BarStyle style = BarStyle.FLAT;
        public int width = 40;
        public int height = 4;
        public ColorMode colorMode = ColorMode.HEALTH;
        public int color = 0xFF46D35C;
        public int background = 0xB0101014;
        public boolean border = true;
        public int borderColor = 0xFF000000;
        public boolean trail = true;
        public int trailColor = 0xFFFFE27A;
        public boolean showName = true;
        public float nameScale = 1f;
        public FontChoice nameFont = FontChoice.DEFAULT;
        public int nameColor = 0xFFFFFFFF;
        public TextEffect nameEffect = TextEffect.SHADOW;
        public ValueText valueText = ValueText.CURRENT;
        public boolean showAbsorption = true;
        public boolean showArmor = false;
        public FontChoice font = FontChoice.DEFAULT;
        public TextEffect textEffect = TextEffect.SHADOW;
        public int segments = 10;
        public float trailHold = 0.6f;
        public float trailSpeed = 1f;
        public int flashColor = 0xFFFFFFFF;
        public boolean lowHpPulse = true;
        public boolean hitFlash = true;
    }

    public static class Overhead {
        public boolean enabled = true;
        public BarLook look = new BarLook();
        public Anchor anchor = Anchor.ABOVE_NAME;
        public float yOffset = 0f;
        public SizeMode sizeMode = SizeMode.WORLD;
        public float scale = 1f;
        public float maxDistance = 32f;
        public ShowWhen showWhen = ShowWhen.ALWAYS;
        public float lingerSeconds = 6f;
    }

    public static class Crosshair {
        public boolean enabled = false;
        public BarLook look = crosshairLook();
        public int offsetY = 14;
        public float scale = 1f;
        public float range = 24f;
        public float lingerSeconds = 0.8f;
    }

    public static class Target {
        public boolean enabled = false;
        public TargetStyle style = TargetStyle.FRAME;
        public HudPos position = HudPos.TOP_CENTER;
        public int offsetX = 0;
        public int offsetY = 4;
        public float scale = 1f;
        public boolean portrait = true;
        public boolean showDistance = true;
        public TargetSource source = TargetSource.LOOK_OR_HIT;
        public float range = 32f;
        public float lingerSeconds = 3f;
        public BarLook look = targetLook();
    }

    public static class Numbers {
        public boolean enabled = true;
        public Animation animation = Animation.SLIDE;
        public Animation critAnimation = Animation.SHAKE;
        public FontChoice font = FontChoice.DEFAULT;
        public TextEffect effect = TextEffect.OUTLINE;
        public int outlineColor = 0xFF000000;
        public float scale = 1.25f;
        public float critScale = 1.35f;
        public float distanceScaling = 0.7f;
        public float lifetime = 1.1f;
        public NumberFormat format = NumberFormat.SMART;
        public boolean colorByType = true;
        public int damageColor = 0xFFFF4B3E;
        public int critColor = 0xFFFFC21F;
        public int healColor = 0xFF5CFF7A;
        public boolean showHeal = false;
        public float minHeal = 0.5f;
        public CritMark critMark = CritMark.EXCLAIM;
        public int mergeMillis = 100;
        public boolean scaleByDamage = true;
        public boolean onlyMine = false;
        public boolean showSelf = false;
        public float maxDistance = 32f;
        public int maxCount = 80;
    }

    static BarLook crosshairLook() {
        BarLook l = new BarLook();
        l.style = BarStyle.GLOSS;
        l.width = 60;
        l.height = 5;
        return l;
    }

    static BarLook targetLook() {
        BarLook l = new BarLook();
        l.style = BarStyle.MMO;
        l.width = 120;
        l.height = 9;
        l.colorMode = ColorMode.RELATION;
        return l;
    }
}
