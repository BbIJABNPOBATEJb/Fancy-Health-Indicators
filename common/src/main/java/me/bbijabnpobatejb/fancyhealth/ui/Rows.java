package me.bbijabnpobatejb.fancyhealth.ui;

import me.bbijabnpobatejb.fancyhealth.compat.Mc;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.bbijabnpobatejb.fancyhealth.config.ConfigStore;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.config.Labeled;
import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class Rows {

    public static final int ROW = 22;

    private Rows() {
    }

    public interface Host {
        void changed(Row row);

        void openColor(ColorRow row, float x, float y);

        FhConfig defaults();
    }

    public static final class Ctx {
        public final double mx;
        public final double my;
        public final float dt;
        public final float time;
        public final boolean hovered;
        public final boolean resetMark;

        public Ctx(double mx, double my, float dt, float time, boolean hovered, boolean resetMark) {
            this.mx = mx;
            this.my = my;
            this.dt = dt;
            this.time = time;
            this.hovered = hovered;
            this.resetMark = resetMark;
        }
    }

    public abstract static class Row {
        public final String key;
        public int height = ROW;
        public float x;
        public float y;
        public float w;
        protected float magX;
        protected float magY;
        protected float resetHover;
        protected boolean showReset;

        public java.util.function.Predicate<FhConfig> activeWhen;
        public java.util.function.Consumer<FhConfig> activate;
        public String parentKey;

        protected Row(String key) {
            this.key = key;
        }

        @SuppressWarnings("unchecked")
        public <R extends Row> R needs(String parentKey, java.util.function.Predicate<FhConfig> when, java.util.function.Consumer<FhConfig> activate) {
            this.parentKey = parentKey;
            this.activeWhen = when;
            this.activate = activate;
            return (R) this;
        }

        public boolean inactive(FhConfig c) {
            return activeWhen != null && !activeWhen.test(c);
        }

        public boolean card() {
            return true;
        }

        public boolean value() {
            return false;
        }

        public boolean modified(FhConfig cur, FhConfig def) {
            return false;
        }

        public void reset(FhConfig cur, FhConfig def) {
        }

        public String descKey() {
            return key == null ? null : key + ".desc";
        }

        public boolean editing() {
            return false;
        }

        public String editText() {
            return "";
        }

        public void selectAll() {
        }

        public boolean keyPressed(Host host, int key) {
            return false;
        }

        public boolean charTyped(Host host, int cp) {
            return false;
        }

        public void stopEditing(Host host, boolean commit) {
        }

        protected FhConfig cfg() {
            return ConfigStore.get();
        }

        protected float controlWidth() {
            return Math.max(70, Math.min(104, w * 0.42f));
        }

        protected float controlX() {
            return x + w - 10 - controlWidth();
        }

        protected float resetX() {
            return controlX() - 13;
        }

        public void render(Canvas c, Host host, Ctx ctx) {
            float cx = controlX() + controlWidth() / 2f, cy = y + height / 2f;
            boolean overControl = ctx.hovered && ctx.mx >= controlX() - 4;
            float tx = overControl ? (float) Math.max(-2, Math.min(2, (ctx.mx - cx) * 0.05)) : 0;
            float ty = overControl ? (float) Math.max(-1.2, Math.min(1.2, (ctx.my - cy) * 0.12)) : 0;
            float k = Math.min(1f, ctx.dt * 14f);
            magX += (tx - magX) * k;
            magY += (ty - magY) * k;
            if (ctx.resetMark && value() && modified(cfg(), host.defaults())) {
                Theme.rrect(c, x + 2, y + 1, x + w - 2, y + height - 1, 7, Theme.RESET_HIGHLIGHT);
            }
            showReset = value() && modified(cfg(), host.defaults());
            label(c, ctx);
            if (showReset) {
                boolean over = ctx.hovered && Theme.in(ctx.mx, ctx.my, resetX() - 2, y, resetX() + 10, y + height);
                resetHover += ((over ? 1 : 0) - resetHover) * Math.min(1f, ctx.dt * 16f);
                if (resetHover > 0.02f) Theme.circle(c, resetX() + 4, y + height / 2f, 6, Theme.mix(0x00FFFFFF, 0x26FFFFFF, resetHover));
                Theme.icon(c, 7, resetX() + 4, y + height / 2f, 8, Theme.mix(Theme.TEXT3, Theme.BLUE, Math.max(resetHover, 0.35f)));
            }
            c.push();
            c.translate(Theme.snap(magX) + Theme.debugNudge, Theme.snap(magY) + Theme.debugNudge);
            control(c, host, ctx);
            c.pop();
        }

        protected void label(Canvas c, Ctx ctx) {
            float max = (showReset ? resetX() : controlX()) - x - 16;
            Theme.fit(c, Theme.tr(key), x + 12, y + height / 2f - 4.5f, max, inactive(cfg()) ? Theme.TEXT3 : Theme.TEXT, ctx.hovered, ctx.time, 0);
        }

        protected void control(Canvas c, Host host, Ctx ctx) {
        }

        public boolean click(Host host, double mx, double my, int button, boolean shift) {
            if (value() && modified(cfg(), host.defaults()) && Theme.in(mx, my, resetX() - 2, y, resetX() + 10, y + height)) {
                reset(cfg(), host.defaults());
                Sounds.play("reset", 1.3f);
                host.changed(this);
                return true;
            }
            return clickControl(host, mx, my, button, shift);
        }

        protected boolean clickControl(Host host, double mx, double my, int button, boolean shift) {
            return false;
        }

        public void drag(Host host, double mx, double my) {
        }

        public void release() {
        }
    }

    public abstract static class ValueRow<T> extends Row {
        protected final Function<FhConfig, T> get;
        protected final BiConsumer<FhConfig, T> set;

        protected ValueRow(String key, Function<FhConfig, T> get, BiConsumer<FhConfig, T> set) {
            super(key);
            this.get = get;
            this.set = set;
        }

        @Override
        public boolean value() {
            return true;
        }

        @Override
        public boolean modified(FhConfig cur, FhConfig def) {
            if (def == null) return false;
            T a = get.apply(cur), b = get.apply(def);
            if (a instanceof Float fa && b instanceof Float fb) return Math.abs(fa - fb) > 0.0001f;
            return !Objects.equals(a, b);
        }

        @Override
        public void reset(FhConfig cur, FhConfig def) {
            set.accept(cur, get.apply(def));
        }

        public Object rawGet(FhConfig c) {
            return get.apply(c);
        }

        @SuppressWarnings("unchecked")
        public void rawSet(FhConfig c, Object v) {
            set.accept(c, (T) v);
        }

        public void auditMutate(FhConfig c) {
        }
    }

    public static class Header extends Row {
        public Header(String key) {
            super(key);
            height = 24;
        }

        @Override
        public boolean card() {
            return false;
        }

        @Override
        public void render(Canvas c, Host host, Ctx ctx) {
            String t = Mc.translatable(key).getString().toUpperCase(Locale.ROOT);
            Theme.small(c, Theme.ui(t), x + 12, y + 12, Theme.TEXT2);
        }

        @Override
        public String descKey() {
            return null;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Caption extends Row {
        final String text;
        final int color;

        public Caption(String text, int color) {
            super(null);
            this.text = text;
            this.color = color;
            height = 24;
        }

        @Override
        public boolean card() {
            return false;
        }

        @Override
        public void render(Canvas c, Host host, Ctx ctx) {
            Theme.circle(c, x + 14, y + 15f, 2.5f, color);
            Theme.small(c, Theme.ui(text.toUpperCase(Locale.ROOT)), x + 20, y + 12, Theme.TEXT2);
        }

        @Override
        public String descKey() {
            return null;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Toggle extends ValueRow<Boolean> {
        float anim = -1;

        public Toggle(String key, Function<FhConfig, Boolean> get, BiConsumer<FhConfig, Boolean> set) {
            super(key, get, set);
        }

        @Override
        public void auditMutate(FhConfig c) {
            set.accept(c, !get.apply(c));
        }

        @Override
        protected float controlWidth() {
            return 26;
        }

        @Override
        protected void control(Canvas c, Host host, Ctx ctx) {
            boolean on = get.apply(cfg());
            if (anim < 0) anim = on ? 1 : 0;
            anim += ((on ? 1 : 0) - anim) * Math.min(1f, ctx.dt * 14f);
            float sx = x + w - 10 - 26, sy = y + height / 2f - 7.5f;
            Theme.rrect(c, sx, sy, sx + 26, sy + 15, 7.5f, Theme.mix(Theme.SWITCH_OFF, Theme.GREEN, anim));
            float kx = sx + 1.5f + anim * 11f;
            Theme.circle(c, kx + 6, sy + 7.5f + 0.6f, 6.4f, 0x30000000);
            Theme.circle(c, kx + 6, sy + 7.5f, 6f, 0xFFFFFFFF);
        }

        @Override
        protected boolean clickControl(Host host, double mx, double my, int button, boolean shift) {
            boolean v = !get.apply(cfg());
            set.accept(cfg(), v);
            Sounds.play(v ? "toggle_on" : "toggle_off");
            host.changed(this);
            return true;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Slider extends ValueRow<Float> {
        final float min;
        final float max;
        final float step;
        final String suffix;
        boolean dragging;
        boolean editing;
        String buffer = "";
        boolean fresh;
        float knob = -1;

        public Slider(String key, float min, float max, float step, String suffix, Function<FhConfig, Float> get, BiConsumer<FhConfig, Float> set) {
            super(key, get, set);
            this.min = min;
            this.max = max;
            this.step = step;
            this.suffix = suffix;
        }

        @Override
        public void auditMutate(FhConfig c) {
            float v = get.apply(c);
            set.accept(c, v - min > (max - min) / 2f ? min : max);
        }

        private float valueWidth() {
            return 34;
        }

        @Override
        protected float controlWidth() {
            return Math.max(84, Math.min(128, w * 0.46f));
        }

        private float trackX0() {
            return controlX() + valueWidth() + 6;
        }

        private float trackX1() {
            return x + w - 14;
        }

        private String format(float v) {
            if (step >= 1) return String.valueOf(Math.round(v));
            if (step >= 0.1f) return String.format(Locale.ROOT, "%.1f", v);
            return String.format(Locale.ROOT, "%.2f", v);
        }

        @Override
        public boolean editing() {
            return editing;
        }

        @Override
        public String editText() {
            return buffer;
        }

        @Override
        public void selectAll() {
            fresh = true;
        }

        @Override
        protected void control(Canvas c, Host host, Ctx ctx) {
            float v = get.apply(cfg());
            float t = Math.max(0, Math.min(1, (v - min) / (max - min)));
            if (knob < 0) knob = t;
            knob += (t - knob) * Math.min(1f, ctx.dt * (dragging ? 40f : 16f));
            float x0 = trackX0(), x1 = trackX1(), cy = y + height / 2f;
            Theme.rrect(c, x0, cy - 1.5f, x1, cy + 1.5f, 1.5f, Theme.TRACK);
            float kx = x0 + (x1 - x0) * knob;
            Theme.rrect(c, x0, cy - 1.5f, Math.max(x0 + 3, kx), cy + 1.5f, 1.5f, Theme.BLUE);
            float kr = dragging ? 6.2f : 5.5f;
            Theme.circle(c, kx, cy + 0.7f, kr + 0.5f, 0x38000000);
            Theme.circle(c, kx, cy, kr, 0xFFFFFFFF);
            float vx = controlX();
            if (editing) {
                Theme.editField(c, vx - 4, vx + valueWidth() + 2, y + 4, y + height - 4, buffer, fresh, ctx.time, 1f);
            } else {
                Theme.fit(c, Theme.ui(format(v) + suffix), vx, cy - 4.5f, valueWidth(), Theme.TEXT2, ctx.hovered, ctx.time, 2);
            }
        }

        @Override
        protected boolean clickControl(Host host, double mx, double my, int button, boolean shift) {
            if (button == 1) {
                editing = true;
                fresh = true;
                buffer = format(get.apply(cfg()));
                Sounds.play("click", 1.2f);
                return true;
            }
            if (mx < trackX0() - 6) return false;
            dragging = true;
            drag(host, mx, my);
            return true;
        }

        @Override
        public void drag(Host host, double mx, double my) {
            if (!dragging) return;
            float t = (float) Math.max(0, Math.min(1, (mx - trackX0()) / (trackX1() - trackX0())));
            apply(host, min + (max - min) * t, true);
        }

        private void apply(Host host, float v, boolean snap) {
            if (snap) v = Math.round(v / step) * step;
            v = Math.max(min, Math.min(max, v));
            if (Math.abs(v - get.apply(cfg())) > 0.00001f) {
                set.accept(cfg(), v);
                Sounds.play("tick", 0.9f + 0.4f * (v - min) / (max - min));
                host.changed(this);
            }
        }

        @Override
        public void release() {
            dragging = false;
        }

        @Override
        public boolean keyPressed(Host host, int key) {
            if (!editing) return false;
            if (key == me.bbijabnpobatejb.fancyhealth.compat.Keys.RETURN || key == me.bbijabnpobatejb.fancyhealth.compat.Keys.NUMPAD_ENTER) {
                stopEditing(host, true);
            } else if (key == me.bbijabnpobatejb.fancyhealth.compat.Keys.ESCAPE) {
                stopEditing(host, false);
            } else if (key == me.bbijabnpobatejb.fancyhealth.compat.Keys.BACKSPACE && !buffer.isEmpty()) {
                buffer = fresh ? "" : buffer.substring(0, buffer.length() - 1);
                fresh = false;
            }
            return true;
        }

        @Override
        public boolean charTyped(Host host, int cp) {
            if (!editing) return false;
            char ch = (char) cp;
            if ((Character.isDigit(ch) || ch == '.' || ch == ',' || ch == '-') && (fresh || buffer.length() < 8)) {
                if (fresh) buffer = "";
                fresh = false;
                buffer += ch == ',' ? '.' : ch;
                Sounds.play("tick", 1.4f);
            }
            return true;
        }

        @Override
        public void stopEditing(Host host, boolean commit) {
            if (!editing) return;
            editing = false;
            if (!commit) return;
            try {
                float v = Float.parseFloat(buffer);
                v = Math.max(min, Math.min(max, v));
                if (step >= 1) v = Math.round(v);
                set.accept(cfg(), v);
                Sounds.play("success", 1.2f);
                host.changed(this);
            } catch (NumberFormatException e) {
                Sounds.play("error");
            }
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Cycle<E extends Enum<E> & Labeled> extends ValueRow<E> {
        final E[] values;
        float leftHover;
        float rightHover;

        public Cycle(String key, E[] values, Function<FhConfig, E> get, BiConsumer<FhConfig, E> set) {
            super(key, get, set);
            this.values = values;
        }

        public E[] values() {
            return values;
        }

        @Override
        public void auditMutate(FhConfig c) {
            E v = get.apply(c);
            set.accept(c, values[(v.ordinal() + 1) % values.length]);
        }

        @Override
        protected float controlWidth() {
            return Math.max(84, Math.min(128, w * 0.46f));
        }

        @Override
        protected void control(Canvas c, Host host, Ctx ctx) {
            float x0 = controlX(), x1 = x + w - 10, cy = y + height / 2f;
            boolean over = ctx.hovered && Theme.in(ctx.mx, ctx.my, x0 - 4, y, x1 + 6, y + height);
            boolean left = over && ctx.mx < (x0 + x1) / 2f;
            boolean right = over && !left;
            float k = Math.min(1f, ctx.dt * 16f);
            leftHover += ((left ? 1 : 0) - leftHover) * k;
            rightHover += ((right ? 1 : 0) - rightHover) * k;
            if (over) Theme.rrect(c, x0 - 2, y + 3, x1 + 2, y + height - 3, 6, 0x10FFFFFF);
            E v = get.apply(cfg());
            Component t = Theme.tr(v.key());
            Theme.fit(c, t, x0 + 10, cy - 4.5f, x1 - x0 - 20, Theme.BLUE, over, ctx.time, 1);
            Theme.text(c, Theme.bold("‹"), x0 + 1 - leftHover, cy - 4.5f, Theme.mix(Theme.TEXT3, Theme.BLUE, leftHover));
            Theme.text(c, Theme.bold("›"), x1 - 5 + rightHover, cy - 4.5f, Theme.mix(Theme.TEXT3, Theme.BLUE, rightHover));
        }

        @Override
        protected boolean clickControl(Host host, double mx, double my, int button, boolean shift) {
            float x0 = controlX(), x1 = x + w - 10;
            if (mx < x0 - 4) return false;
            int dir = button == 1 || mx < (x0 + x1) / 2f ? -1 : 1;
            E v = get.apply(cfg());
            set.accept(cfg(), values[(v.ordinal() + dir + values.length) % values.length]);
            Sounds.play("click", dir < 0 ? 0.9f : 1.1f);
            host.changed(this);
            return true;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ColorRow extends ValueRow<Integer> {
        boolean editing;
        boolean fresh;
        String buffer = "";

        public ColorRow(String key, Function<FhConfig, Integer> get, BiConsumer<FhConfig, Integer> set) {
            super(key, get, set);
        }

        public Function<FhConfig, Integer> getter() {
            return get;
        }

        public BiConsumer<FhConfig, Integer> setter() {
            return set;
        }

        @Override
        public void auditMutate(FhConfig c) {
            int v = get.apply(c);
            int a = v >>> 24;
            set.accept(c, a < 0x40 ? 0xFFFF00FF : (a << 24) | (~v & 0xFFFFFF));
        }

        @Override
        protected float controlWidth() {
            return 72;
        }

        @Override
        public boolean editing() {
            return editing;
        }

        @Override
        public String editText() {
            return buffer;
        }

        @Override
        public void selectAll() {
            fresh = true;
        }

        private static String hex(int v) {
            return (v >>> 24) == 0xFF ? String.format(Locale.ROOT, "#%06X", v & 0xFFFFFF) : String.format(Locale.ROOT, "#%08X", v);
        }

        @Override
        protected void control(Canvas c, Host host, Ctx ctx) {
            int v = get.apply(cfg());
            float cx = x + w - 18, cy = y + height / 2f;
            Theme.circle(c, cx, cy, 7.5f, 0xFF3A3A3C);
            checker(c, cx, cy, 6.5f);
            Theme.circle(c, cx, cy, 6.5f, v);
            if (editing) {
                Theme.editField(c, cx - 66, cx - 11, y + 4, y + height - 4, buffer, fresh, ctx.time, 0.9f);
            } else {
                Component t = Theme.ui(hex(v));
                Theme.small(c, t, cx - 11 - Theme.width(t) * 0.8f, cy - 3.5f, Theme.TEXT2);
            }
        }

        public static void checker(Canvas c, float cx, float cy, float r) {
            Theme.circle(c, cx, cy, r, 0xFFBDBDBD);
            Theme.rrect(c, cx - r * 0.7f, cy - r * 0.7f, cx, cy, 0, 0xFF808080);
            Theme.rrect(c, cx, cy, cx + r * 0.7f, cy + r * 0.7f, 0, 0xFF808080);
        }

        @Override
        protected boolean clickControl(Host host, double mx, double my, int button, boolean shift) {
            if (mx < controlX() - 4) return false;
            if (button == 1) {
                editing = true;
                fresh = true;
                buffer = hex(get.apply(cfg()));
                Sounds.play("click", 1.2f);
                return true;
            }
            host.openColor(this, x + w - 10, y + height);
            Sounds.play("click");
            return true;
        }

        @Override
        public boolean keyPressed(Host host, int key) {
            if (!editing) return false;
            if (key == me.bbijabnpobatejb.fancyhealth.compat.Keys.RETURN || key == me.bbijabnpobatejb.fancyhealth.compat.Keys.NUMPAD_ENTER) {
                stopEditing(host, true);
            } else if (key == me.bbijabnpobatejb.fancyhealth.compat.Keys.ESCAPE) {
                stopEditing(host, false);
            } else if (key == me.bbijabnpobatejb.fancyhealth.compat.Keys.BACKSPACE && !buffer.isEmpty()) {
                buffer = fresh ? "" : buffer.substring(0, buffer.length() - 1);
                fresh = false;
            }
            return true;
        }

        @Override
        public boolean charTyped(Host host, int cp) {
            if (!editing) return false;
            char ch = Character.toUpperCase((char) cp);
            if (Character.digit(ch, 16) >= 0 || ch == '#') {
                if (fresh) buffer = "";
                fresh = false;
                if (buffer.length() < 9) {
                    buffer += ch;
                    Sounds.play("tick", 1.4f);
                }
            }
            return true;
        }

        @Override
        public void stopEditing(Host host, boolean commit) {
            if (!editing) return;
            editing = false;
            if (!commit) return;
            String h = buffer.replace("#", "");
            int old = get.apply(cfg());
            try {
                int v;
                if (h.length() == 3) {
                    StringBuilder sb = new StringBuilder();
                    for (char ch : h.toCharArray()) sb.append(ch).append(ch);
                    v = (old & 0xFF000000) | Integer.parseInt(sb.toString(), 16);
                } else if (h.length() == 6) {
                    v = ((old >>> 24) == 0 ? 0xFF000000 : old & 0xFF000000) | Integer.parseInt(h, 16);
                } else if (h.length() == 8) {
                    v = (int) Long.parseLong(h, 16);
                } else {
                    throw new NumberFormatException();
                }
                set.accept(cfg(), v);
                Sounds.play("success", 1.2f);
                host.changed(this);
            } catch (NumberFormatException e) {
                Sounds.play("error");
            }
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Button extends Row {
        final Runnable action;
        final int color;

        public Button(String key, int color, Runnable action) {
            super(key);
            this.action = action;
            this.color = color;
        }

        @Override
        protected void label(Canvas c, Ctx ctx) {
            Component t = Theme.tr(key);
            Theme.centered(c, t, x + w / 2f + magX, y + height / 2f - 4.5f, ctx.hovered ? Theme.mix(color, 0xFFFFFFFF, 0.2f) : color);
        }

        @Override
        protected boolean clickControl(Host host, double mx, double my, int button, boolean shift) {
            action.run();
            Sounds.play("click");
            host.changed(this);
            return true;
        }
    }
}
