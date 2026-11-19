package me.bbijabnpobatejb.fancyhealth.ui;

import me.bbijabnpobatejb.fancyhealth.compat.Mc;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import me.bbijabnpobatejb.fancyhealth.config.Anchor;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.config.HudPos;
import me.bbijabnpobatejb.fancyhealth.config.ShowWhen;
import me.bbijabnpobatejb.fancyhealth.config.SizeMode;
import me.bbijabnpobatejb.fancyhealth.hud.CrosshairHud;
import me.bbijabnpobatejb.fancyhealth.hud.TargetHud;
import me.bbijabnpobatejb.fancyhealth.numbers.DamageNumber;
import me.bbijabnpobatejb.fancyhealth.numbers.DamageNumbers;
import me.bbijabnpobatejb.fancyhealth.render.BarModel;
import me.bbijabnpobatejb.fancyhealth.render.BarRenderer;
import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import me.bbijabnpobatejb.fancyhealth.track.BarAnim;
import me.bbijabnpobatejb.fancyhealth.track.DamageKind;
import me.bbijabnpobatejb.fancyhealth.track.EntityFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Preview {

    private static final String[] GALLERY = {"player", "minecraft:zombie", "minecraft:iron_golem", "minecraft:pig", "minecraft:wither", "minecraft:mannequin", "minecraft:armor_stand", "self"};
    private static final String[] GALLERY_KEYS = {"players", "hostile", "neutral", "passive", "bosses", "npcs", "armor_stands", "self_third_person"};

    final Random random = new Random();
    final BarAnim anim = new BarAnim();
    final BarModel model = new BarModel();
    final List<DamageNumber> numbers = new ArrayList<>();
    final List<LivingEntity> dummies = new ArrayList<>();
    final List<LivingEntity> gallery = new ArrayList<>();
    final List<float[]> scheduled = new ArrayList<>();
    ClientLevel dummyLevel;
    int index;
    float hp = 20;
    float max = 20;
    float absorption;
    float timer = 0.4f;
    float dead;
    float clock;
    float lastChange = -100;
    float plateAlpha = 1;
    int events;
    public boolean stage = true;
    public static boolean frozen;
    public static float freezeAge = 0.2f;
    public boolean changed;
    final float[][] cells = new float[GALLERY.length][];
    final float[] cellHover = new float[GALLERY.length];
    double lastMx;
    double lastMy;

    float x0;
    float y0;
    float x1;
    float y1;
    float entityX0;
    float entityY0;
    float entityX1;
    float entityY1;

    public Preview() {
        anim.reset(hp);
    }

    private LivingEntity create(String id, int n) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        if (id.equals("player") || id.equals("self")) return mc.player;
        try {
            EntityType<?> type = Mc.entityType(id);
            if (type == null) return id.equals("minecraft:mannequin") ? mc.player : null;
            Entity e = Mc.create(type, mc.level);
            if (e instanceof LivingEntity le) {
                le.setId(-2000 - n);
                return le;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void ensure() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || dummyLevel == mc.level) return;
        dummyLevel = mc.level;
        dummies.clear();
        gallery.clear();
        for (EntityType<? extends LivingEntity> t : FancyHealth.platform().previewTypes()) {
            try {
                LivingEntity e = Mc.create(t, mc.level);
                if (e != null) {
                    e.setId(-1000 - dummies.size());
                    dummies.add(e);
                }
            } catch (Exception ignored) {
            }
        }
        if (mc.player != null) dummies.add(mc.player);
        for (int i = 0; i < GALLERY.length; i++) gallery.add(create(GALLERY[i], i));
    }

    private LivingEntity entity() {
        ensure();
        if (dummies.isEmpty()) return null;
        return dummies.get(Math.floorMod(index, dummies.size()));
    }

    private void cycle(int dir) {
        index += dir;
        LivingEntity e = entity();
        max = e != null && e != Minecraft.getInstance().player ? Math.max(1f, e.getMaxHealth()) : 20f;
        hp = max;
        absorption = 0;
        anim.reset(hp);
        numbers.clear();
        Sounds.play("click");
    }

    private void freeze(FhConfig c) {
        LivingEntity e = entity();
        max = e != null && e != Minecraft.getInstance().player ? Math.max(1f, e.getMaxHealth()) : 20f;
        clock = 10f;
        lastChange = 9.9f;
        hp = max * 0.22f;
        absorption = 3;
        anim.freeze(hp, hp + max * 0.3f, freezeAge);
        scheduled.clear();
        numbers.clear();
        frozenNumber(-7, max * 0.15f, DamageKind.PHYSICAL, false, false, -0.8f, c);
        frozenNumber(-8, max * 0.12f, DamageKind.MAGIC, false, false, 0.8f, c);
        frozenNumber(-9, max * 0.3f, DamageKind.PHYSICAL, true, false, 0f, c);
        if (c.numbers.showHeal && 3f >= c.numbers.minHeal) frozenNumber(-10, 3f, DamageKind.PHYSICAL, false, true, 0.4f, c);
    }

    private void frozenNumber(int id, float v, DamageKind kind, boolean crit, boolean heal, float side, FhConfig c) {
        DamageNumber n = DamageNumbers.create(numbers, id, v, max, kind, crit, heal, c);
        if (n == null) return;
        n.s1 = side * 0.5f;
        n.s2 = side;
        n.age = freezeAge;
        if (n.age > n.life) numbers.remove(n);
    }

    public void react(String key, FhConfig c) {
        if (key == null || frozen) return;
        String k = key.substring(key.lastIndexOf('.') + 1);
        scheduled.clear();
        if (hp <= max * 0.3f) {
            hp = max;
            anim.reset(hp);
        }
        switch (k) {
            case "crit_animation", "crit_scale", "crit_color", "crit_mark" -> hit(true, DamageKind.PHYSICAL, c);
            case "heal_color", "show_heal", "min_heal" -> {
                if (hp >= max - 1) {
                    hp = max * 0.6f;
                    anim.reset(hp);
                }
                heal(3 + random.nextInt(3), c);
            }
            case "merge" -> {
                for (int i = 0; i < 4; i++) scheduled.add(new float[]{clock + i * 0.07f, 0, 0});
            }
            case "color_by_type", "damage_color" -> {
                DamageKind[] kinds = {DamageKind.PHYSICAL, DamageKind.FIRE, DamageKind.MAGIC, DamageKind.FALL};
                for (int i = 0; i < kinds.length; i++) scheduled.add(new float[]{clock + i * 0.25f, 0, kinds[i].ordinal()});
            }
            case "show_when", "linger" -> lastChange = clock;
            default -> hit(false, DamageKind.PHYSICAL, c);
        }
    }

    private void update(float dt, FhConfig c) {
        clock += dt;
        anim.update(dt);
        for (int i = 0; i < scheduled.size(); i++) {
            float[] s = scheduled.get(i);
            if (s[0] <= clock) {
                scheduled.remove(i--);
                hit(false, DamageKind.values()[(int) s[2]], c);
            }
        }
        if (hp <= 0) {
            dead -= dt;
            if (dead <= 0) {
                hp = max;
                anim.target = hp;
                lastChange = clock;
            }
            return;
        }
        timer -= dt;
        if (timer > 0) return;
        timer = 0.7f + random.nextFloat() * 0.6f;
        events++;
        float roll = random.nextFloat();
        if (events % 5 == 0) {
            float gap = Math.max(0.03f, c.numbers.mergeMillis / 1000f * 0.6f);
            for (int i = 0; i < 3; i++) scheduled.add(new float[]{clock + i * gap, 0, 0});
        } else if (roll < 0.16f && hp < max) {
            heal(1.5f + random.nextInt(4), c);
        } else if (roll < 0.2f && absorption <= 0) {
            absorption = 4;
        } else {
            DamageKind[] kinds = {DamageKind.PHYSICAL, DamageKind.PHYSICAL, DamageKind.PHYSICAL, DamageKind.FIRE, DamageKind.MAGIC, DamageKind.PROJECTILE};
            hit(roll > 0.82f, kinds[random.nextInt(kinds.length)], c);
        }
    }

    public void hit(boolean crit, DamageKind kind, FhConfig c) {
        if (hp <= 0 || frozen) return;
        float dmg = (max / 20f) * (1.5f + random.nextFloat() * 3f) * (crit ? 1.5f : 1f);
        float fromAbs = Math.min(absorption, dmg);
        absorption -= fromAbs;
        hp = Math.max(0, hp - (dmg - fromAbs));
        anim.set(hp);
        lastChange = clock;
        if (c.numbers.enabled) DamageNumbers.create(numbers, -7, dmg, max, kind, crit, false, c);
        if (hp <= 0) dead = 1.3f;
    }

    private void heal(float v, FhConfig c) {
        float h = Math.min(v, max - hp);
        if (h <= 0) return;
        hp += h;
        anim.set(hp);
        lastChange = clock;
        if (c.numbers.showHeal && h >= c.numbers.minHeal) DamageNumbers.create(numbers, -7, h, max, DamageKind.PHYSICAL, false, true, c);
    }

    private void fillModel(LivingEntity e) {
        model.health = hp;
        model.max = max;
        model.shown = anim.shown;
        model.trail = anim.trail;
        model.absorption = absorption;
        model.flash = anim.flash;
        model.anim = anim;
        model.time = clock;
        model.armor = e != null ? e.getArmorValue() : 4;
        model.name = e != null ? e.getDisplayName() : Mc.translatable("fancyhealth.preview.sample_name");
        model.relation = e != null ? EntityFilter.relation(e) : EntityFilter.HOSTILE;
        model.team = 0;
    }

    private float worldScale(Canvas c, float size, float reference) {
        float ppbRef = c.height() / 2f * 1.4281480f / reference;
        return Math.max(0.3f, Math.min(3.2f, size / ppbRef));
    }

    public void render(Canvas c, FhConfig cfg, ConfigUi.Tab tab, float x0, float y0, float x1, float y1, double mx, double my, float dt) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        if (frozen) freeze(cfg);
        else update(dt, cfg);
        Minecraft mc = Minecraft.getInstance();
        boolean world = mc.level != null;
        if (stage || !world) {
            c.gradient((int) x0, (int) y0, (int) x1, (int) y1, 0xF01A1B22, 0xF0060608);
        } else {
            c.fill((int) x0, (int) y0, (int) x1, (int) y1, 0x22000000);
        }
        Component title = Theme.tr("fancyhealth.preview.title");
        float segW = world ? Theme.width(Theme.tr("fancyhealth.preview.stage")) * 0.8f + Theme.width(Theme.tr("fancyhealth.preview.world")) * 0.8f + 34 : 0;
        float titleMax = (x1 - x0) - 20 - segW;
        c.push();
        c.translate(x0 + 10, y0 + 8);
        c.scale(0.8f);
        Theme.fit(c, title, 0, 0, titleMax / 0.8f, Theme.TEXT2, false, clock, 0);
        c.pop();
        if (world) segmented(c);

        c.scissor((int) x0, (int) y0 + 20, (int) x1, (int) y1);
        if (tab == ConfigUi.Tab.FILTERS) {
            lastMx = mx;
            lastMy = my;
            gallery(c, cfg, x0, y0 + 24, x1, y1 - 8, dt);
            c.endScissor();
            return;
        }
        LivingEntity e = entity();
        fillModel(e);

        float top = y0 + 26;
        boolean targetShown = cfg.target.enabled || tab == ConfigUi.Tab.TARGET;
        boolean hotbarTarget = cfg.target.position == HudPos.ABOVE_HOTBAR;
        int th = TargetHud.height(cfg);
        if (targetShown) {
            int tw = TargetHud.width(cfg);
            float s = Math.min(cfg.target.scale, (x1 - x0 - 16) / (float) tw);
            float tx = switch (cfg.target.position) {
                case TOP_LEFT -> x0 + 8;
                case TOP_RIGHT -> x1 - 8 - tw * s;
                default -> (x0 + x1) / 2f - tw * s / 2f;
            };
            tx = Math.max(x0 + 4, Math.min(x1 - 4 - tw * s, tx + cfg.target.offsetX * 0.35f));
            float ty = hotbarTarget ? y1 - 34 - th * s : top + Math.min(20, cfg.target.offsetY * 0.5f);
            c.push();
            c.translate(tx, ty);
            c.scale(s);
            TargetHud.draw(c, cfg, model, e, 6.5f, cfg.target.enabled ? 1f : 0.45f);
            c.pop();
            if (!hotbarTarget) top += (int) (th * s) + 8;
        }
        boolean crossShown = cfg.crosshair.enabled || tab == ConfigUi.Tab.CROSSHAIR;
        float bottomReserve = (crossShown ? 64 : 30) + (targetShown && hotbarTarget ? th + 6 : 0);
        float areaTop = top + 34;
        float areaBottom = y1 - bottomReserve;
        float cx = (x0 + x1) / 2f;
        float bbh = e != null ? Math.max(0.5f, e.getBbHeight()) : 1.95f;
        float bbw = e != null ? Math.max(0.5f, e.getBbWidth()) : 0.6f;
        float areaH = Math.max(30, areaBottom - areaTop);
        int size = (int) Math.max(10, Math.min(Math.min(areaH * 0.62f, 110f) / bbh, (x1 - x0) * 0.28f / bbw));
        float feet = areaBottom;
        float headY = feet - bbh * size;
        entityX0 = cx - bbw * size / 2f - 6;
        entityX1 = cx + bbw * size / 2f + 6;
        entityY0 = headY - 6;
        entityY1 = feet;
        Theme.rrect(c, cx - size * bbw * 0.8f, feet - 1.5f, cx + size * bbw * 0.8f, feet + 1.5f, 1.5f, 0x40000000);
        if (e != null) {
            int half = (int) (bbh * size / 2f) + 4;
            int mid = (int) ((feet + headY) / 2);
            c.entity(e, (int) cx - half, mid - half, (int) cx + half, mid + half, size, 0f, (float) mx, (float) my);
        } else {
            silhouette(c, cx, feet, size);
        }

        boolean vanillaName = e instanceof net.minecraft.world.entity.player.Player || e != null && e.hasCustomName();
        if (vanillaName) nametag(c, model.name, cx, headY, size);
        if (cfg.overhead.enabled || tab == ConfigUi.Tab.OVERHEAD) {
            boolean looking = Theme.in(mx, my, entityX0, entityY0, entityX1, entityY1);
            boolean show = switch (cfg.overhead.showWhen) {
                case ALWAYS -> true;
                case DAMAGED -> hp < max - 0.01f || clock - lastChange < cfg.overhead.lingerSeconds;
                case RECENT -> clock - lastChange < cfg.overhead.lingerSeconds || looking;
                case LOOKING -> looking;
            };
            plateAlpha += ((show ? 1 : 0) - plateAlpha) * Math.min(1f, dt * cfg.general.fadeSpeed);
            if (frozen) plateAlpha = show ? 1 : 0;
            float a = plateAlpha * (cfg.overhead.enabled ? 1f : 0.4f);
            float scale = cfg.overhead.scale * (cfg.overhead.sizeMode != SizeMode.WORLD ? 1f
                    : cfg.overhead.look.style == me.bbijabnpobatejb.fancyhealth.config.BarStyle.NAMETAG ? size * 0.025f : worldScale(c, size, 7f));
            scale = Math.min(scale, (x1 - x0 - 16) / (Math.max(20, cfg.overhead.look.width) + 44f));
            float anchorY = switch (cfg.overhead.anchor) {
                case ABOVE_NAME -> vanillaName ? headY - 0.56f * size : headY - 0.14f * size;
                case BELOW_NAME -> vanillaName ? headY - 0.245f * size : headY - 0.14f * size;
                case ABOVE_HEAD -> headY - 0.14f * size;
                case CHEST -> feet - bbh * 0.62f * size;
            };
            anchorY -= cfg.overhead.yOffset * size;
            if (a > 0.01f) {
                c.push();
                c.translate(cx, anchorY);
                c.scale(scale);
                if (cfg.overhead.anchor == Anchor.BELOW_NAME && vanillaName) c.translate(0, BarRenderer.plateHeight(cfg.overhead.look, model, false) + 1);
                BarRenderer.plate(c, cfg.overhead.look, model, a, !vanillaName, false);
                c.pop();
            }
            if (cfg.overhead.showWhen == ShowWhen.LOOKING && !looking) {
                Component hm = Theme.tr("fancyhealth.preview.hover_me");
                Theme.small(c, hm, cx - Theme.width(hm) * 0.4f, headY - 0.3f * size, Theme.TEXT3);
            }
        }

        if (cfg.numbers.enabled || tab == ConfigUi.Tab.NUMBERS) {
            float near = worldScale(c, size, 6f);
            float factor = 1f + (near - 1f) * cfg.numbers.distanceScaling;
            DamageNumbers.stackIndices(numbers);
            for (int i = 0; i < numbers.size(); i++) {
                DamageNumber n = numbers.get(i);
                if (!frozen) n.age += dt;
                n.bump = frozen ? 0 : Math.max(0, n.bump - dt * 5f);
                if (n.age > n.life) {
                    numbers.remove(i--);
                    continue;
                }
                DamageNumbers.draw(c, n, cx + n.s2 * bbw * size * 0.25f, headY + bbh * size * 0.3f, factor, cfg.numbers.enabled ? 1f : 0.45f, cfg);
            }
        } else {
            numbers.clear();
        }

        if (crossShown) {
            float chY = y1 - 56 - (targetShown && hotbarTarget ? th + 6 : 0);
            c.fill((int) cx - 4, (int) chY, (int) cx + 5, (int) chY + 1, 0xC0FFFFFF);
            c.fill((int) cx, (int) chY - 4, (int) cx + 1, (int) chY + 5, 0xC0FFFFFF);
            CrosshairHud.draw(c, cfg, model, cfg.crosshair.enabled ? 1f : 0.45f, cx, chY);
        }
        c.endScissor();

        Component sel = Theme.ui("‹   " + model.name.getString() + "   ›");
        boolean selHover = Theme.in(mx, my, x0, y1 - 26, x1, y1 - 12);
        Theme.centered(c, sel, cx, y1 - 23, selHover ? Theme.BLUE : Theme.TEXT);
        Component hint = Theme.tr("fancyhealth.preview.hint");
        Theme.scaled(c, hint, cx - Theme.width(hint) * 0.35f, y1 - 11, 0.7f, Theme.TEXT3);
    }

    private void nametag(Canvas c, Component name, float cx, float headY, int size) {
        float s = size * 0.025f;
        Component plain = Mc.literal(name.getString());
        int w = Theme.font().width(plain);
        c.push();
        c.translate(cx, headY - 0.5f * size);
        c.scale(s);
        c.fill(-w / 2 - 1, -1, w / 2 + 1, 8, 0x40000000);
        c.text(Theme.font(), plain, -w / 2, 0, 0xFFFFFFFF, false);
        c.pop();
    }

    private void segmented(Canvas c) {
        Component a = Theme.tr("fancyhealth.preview.stage"), b = Theme.tr("fancyhealth.preview.world");
        float wa = Theme.width(a) * 0.8f + 12, wb = Theme.width(b) * 0.8f + 12;
        float sx1 = x1 - 8, sx0 = sx1 - wa - wb, sy0 = y0 + 5, sy1 = y0 + 17;
        Theme.rrect(c, sx0, sy0, sx1, sy1, 6, 0xFF2C2C2E);
        float selX0 = stage ? sx0 + 1 : sx0 + wa, selX1 = stage ? sx0 + wa : sx1 - 1;
        Theme.rrect(c, selX0, sy0 + 1, selX1, sy1 - 1, 5, 0xFF636366);
        Theme.small(c, a, sx0 + 6, sy0 + 2.5f, stage ? Theme.TEXT : Theme.TEXT2);
        Theme.small(c, b, sx0 + wa + 6, sy0 + 2.5f, !stage ? Theme.TEXT : Theme.TEXT2);
    }

    private void gallery(Canvas c, FhConfig cfg, float gx0, float gy0, float gx1, float gy1, float dt) {
        ensure();
        int n = GALLERY.length;
        int cols = Math.max(2, Math.min(4, (int) ((gx1 - gx0) / 72)));
        int rows = (n + cols - 1) / cols;
        float cw = (gx1 - gx0 - 8) / cols, ch = Math.min(110, (gy1 - gy0 - 8) / rows);
        for (int i = 0; i < n; i++) {
            float cx0 = gx0 + 4 + (i % cols) * cw, cy0 = gy0 + 4 + (i / cols) * ch;
            float ccx = cx0 + cw / 2f;
            LivingEntity e = i < gallery.size() ? gallery.get(i) : null;
            boolean on = switch (GALLERY_KEYS[i]) {
                case "players" -> cfg.filters.players;
                case "hostile" -> cfg.filters.hostile;
                case "neutral" -> cfg.filters.neutral;
                case "passive" -> cfg.filters.passive;
                case "bosses" -> cfg.filters.bosses;
                case "armor_stands" -> cfg.filters.armorStands;
                case "npcs" -> cfg.filters.npcs;
                default -> cfg.general.selfInThirdPerson;
            };
            cells[i] = new float[]{cx0 + 2, cy0 + 2, cx0 + cw - 2, cy0 + ch - 2};
            boolean over = Theme.in(lastMx, lastMy, cx0 + 2, cy0 + 2, cx0 + cw - 2, cy0 + ch - 2);
            cellHover[i] += ((over ? 1 : 0) - cellHover[i]) * Math.min(1f, dt * 14f);
            if (cellHover[i] > 0.02f) Theme.shadow(c, cx0 + 2, cy0 + 2, cx0 + cw - 2, cy0 + ch - 2, 9, (int) (80 * cellHover[i]));
            Theme.rrect(c, cx0 + 2, cy0 + 2, cx0 + cw - 2, cy0 + ch - 2, 9, Theme.mix(on ? 0x1EFFFFFF : 0x0CFFFFFF, 0x30FFFFFF, cellHover[i]));
            if (cellHover[i] > 0.02f) Theme.rrect(c, cx0 + 2, cy0 + ch - 4, cx0 + cw - 2, cy0 + ch - 2, 1, Theme.mix(0x000A84FF, on ? Theme.RED : Theme.GREEN, cellHover[i]));
            float bbh = e != null ? Math.max(0.5f, e.getBbHeight()) : 1.8f;
            float bbw = e != null ? Math.max(0.5f, e.getBbWidth()) : 0.6f;
            int size = (int) Math.max(6, Math.min((ch - 40) / bbh, (cw - 16) / Math.max(bbw, 0.8f)));
            float feet = cy0 + ch - 16;
            float head = feet - bbh * size;
            if (e != null) {
                int half = (int) (bbh * size / 2f) + 3;
                int mid = (int) ((feet + head) / 2);
                c.entity(e, (int) ccx - half, mid - half, (int) ccx + half, mid + half, size, 0f, ccx + 30, mid);
            }
            if (!on) {
                c.push();
                c.front();
                Theme.rrect(c, cx0 + 2, cy0 + 2, cx0 + cw - 2, cy0 + ch - 2, 9, 0xB0101012);
                Theme.circle(c, ccx, (head + feet) / 2f, 9, 0xE0FF453A);
                Theme.icon(c, 9, ccx, (head + feet) / 2f, 10, 0xFFFFFFFF);
                c.pop();
            } else {
                fillModel(e);
                model.health = model.max * (0.45f + 0.08f * i);
                model.shown = model.health;
                model.trail = model.health;
                model.flash = 0;
                model.anim = null;
                c.push();
                c.translate(ccx, head - 3);
                c.scale(0.7f);
                BarRenderer.plate(c, cfg.overhead.look, model, 1f, false, false);
                c.pop();
            }
            Component label = Theme.tr("fancyhealth.option." + GALLERY_KEYS[i]);
            Theme.fit(c, label, cx0 + 5, cy0 + ch - 13, cw - 10, on ? Theme.TEXT : Theme.TEXT3, false, clock, 1);
        }
    }

    private void silhouette(Canvas c, float cx, float feet, int size) {
        int u = Math.max(1, size / 16);
        int col = 0xFF3B4152;
        int x = (int) cx, f = (int) feet;
        c.fill(x - 4 * u, f - 32 * u, x + 4 * u, f - 24 * u, col);
        c.fill(x - 4 * u, f - 24 * u, x + 4 * u, f - 12 * u, col);
        c.fill(x - 8 * u, f - 24 * u, x - 4 * u, f - 12 * u, col);
        c.fill(x + 4 * u, f - 24 * u, x + 8 * u, f - 12 * u, col);
        c.fill(x - 4 * u, f - 12 * u, x, f, col);
        c.fill(x, f - 12 * u, x + 4 * u, f, col);
    }

    public boolean click(double mx, double my, int button, FhConfig cfg, ConfigUi.Tab tab) {
        if (!Theme.in(mx, my, x0, y0, x1, y1)) return false;
        Minecraft mc = Minecraft.getInstance();
        if (my < y0 + 20) {
            if (mc.level != null && mx > x1 - 90) {
                stage = !stage;
                Sounds.play("click");
            }
            return true;
        }
        if (tab == ConfigUi.Tab.FILTERS) {
            for (int i = 0; i < cells.length; i++) {
                float[] r = cells[i];
                if (r == null || !Theme.in(mx, my, r[0], r[1], r[2], r[3])) continue;
                boolean v;
                switch (GALLERY_KEYS[i]) {
                    case "players" -> v = cfg.filters.players = !cfg.filters.players;
                    case "hostile" -> v = cfg.filters.hostile = !cfg.filters.hostile;
                    case "neutral" -> v = cfg.filters.neutral = !cfg.filters.neutral;
                    case "passive" -> v = cfg.filters.passive = !cfg.filters.passive;
                    case "bosses" -> v = cfg.filters.bosses = !cfg.filters.bosses;
                    case "npcs" -> v = cfg.filters.npcs = !cfg.filters.npcs;
                    case "armor_stands" -> v = cfg.filters.armorStands = !cfg.filters.armorStands;
                    default -> v = cfg.general.selfInThirdPerson = !cfg.general.selfInThirdPerson;
                }
                Sounds.play(v ? "toggle_on" : "toggle_off");
                changed = true;
                return true;
            }
            return true;
        }
        if (my >= y1 - 26 && my < y1 - 12) {
            cycle(mx < (x0 + x1) / 2.0 ? -1 : 1);
            return true;
        }
        if (button == 2) {
            if (hp >= max - 0.5f) {
                hp = max * 0.55f;
                anim.reset(hp);
            }
            heal(2 + random.nextInt(4), cfg);
            Sounds.play("toggle_on", 1.4f);
        } else {
            hit(button == 1, DamageKind.PHYSICAL, cfg);
            Sounds.play("click", button == 1 ? 0.8f : 1f);
        }
        return true;
    }
}
