package me.bbijabnpobatejb.fancyhealth.ui;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.bbijabnpobatejb.fancyhealth.config.BarStyle;
import me.bbijabnpobatejb.fancyhealth.config.ConfigStore;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.config.Presets;
import me.bbijabnpobatejb.fancyhealth.numbers.DamageNumber;
import me.bbijabnpobatejb.fancyhealth.numbers.DamageNumbers;
import me.bbijabnpobatejb.fancyhealth.render.BarModel;
import me.bbijabnpobatejb.fancyhealth.render.BarRenderer;
import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import me.bbijabnpobatejb.fancyhealth.track.DamageKind;
import me.bbijabnpobatejb.fancyhealth.track.EntityFilter;
import net.minecraft.network.chat.Component;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class PresetRow extends Rows.Row {

    final String id;
    final FhConfig sample;
    final BarModel model = new BarModel();
    final DamageNumber number = new DamageNumber();
    float clock;
    float hover;

    public PresetRow(String id) {
        super("fancyhealth.preset." + id);
        this.id = id;
        this.sample = Presets.create(id);
        height = 54;
        clock = (id.hashCode() & 0xFF) / 255f * 2f;
        number.kind = DamageKind.PHYSICAL;
        number.maxHp = 20;
        number.s1 = 0.4f;
        number.s2 = 0.2f;
    }

    @Override
    public boolean card() {
        return false;
    }

    @Override
    public void render(Canvas c, Rows.Host host, Rows.Ctx ctx) {
        clock += ctx.dt;
        boolean active = id.equals(ConfigStore.get().preset);
        boolean base = id.equals(ConfigStore.get().base);
        hover += ((ctx.hovered ? 1 : 0) - hover) * Math.min(1f, ctx.dt * 14f);
        float lift = hover * 1.2f;
        float x0 = x + 4, y0 = y + 3 - lift, x1 = x + w - 4, y1 = y + height - 3 - lift;
        int bg = Theme.mix(Theme.CARD, 0xFF2A2A2D, hover);
        if (hover > 0.02f) Theme.shadow(c, x0, y0, x1, y1, 10, (int) (90 * hover));
        if (active) {
            Theme.rrect(c, x0, y0, x1, y1, 10, Theme.BLUE);
            Theme.rrect(c, x0 + 1.5f, y0 + 1.5f, x1 - 1.5f, y1 - 1.5f, 8.5f, bg);
        } else {
            Theme.rrect(c, x0, y0, x1, y1, 10, bg);
        }
        Component title = Theme.bold(Component.translatable(key).getString());
        Theme.text(c, title, x0 + 10, y0 + 8, Theme.TEXT);
        float after = x0 + 14 + Theme.width(title);
        if (active) Theme.icon(c, 8, after + 4, y0 + 12, 9, Theme.BLUE);
        else if (base) Theme.small(c, Theme.tr("fancyhealth.label.edited"), after, y0 + 9, Theme.ORANGE);
        int textW = (int) ((x1 - x0) * 0.54f);
        List<String> lines = Theme.wrap(Component.translatable(key + ".desc").getString(), (int) (textW / 0.8f), 2);
        for (int i = 0; i < lines.size(); i++) Theme.small(c, Theme.ui(lines.get(i)), x0 + 10, y0 + 21 + i * 9, Theme.TEXT2);

        float cycle = clock % 2.4f;
        float hp = 20f - (cycle > 0.8f ? 7f : 0f) - (cycle > 1.6f ? 4f : 0f);
        model.health = hp;
        model.max = 20f;
        model.shown = hp;
        model.trail = Math.min(20f, hp + (cycle % 0.8f < 0.45f && cycle > 0.8f ? 5f : 0f));
        model.absorption = 0;
        model.armor = 8;
        model.flash = cycle % 0.8f < 0.15f && cycle > 0.8f ? 0.8f : 0f;
        model.name = Component.translatable("fancyhealth.preview.sample_name");
        model.relation = EntityFilter.HOSTILE;
        model.time = clock;
        FhConfig.BarLook look = sample.overhead.enabled ? sample.overhead.look : sample.target.look;
        int previewX = (int) (x0 + (x1 - x0) * 0.58f);
        int previewW = (int) (x1 - previewX - 6);
        c.scissor(previewX - 2, (int) y0 + 1, (int) x1 - 1, (int) y1 - 1);
        c.push();
        float scale = 1f;
        int lw = look.style == BarStyle.HEARTS ? 82 : look.width + 30;
        if (lw > previewW) scale = previewW / (float) lw;
        c.translate(previewX + previewW * 0.45f, y0 + height - 14);
        c.scale(scale);
        BarRenderer.plate(c, look, model, 1f, false, false);
        c.pop();
        if (cycle > 0.8f) {
            number.animation = cycle > 1.6f ? sample.numbers.critAnimation : sample.numbers.animation;
            number.crit = cycle > 1.6f;
            number.value = cycle > 1.6f ? 4f : 7f;
            number.age = cycle % 0.8f;
            number.life = 0.8f;
            number.stackIndex = 0;
            DamageNumbers.draw(c, number, previewX + previewW * 0.8f, y0 + 20, 0.8f, 1f, sample);
        }
        c.endScissor();
    }

    @Override
    public boolean click(Rows.Host host, double mx, double my, int button, boolean shift) {
        ConfigStore.set(Presets.apply(ConfigStore.get(), id));
        Sounds.play("success");
        host.changed(this);
        ConfigStore.get().preset = id;
        return true;
    }

    @Override
    public String descKey() {
        return key + ".desc";
    }
}
