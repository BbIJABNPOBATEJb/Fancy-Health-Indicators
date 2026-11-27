package me.bbijabnpobatejb.fancyhealth.ui;

import me.bbijabnpobatejb.fancyhealth.compat.Mc;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.bbijabnpobatejb.fancyhealth.config.Anchor;
import me.bbijabnpobatejb.fancyhealth.config.Animation;
import me.bbijabnpobatejb.fancyhealth.config.BarStyle;
import me.bbijabnpobatejb.fancyhealth.config.ColorMode;
import me.bbijabnpobatejb.fancyhealth.config.ConfigStore;
import me.bbijabnpobatejb.fancyhealth.config.CritMark;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.config.FontChoice;
import me.bbijabnpobatejb.fancyhealth.config.HudPos;
import me.bbijabnpobatejb.fancyhealth.config.ManyHearts;
import me.bbijabnpobatejb.fancyhealth.config.NumberFormat;
import me.bbijabnpobatejb.fancyhealth.config.Occlusion;
import me.bbijabnpobatejb.fancyhealth.config.Presets;
import me.bbijabnpobatejb.fancyhealth.config.ShowWhen;
import me.bbijabnpobatejb.fancyhealth.config.SizeMode;
import me.bbijabnpobatejb.fancyhealth.config.TargetSource;
import me.bbijabnpobatejb.fancyhealth.config.TargetStyle;
import me.bbijabnpobatejb.fancyhealth.config.TextEffect;
import me.bbijabnpobatejb.fancyhealth.config.ValueText;
import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import me.bbijabnpobatejb.fancyhealth.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigUi implements Rows.Host {

    public static Integer debugMouseX;
    public static Integer debugMouseY;
    public static Boolean debugShift;
    public static java.util.function.Consumer<ConfigUi> frameHook;
    public static double renderMs;
    public static long fillCount;

    public enum Tab {
        PRESETS("✦", 0xFFFF9F0A), OVERHEAD("❤", 0xFFFF453A), CROSSHAIR("✛", 0xFF0A84FF), TARGET("▣", 0xFFBF5AF2),
        NUMBERS("7", 0xFFFFD60A), FILTERS("⚑", 0xFF30D158), GENERAL("⚙", 0xFF8E8E93);

        final String icon;
        final int color;

        Tab(String icon, int color) {
            this.icon = icon;
            this.color = color;
        }

        public String key() {
            return "fancyhealth.tab." + name().toLowerCase();
        }
    }

    private static Tab lastTab = Tab.PRESETS;
    private static final Map<Tab, Float> SCROLLS = new EnumMap<>(Tab.class);
    private static final String SHARE_PREFIX = "FHI1:";

    final Preview preview = new Preview();
    final Runnable onDone;
    final Map<Tab, List<Rows.Row>> tabs = new EnumMap<>(Tab.class);
    final Rows.Toggle modSwitch = new Rows.Toggle("fancyhealth.option.mod_enabled", c -> c.general.enabled, (c, v) -> c.general.enabled = v);
    Tab tab = lastTab;
    int width;
    int height;
    float sideW;
    float listX0;
    float listX1;
    float listY0;
    float listY1;
    float scroll;
    float scrollTarget;
    Rows.Row active;
    Rows.Row editing;
    Rows.Row lastHover;
    ColorPopup popup;
    boolean draggingBar;
    boolean dirty;
    long dirtyAt;
    long lastNanos;
    float time;
    float hlX0;
    float hlY0;
    float hlX1;
    float hlY1;
    float hlAlpha;
    float tabHl = -1;
    FhConfig defaults;
    String defaultsBase;
    String toast;
    int toastColor;
    float toastTime;
    float resetHover;
    float doneHover;
    boolean resetMark;
    boolean resetAllMark;
    public boolean shiftHeld;
    private static final float TABS_Y = 80;
    private static final float SEARCH_Y0 = 59;
    private static final float SEARCH_Y1 = 75;
    String query = "";
    boolean searchFocus;
    float searchAnim;
    final TextField field = new TextField(60);
    int searchFirst;
    boolean searchDrag;
    long searchClickAt;
    int searchClickPos = -1;
    boolean textInput;
    final List<Rows.Row> results = new ArrayList<>();
    final Map<Rows.Row, Tab> resultTab = new java.util.HashMap<>();
    int resultCount;
    Tab searchPreviewTab;

    public ConfigUi(Runnable onDone) {
        this.onDone = onDone;
        for (Tab t : Tab.values()) tabs.put(t, build(t));
        scroll = scrollTarget = SCROLLS.getOrDefault(tab, 0f);
    }

    private List<Rows.Row> rows() {
        return searching() ? results : tabs.get(tab);
    }

    public boolean searching() {
        return !query.isEmpty();
    }

    public void setQuery(String q) {
        field.reset(q);
        boolean was = searching();
        query = q;
        if (!was && searching()) SCROLLS.put(tab, scrollTarget);
        if (searching()) {
            scroll = scrollTarget = 0;
            refreshSearch();
        } else {
            results.clear();
            resultTab.clear();
            if (was) scroll = scrollTarget = SCROLLS.getOrDefault(tab, 0f);
        }
        active = null;
        lastHover = null;
        popup = null;
    }

    private void refreshSearch() {
        results.clear();
        resultTab.clear();
        List<Search.Hit> hits = Search.find(query, tabs, ConfigStore.get(), defaults());
        resultCount = hits.size();
        Tab groupTab = null;
        String groupSection = null;
        for (Search.Hit h : hits) {
            if (h.tab() != groupTab || !h.section().equals(groupSection)) {
                String title = Mc.translatable(h.tab().key()).getString();
                String sec = h.section().isEmpty() ? "" : Mc.translatable(h.section()).getString();
                if (!sec.isEmpty() && !sec.equalsIgnoreCase(title)) title += " · " + sec;
                results.add(new Rows.Caption(title, h.tab().color));
                groupTab = h.tab();
                groupSection = h.section();
            }
            results.add(h.row());
            resultTab.put(h.row(), h.tab());
        }
        searchPreviewTab = hits.isEmpty() ? tab : hits.get(0).tab();
    }

    public void resize(int w, int h) {
        width = w;
        height = h;
        sideW = Math.max(106, Math.min(124, w / 5.4f));
        float rest = w - sideW;
        float listW = Math.max(200, Math.min(300, rest * 0.52f));
        if (rest - listW < 150) listW = Math.max(176, rest - 150);
        listX0 = sideW;
        listX1 = sideW + listW;
        listY0 = 36;
        listY1 = h - 30;
        popup = null;
    }

    public Tab tab() {
        return tab;
    }

    public void select(Tab t) {
        if (t == tab) return;
        stopEditing(true);
        SCROLLS.put(tab, scrollTarget);
        tab = t;
        lastTab = t;
        scroll = scrollTarget = SCROLLS.getOrDefault(t, 0f);
        popup = null;
        lastHover = null;
        Sounds.play("tab");
        save();
    }

    private void save() {
        if (dirty) {
            ConfigStore.save();
            dirty = false;
        }
    }

    private void markDirty() {
        dirty = true;
        dirtyAt = System.currentTimeMillis();
    }

    public void close() {
        SCROLLS.put(tab, scrollTarget);
        stopEditing(true);
        releaseInput();
        dirty = true;
        save();
    }

    @Override
    public FhConfig defaults() {
        String base = ConfigStore.get().base;
        if (defaults == null || !base.equals(defaultsBase)) {
            defaults = Presets.create(base);
            defaultsBase = base;
        }
        return defaults;
    }

    @Override
    public void changed(Rows.Row row) {
        FhConfig cur = ConfigStore.get();
        if (row.inactive(cur) && row.activate != null) {
            row.activate.accept(cur);
            toast(Mc.translatable("fancyhealth.toast.parent_changed", Mc.translatable(row.parentKey)).getString(), Theme.GREEN);
        }
        if (!(row instanceof PresetRow)) ConfigStore.get().preset = "custom";
        markDirty();
        preview.react(row.key, ConfigStore.get());
    }

    @Override
    public void openColor(Rows.ColorRow row, float x, float y) {
        float px = Math.min(x - ColorPopup.W, width - ColorPopup.W - 6);
        float py = y + 2;
        if (py + ColorPopup.H > height - 6) py = y - 24 - ColorPopup.H;
        popup = new ColorPopup(row, this, Math.max(6, px), Math.max(6, py));
    }

    private void toast(String text, int color) {
        toast = text;
        toastColor = color;
        toastTime = 2.2f;
    }

    private float contentHeight() {
        float h = 6;
        for (Rows.Row r : rows()) h += r.height;
        return h + 8;
    }

    private float maxScroll() {
        return Math.max(0, contentHeight() - (listY1 - listY0));
    }

    private boolean shift() {
        return debugShift != null ? debugShift : shiftHeld;
    }

    public String fieldState() {
        return "q=" + query + "|c=" + field.cursor + "|a=" + field.anchor + "|focus=" + searchFocus + "|input=" + textInput
                + "|edit=" + (editing == null ? "-" : editing.editText());
    }

    public void focusSearch() {
        searchFocus = true;
    }

    public void releaseInput() {
        if (textInput) Mc.textInput(this, false);
        textInput = false;
    }

    public void syncInput() {
        boolean want = searchFocus || editing != null;
        if (want == textInput) return;
        textInput = want;
        Mc.textInput(this, want);
    }

    private void applyField() {
        if (!field.text.equals(query)) setQuery(field.text);
    }

    private float textX(String s, int i) {
        return Theme.width(Theme.ui(s.substring(0, i))) * 0.8f;
    }

    private int searchIndex(double mx) {
        String s = query.substring(Math.min(searchFirst, query.length()));
        float x = (float) mx - (6 + 17);
        int best = 0;
        float bestD = Float.MAX_VALUE;
        for (int i = 0; i <= s.length(); i++) {
            float d = Math.abs(textX(s, i) - x);
            if (d < bestD) {
                bestD = d;
                best = i;
            }
        }
        return Math.min(query.length(), searchFirst + best);
    }

    public void render(Canvas c, int mouseX, int mouseY) {
        syncInput();
        long start = System.nanoTime();
        draw(c, mouseX, mouseY);
        renderMs = renderMs * 0.9 + (System.nanoTime() - start) / 1e6 * 0.1;
        if (frameHook != null) frameHook.accept(this);
    }

    public List<Rows.Row> rowsOf(Tab t) {
        return tabs.get(t);
    }

    public float[] previewRect() {
        return new float[]{listX1, 20, width, height - 28};
    }

    private void draw(Canvas c, int mouseX, int mouseY) {
        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 0.016f : Math.min(0.1f, (now - lastNanos) / 1e9f);
        lastNanos = now;
        time += dt;
        if (dirty && System.currentTimeMillis() - dirtyAt > 1500) save();
        double mx = debugMouseX != null ? debugMouseX : mouseX;
        double my = debugMouseY != null ? debugMouseY : mouseY;
        double hmx = popup != null && popup.contains(mx, my) ? -1000 : mx;
        if (width > 0) scrollTarget = Math.max(0, Math.min(maxScroll(), scrollTarget));
        scroll += (scrollTarget - scroll) * Math.min(1f, dt * 16f);
        FhConfig cfg = ConfigStore.get();

        boolean overReset = Theme.in(hmx, my, 8, height - 46, sideW - 8, height - 30);
        boolean overDone = Theme.in(hmx, my, 8, height - 26, sideW - 8, height - 8);
        resetAllMark = overReset && shift();
        resetMark = overReset && !resetAllMark;

        sidebar(c, hmx, my, dt, overReset, overDone);

        c.fill((int) listX0, 0, (int) listX1, height, Theme.PANEL);
        Component title = Theme.bold(Mc.translatable(searching() ? "fancyhealth.search.title" : tab.key()).getString());
        float titleEnd = listX0 + 12 + Theme.width(title) * 1.25f + 8;
        Component pc = null;
        int pcColor = Theme.TEXT3;
        if (searching()) {
            pc = Theme.ui(Mc.translatable("fancyhealth.search.count", resultCount).getString());
        } else if (tab != Tab.PRESETS && !me.bbijabnpobatejb.fancyhealth.api.FancyHealthApi.active().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (me.bbijabnpobatejb.fancyhealth.api.Restriction r : me.bbijabnpobatejb.fancyhealth.api.FancyHealthApi.active()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(Mc.translatable("fancyhealth.restriction." + r.name().toLowerCase(java.util.Locale.ROOT)).getString());
            }
            pc = Theme.ui(Mc.translatable("fancyhealth.server.limited", sb.toString()).getString());
            pcColor = Theme.ORANGE;
        } else if (tab != Tab.PRESETS) {
            pc = Theme.ui(Mc.translatable("fancyhealth.label.preset", Mc.translatable("fancyhealth.preset." + cfg.preset)).getString());
        }
        if (tab == Tab.PRESETS && !searching()) {
            float importX0 = listX1 - 10 - buttonWidth("fancyhealth.button.import");
            float exportX0 = importX0 - 6 - buttonWidth("fancyhealth.button.export");
            if (titleEnd <= exportX0) Theme.scaled(c, title, listX0 + 12, 11, 1.25f, Theme.TEXT);
            headerButton(c, "fancyhealth.button.import", importX0, listX1 - 10, hmx, my);
            headerButton(c, "fancyhealth.button.export", exportX0, importX0 - 6, hmx, my);
        } else {
            Theme.scaled(c, title, listX0 + 12, 11, 1.25f, Theme.TEXT);
            if (pc != null && titleEnd <= listX1 - 10 - Theme.width(pc) * 0.8f) Theme.small(c, pc, listX1 - 10 - Theme.width(pc) * 0.8f, 15, pcColor);
        }

        c.scissor((int) listX0, (int) listY0, (int) listX1, (int) listY1);
        float y = listY0 + 4 - scroll;
        float rx = listX0 + 8, rw = listX1 - listX0 - 20;
        List<Rows.Row> rows = rows();
        for (Rows.Row r : rows) {
            r.x = rx;
            r.y = y;
            r.w = rw;
            y += r.height;
        }
        for (int i = 0; i < rows.size(); i++) {
            Rows.Row r = rows.get(i);
            if (!r.card()) continue;
            int j = i;
            while (j + 1 < rows.size() && rows.get(j + 1).card()) j++;
            Rows.Row last = rows.get(j);
            Theme.rrect(c, rx, r.y, rx + rw, last.y + last.height, 10, Theme.CARD);
            for (int k = i; k < j; k++) {
                Rows.Row a = rows.get(k);
                c.fill((int) (rx + 12), (int) (a.y + a.height), (int) (rx + rw), (int) (a.y + a.height) + 1, Theme.SEPARATOR);
            }
            i = j;
        }
        Rows.Row hovered = null;
        for (Rows.Row r : rows) {
            if (r.descKey() != null && Theme.in(hmx, my, r.x, Math.max(r.y, listY0), r.x + r.w, Math.min(r.y + r.height, listY1))) hovered = r;
        }
        if (hovered != lastHover) {
            if (hovered != null && popup == null) Sounds.play("hover");
            lastHover = hovered;
        }
        Rows.Row hl = active != null ? active : hovered;
        if (hl != null && hl.card()) {
            float k = Math.min(1f, dt * 20f);
            if (hlAlpha <= 0.01f) {
                hlX0 = hl.x + 2;
                hlY0 = hl.y + 1;
                hlX1 = hl.x + hl.w - 2;
                hlY1 = hl.y + hl.height - 1;
            }
            hlX0 += (hl.x + 2 - hlX0) * k;
            hlY0 += (hl.y + 1 - hlY0) * k;
            hlX1 += (hl.x + hl.w - 2 - hlX1) * k;
            hlY1 += (hl.y + hl.height - 1 - hlY1) * k;
            hlAlpha = Math.min(1f, hlAlpha + dt * 8f);
        } else {
            hlAlpha = Math.max(0f, hlAlpha - dt * 6f);
        }
        if (hlAlpha > 0.01f) {
            float px = (float) Math.max(-1.5, Math.min(1.5, (hmx - (hlX0 + hlX1) / 2) * 0.01));
            Theme.rrect(c, hlX0 + px, hlY0, hlX1 + px, hlY1, 8, (int) (hlAlpha * 0x14) << 24 | 0xFFFFFF);
        }
        for (Rows.Row r : rows) {
            if (r.y + r.height < listY0 || r.y > listY1) continue;
            r.render(c, this, new Rows.Ctx(hmx, my, dt, time, r == hovered || r == active, resetMark || resetAllMark));
        }
        if (searching() && results.isEmpty()) {
            float cx = (listX0 + listX1) / 2f, cy = listY0 + 60;
            Theme.icon(c, 10, cx, cy, 22, Theme.TEXT3);
            Theme.centered(c, Theme.bold(Mc.translatable("fancyhealth.search.empty").getString()), cx, cy + 20, Theme.TEXT2);
            List<String> tl = Theme.wrap(Mc.translatable("fancyhealth.search.try").getString(), (int) ((listX1 - listX0 - 30) / 0.8f), 3);
            for (int i = 0; i < tl.size(); i++) {
                Component line = Theme.ui(tl.get(i));
                Theme.small(c, line, cx - Theme.width(line) * 0.4f, cy + 34 + i * 10, Theme.TEXT3);
            }
        }
        c.endScissor();

        float max = maxScroll();
        if (max > 0) {
            float trackH = listY1 - listY0;
            float barH = Math.max(18, trackH * (trackH / contentHeight()));
            float barY = listY0 + (trackH - barH) * (scroll / max);
            boolean overBar = hmx >= listX1 - 8 && hmx < listX1;
            Theme.rrect(c, listX1 - (overBar || draggingBar ? 6 : 5), barY + 2, listX1 - 3, barY + barH - 2, 1.5f, overBar || draggingBar ? 0xA0FFFFFF : 0x50FFFFFF);
        }

        c.fill((int) listX0, (int) listY1, (int) listX1, height, 0xF8080809);
        c.fill((int) listX0, (int) listY1, (int) listX1, (int) listY1 + 1, Theme.SEPARATOR);
        String desc;
        int descColor = Theme.TEXT2;
        if (overReset) {
            desc = resetAllMark ? Mc.translatable("fancyhealth.hint.reset_all").getString()
                    : Mc.translatable("fancyhealth.hint.reset", Mc.translatable(tab.key())).getString();
            descColor = Theme.GREEN;
        } else {
            Rows.Row info = active != null ? active : hovered;
            String dk = info != null ? info.descKey() : null;
            boolean has = dk != null && Language.getInstance().has(dk);
            desc = Mc.translatable(has ? dk : "fancyhealth.hint.general").getString();
            if (!has) descColor = Theme.TEXT3;
            if (info != null && info.inactive(cfg)) {
                desc = Mc.translatable("fancyhealth.hint.needs", Mc.translatable(info.parentKey)).getString() + (has ? " " + desc : "");
                descColor = Theme.ORANGE;
            }
        }
        List<String> lines = Theme.wrap(desc, (int) ((listX1 - listX0 - 20) / 0.8f), 2);
        for (int i = 0; i < lines.size(); i++) Theme.small(c, Theme.ui(lines.get(i)), listX0 + 10, listY1 + 5 + i * 10, descColor);

        Tab previewTab = tab;
        if (searching()) {
            Rows.Row src = active != null ? active : hovered;
            Tab t = src != null ? resultTab.get(src) : null;
            if (t != null) searchPreviewTab = t;
            if (searchPreviewTab != null) previewTab = searchPreviewTab;
        }
        preview.render(c, cfg, previewTab, listX1, 0, width, height, hmx, my, dt);

        if (toast != null && toastTime > 0) {
            toastTime -= dt;
            float a = Math.max(0, Math.min(1f, toastTime * 3f) * Math.min(1f, (2.2f - toastTime) * 6f));
            Component t = Theme.ui(toast);
            float tw = Theme.width(t) + 20;
            float tx = (listX0 + listX1) / 2f - tw / 2f, ty = listY0 + 4 - (1 - a) * 6;
            Theme.shadow(c, tx, ty, tx + tw, ty + 18, 9, (int) (120 * a));
            Theme.rrect(c, tx, ty, tx + tw, ty + 18, 9, (int) (a * 0xF0) << 24 | 0x2C2C2E);
            Theme.text(c, t, tx + 10, ty + 5, Colors.alpha(toastColor, a));
        }
        updateCard(c, hmx, my, dt);
        if (popup != null) popup.render(c, mx, my, dt);
    }

    private float buttonWidth(String key) {
        return Theme.width(Theme.tr(key)) * 0.85f + 14;
    }

    private void headerButton(Canvas c, String key, float x0, float x1, double mx, double my) {
        float y0 = 9, y1 = 25;
        boolean over = Theme.in(mx, my, x0, y0, x1, y1);
        Theme.rrect(c, x0, y0, x1, y1, 8, over ? 0xFF3A3A3C : 0xFF2C2C2E);
        Theme.scaled(c, Theme.tr(key), x0 + 7, y0 + 4.5f, 0.85f, Theme.BLUE);
    }

    private void sidebar(Canvas c, double mx, double my, float dt, boolean overReset, boolean overDone) {
        c.fill(0, 0, (int) sideW, height, Theme.SIDEBAR);
        c.fill((int) sideW - 1, 0, (int) sideW, height, Theme.SEPARATOR);
        Theme.text(c, Theme.bold("Fancy Health"), 10, 9, Theme.TEXT);
        Theme.small(c, Theme.tr("fancyhealth.subtitle"), 10, 20, Theme.TEXT2);

        Theme.rrect(c, 6, 32, sideW - 6, 54, 9, Theme.CARD);
        modSwitch.x = 4;
        modSwitch.y = 32;
        modSwitch.w = sideW - 8;
        modSwitch.render(c, this, new Rows.Ctx(mx, my, dt, time, Theme.in(mx, my, 6, 32, sideW - 6, 54), false));
        searchField(c, mx, my, dt);

        float step = tabStep();
        float ty = TABS_Y;
        float target = ty + tab.ordinal() * step;
        tabHl = tabHl < 0 ? target : tabHl + (target - tabHl) * Math.min(1f, dt * 18f);
        Theme.rrect(c, 6, tabHl, sideW - 6, tabHl + step - 2, 8, searching() ? 0x400A84FF : Theme.BLUE);
        for (Tab t : Tab.values()) {
            boolean selected = t == tab;
            float h = step - 2, mid = ty + h / 2f;
            boolean hover = Theme.in(mx, my, 6, ty, sideW - 6, ty + h);
            if (hover && !selected) Theme.rrect(c, 6, ty, sideW - 6, ty + h, 8, 0x14FFFFFF);
            selected &= !searching();
            Theme.rrect(c, 11, mid - 6, 23, mid + 6, 3.5f, selected ? 0x40FFFFFF : t.color);
            Theme.icon(c, t.ordinal(), 17, mid, 9, 0xFFFFFFFF);
            Theme.fit(c, Theme.tr(t.key()), 28, mid - 4, sideW - 42, selected ? 0xFFFFFFFF : Theme.TEXT, hover, time, 0);
            if ((resetAllMark || resetMark && t == tab) && tabModified(t)) Theme.circle(c, sideW - 13, mid, 3, Theme.GREEN);
            ty += step;
        }

        resetHover += ((overReset ? 1 : 0) - resetHover) * Math.min(1f, dt * 14f);
        doneHover += ((overDone ? 1 : 0) - doneHover) * Math.min(1f, dt * 14f);
        if (resetHover > 0.02f) Theme.rrect(c, 8, height - 46, sideW - 8, height - 30, 8, (int) (resetHover * 0x26) << 24 | 0xFF453A);
        Component rs = Theme.tr(resetAllMark ? "fancyhealth.button.reset_all" : "fancyhealth.button.reset_tab");
        Theme.centered(c, rs, sideW / 2f, height - 42, Theme.RED);
        float lift = doneHover * 0.8f;
        Theme.shadow(c, 8, height - 26 - lift, sideW - 8, height - 8 - lift, 9, (int) (60 + 60 * doneHover));
        Theme.rrect(c, 8, height - 26 - lift, sideW - 8, height - 8 - lift, 9, Theme.mix(Theme.BLUE, 0xFF409CFF, doneHover));
        Theme.centered(c, Theme.bold(Mc.translatable("fancyhealth.button.done").getString()), sideW / 2f, height - 21 - lift, 0xFFFFFFFF);
    }

    private float tabStep() {
        return Math.max(15, Math.min(21, (height - TABS_Y - 52) / (float) Tab.values().length));
    }

    private void searchField(Canvas c, double mx, double my, float dt) {
        float x0 = 6, x1 = sideW - 6, y0 = SEARCH_Y0, y1 = SEARCH_Y1, cy = (y0 + y1) / 2f;
        boolean hover = Theme.in(mx, my, x0, y0, x1, y1);
        searchAnim += ((searchFocus ? 1 : 0) - searchAnim) * Math.min(1f, dt * 14f);
        if (searchAnim > 0.02f) Theme.rrect(c, x0 - 1.5f, y0 - 1.5f, x1 + 1.5f, y1 + 1.5f, 9.5f, (int) (searchAnim * 0x90) << 24 | 0x0A84FF);
        Theme.rrect(c, x0, y0, x1, y1, 8, searchFocus ? 0xFF2C2C2E : hover ? 0xFF29292C : 0xFF222224);
        Theme.icon(c, 10, x0 + 9, cy, 8, searching() ? Theme.TEXT2 : Theme.TEXT3);
        float tx = x0 + 17, maxW = x1 - tx - (searching() ? 14 : 4);
        if (!searching()) {
            String ph = Mc.translatable("fancyhealth.search.placeholder").getString();
            Theme.small(c, Theme.ui(ph), tx, cy - 3.5f, Theme.TEXT3);
            if (searchFocus && (time % 1f) < 0.55f) Theme.rrect(c, tx - 0.5f, cy - 4.5f, tx + 0.5f, cy + 4.5f, 0, Theme.BLUE);
        } else {
            int cur = field.cursor;
            searchFirst = Math.min(searchFirst, cur);
            while (searchFirst < cur && textX(query.substring(searchFirst), cur - searchFirst) > maxW) searchFirst++;
            while (searchFirst > 0 && textX(query.substring(searchFirst - 1), query.length() - searchFirst + 1) <= maxW) searchFirst--;
            String rest = query.substring(searchFirst);
            int end = rest.length();
            while (end > 0 && textX(rest, end) > maxW) end--;
            String shown = rest.substring(0, end);
            if (searchFocus && field.hasSelection()) {
                int a = Math.max(0, Math.min(end, field.selStart() - searchFirst)), b = Math.max(0, Math.min(end, field.selEnd() - searchFirst));
                if (b > a) Theme.rrect(c, tx + textX(shown, a), cy - 5, tx + textX(shown, b) + 0.5f, cy + 4.5f, 1, 0x800A84FF);
            }
            Theme.small(c, Theme.ui(shown), tx, cy - 3.5f, Theme.TEXT);
            float caret = tx + textX(rest, Math.max(0, Math.min(rest.length(), cur - searchFirst))) + 0.5f;
            if (searchFocus && (time % 1f) < 0.55f) Theme.rrect(c, caret - 0.5f, cy - 4.5f, caret + 0.5f, cy + 4.5f, 0, Theme.BLUE);
            boolean overX = Theme.in(mx, my, x1 - 13, y0, x1, y1);
            Theme.circle(c, x1 - 7, cy, 5, overX ? 0xFF8E8E93 : 0xFF5A5A5F);
            Theme.icon(c, 9, x1 - 7, cy, 6, 0xFF1C1C1E);
        }
    }

    private boolean tabModified(Tab t) {
        FhConfig cur = ConfigStore.get(), def = defaults();
        if (t == Tab.PRESETS) return "custom".equals(cur.preset);
        for (Rows.Row r : tabs.get(t)) if (r.value() && r.modified(cur, def)) return true;
        return false;
    }

    private void stopEditing(boolean commit) {
        if (editing != null) {
            editing.stopEditing(this, commit);
            editing = null;
        }
    }

    private static final float CARD_W = 178;
    private static final float CARD_H = 52;
    private float updateAnim;
    private String updateShown;

    private float[] cardRect() {
        float x0 = width - 8 - CARD_W + (1 - updateAnim) * (CARD_W + 12), y0 = height - 8 - CARD_H;
        return new float[]{x0, y0, x0 + CARD_W, y0 + CARD_H};
    }

    private float[][] cardButtons() {
        float[] r = cardRect();
        String[] keys = {"fancyhealth.update.download", "fancyhealth.update.hide", "fancyhealth.update.never"};
        float[][] out = new float[3][];
        float x = r[0] + 10;
        for (int i = 0; i < 3; i++) {
            float w = Theme.width(Theme.tr(keys[i])) * 0.8f + 14;
            out[i] = new float[]{x, r[3] - 20, x + w, r[3] - 7};
            x += w + 5;
        }
        return out;
    }

    private void updateCard(Canvas c, double mx, double my, float dt) {
        String latest = UpdateCheck.latest();
        if (latest != null) updateShown = latest;
        updateAnim += ((latest != null ? 1 : 0) - updateAnim) * Math.min(1f, dt * 9f);
        if (updateAnim < 0.01f || updateShown == null) return;
        float[] r = cardRect();
        Theme.shadow(c, r[0], r[1], r[2], r[3], 12, (int) (150 * updateAnim));
        Theme.rrect(c, r[0], r[1], r[2], r[3], 12, 0xF62C2C2E);
        Theme.icon(c, 0, r[0] + 16, r[1] + 13, 9, Theme.ORANGE);
        Theme.text(c, Theme.bold(Mc.translatable("fancyhealth.update.title", updateShown).getString()), r[0] + 25, r[1] + 9, Theme.TEXT);
        String[] keys = {"fancyhealth.update.download", "fancyhealth.update.hide", "fancyhealth.update.never"};
        float[][] b = cardButtons();
        for (int i = 0; i < 3; i++) {
            boolean over = Theme.in(mx, my, b[i][0], b[i][1], b[i][2], b[i][3]);
            int bg = i == 0 ? (over ? 0xFF409CFF : Theme.BLUE) : (over ? 0xFF48484A : 0xFF3A3A3C);
            Theme.rrect(c, b[i][0], b[i][1], b[i][2], b[i][3], 6, bg);
            Theme.small(c, Theme.tr(keys[i]), b[i][0] + 7, b[i][1] + 3.5f, i == 0 ? 0xFFFFFFFF : Theme.TEXT);
        }
    }

    private boolean clickUpdate(double mx, double my) {
        if (updateAnim < 0.5f || UpdateCheck.latest() == null) return false;
        float[] r = cardRect();
        if (!Theme.in(mx, my, r[0], r[1], r[2], r[3])) return false;
        float[][] b = cardButtons();
        if (Theme.in(mx, my, b[0][0], b[0][1], b[0][2], b[0][3]) && UpdateCheck.url() != null) {
            me.bbijabnpobatejb.fancyhealth.FancyHealth.platform().openUrl(UpdateCheck.url());
            UpdateCheck.hidden = true;
        } else if (Theme.in(mx, my, b[1][0], b[1][1], b[1][2], b[1][3])) {
            UpdateCheck.hidden = true;
        } else if (Theme.in(mx, my, b[2][0], b[2][1], b[2][2], b[2][3])) {
            ConfigStore.get().general.updateCheck = false;
            markDirty();
        } else {
            return true;
        }
        Sounds.play("click");
        return true;
    }

    public boolean mouseDown(double mx, double my, int button, boolean shiftDown) {
        if (clickUpdate(mx, my)) return true;
        if (searchFocus && !(mx < sideW && my >= SEARCH_Y0 && my < SEARCH_Y1)) searchFocus = false;
        if (editing != null) {
            boolean inside = Theme.in(mx, my, editing.x, editing.y, editing.x + editing.w, editing.y + editing.height);
            if (!inside || button != 1) stopEditing(true);
            if (!inside) return true;
        }
        if (popup != null) {
            if (popup.click(mx, my)) return true;
            popup = null;
            return true;
        }
        if (mx < sideW) {
            if (Theme.in(mx, my, 6, 32, sideW - 6, 54)) {
                modSwitch.click(this, modSwitch.x + modSwitch.w - 12, my, 0, false);
                return true;
            }
            if (Theme.in(mx, my, 6, SEARCH_Y0, sideW - 6, SEARCH_Y1)) {
                if (searching() && mx >= sideW - 19) {
                    setQuery("");
                    Sounds.play("click");
                    searchFocus = true;
                    return true;
                }
                int at = searchIndex(mx);
                long now = System.currentTimeMillis();
                if (searchFocus && at == searchClickPos && now - searchClickAt < 350) {
                    field.selectWord(at);
                    searchClickAt = 0;
                } else {
                    field.moveTo(at, shiftDown && searchFocus);
                    searchClickAt = now;
                    searchDrag = true;
                }
                searchClickPos = at;
                searchFocus = true;
                return true;
            }
            float ty = TABS_Y, step = tabStep();
            for (Tab t : Tab.values()) {
                if (my >= ty && my < ty + step - 2) {
                    searchFocus = false;
                    if (searching()) setQuery("");
                    select(t);
                    return true;
                }
                ty += step;
            }
            if (Theme.in(mx, my, 8, height - 26, sideW - 8, height - 8)) {
                close();
                Sounds.play("close");
                onDone.run();
                return true;
            }
            if (Theme.in(mx, my, 8, height - 46, sideW - 8, height - 30)) {
                if (shiftDown || shift()) resetAll();
                else resetTab();
                return true;
            }
            return true;
        }
        if (mx >= listX0 && mx < listX1) {
            if (my < listY0) {
                if (tab == Tab.PRESETS && !searching()) {
                    float importX0 = listX1 - 10 - buttonWidth("fancyhealth.button.import");
                    float exportX1 = importX0 - 6, exportX0 = exportX1 - buttonWidth("fancyhealth.button.export");
                    if (Theme.in(mx, my, importX0, 9, listX1 - 10, 25)) importClipboard();
                    else if (Theme.in(mx, my, exportX0, 9, exportX1, 25)) exportClipboard();
                }
                return true;
            }
            if (mx >= listX1 - 8 && my >= listY0 && my < listY1 && maxScroll() > 0) {
                draggingBar = true;
                dragBar(my);
                return true;
            }
            if (my >= listY1) return true;
            for (Rows.Row r : rows()) {
                if (my >= r.y && my < r.y + r.height) {
                    if (r.click(this, mx, my, button, shiftDown)) {
                        active = r;
                        if (r.editing()) editing = r;
                    }
                    return true;
                }
            }
            return true;
        }
        boolean handled = preview.click(mx, my, button, ConfigStore.get(), searching() && searchPreviewTab != null ? searchPreviewTab : tab);
        if (preview.changed) {
            preview.changed = false;
            ConfigStore.get().preset = "custom";
            markDirty();
        }
        return handled;
    }

    private void resetTab() {
        FhConfig c = ConfigStore.get();
        if (tab == Tab.PRESETS) {
            ConfigStore.set(Presets.apply(c, c.base));
        } else {
            FhConfig def = defaults();
            for (Rows.Row r : rows()) if (r.value() && r.modified(c, def)) r.reset(c, def);
            if (!anyModified(c, def)) c.preset = c.base;
        }
        markDirty();
        Sounds.play("reset");
        toast(Mc.translatable("fancyhealth.toast.reset_tab", Mc.translatable(tab.key())).getString(), Theme.GREEN);
        preview.react("reset", ConfigStore.get());
    }

    private boolean anyModified(FhConfig c, FhConfig def) {
        for (Tab t : Tab.values()) {
            if (t == Tab.PRESETS) continue;
            for (Rows.Row r : tabs.get(t)) if (r.value() && r.modified(c, def)) return true;
        }
        return false;
    }

    private void resetAll() {
        ConfigStore.set(Presets.create("classic"));
        markDirty();
        Sounds.play("reset", 0.8f);
        toast(Mc.translatable("fancyhealth.toast.reset_all").getString(), Theme.GREEN);
        preview.react("reset", ConfigStore.get());
    }

    private void exportClipboard() {
        try {
            byte[] json = ConfigStore.toJson(ConfigStore.get()).getBytes(StandardCharsets.UTF_8);
            Deflater d = new Deflater(Deflater.BEST_COMPRESSION);
            d.setInput(json);
            d.finish();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            while (!d.finished()) out.write(buf, 0, d.deflate(buf));
            d.end();
            Minecraft.getInstance().keyboardHandler.setClipboard(SHARE_PREFIX + Base64.getEncoder().encodeToString(out.toByteArray()));
            Sounds.play("success");
            toast(Mc.translatable("fancyhealth.toast.copied").getString(), Theme.GREEN);
        } catch (RuntimeException e) {
            Sounds.play("error");
            toast(Mc.translatable("fancyhealth.toast.error").getString(), Theme.RED);
        }
    }

    private void importClipboard() {
        try {
            String s = Minecraft.getInstance().keyboardHandler.getClipboard().trim();
            String json;
            if (s.startsWith(SHARE_PREFIX)) {
                byte[] data = Base64.getDecoder().decode(s.substring(SHARE_PREFIX.length()));
                Inflater inf = new Inflater();
                inf.setInput(data);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                while (!inf.finished()) {
                    int n = inf.inflate(buf);
                    if (n == 0 && (inf.needsInput() || inf.needsDictionary())) break;
                    out.write(buf, 0, n);
                }
                inf.end();
                json = out.toString(StandardCharsets.UTF_8);
            } else if (s.startsWith("{")) {
                json = s;
            } else {
                throw new IllegalArgumentException();
            }
            FhConfig c = ConfigStore.fromJson(json);
            if (c == null) throw new IllegalArgumentException();
            ConfigStore.set(c);
            markDirty();
            Sounds.play("success");
            toast(Mc.translatable("fancyhealth.toast.imported").getString(), Theme.GREEN);
            preview.react("import", c);
        } catch (IllegalArgumentException | DataFormatException | com.google.gson.JsonParseException e) {
            Sounds.play("error");
            toast(Mc.translatable("fancyhealth.toast.bad_clipboard").getString(), Theme.RED);
        }
    }

    private void dragBar(double my) {
        float t = (float) ((my - listY0) / (listY1 - listY0));
        scrollTarget = scroll = Math.max(0, Math.min(1, t)) * maxScroll();
    }

    public boolean mouseDrag(double mx, double my) {
        if (searchDrag) {
            field.moveTo(searchIndex(mx), true);
            return true;
        }
        if (popup != null) {
            popup.drag(mx, my);
            return true;
        }
        if (draggingBar) {
            dragBar(my);
            return true;
        }
        if (active != null) {
            active.drag(this, mx, my);
            return true;
        }
        return false;
    }

    public boolean mouseUp(double mx, double my, int button) {
        searchDrag = false;
        if (popup != null) popup.release();
        if (active != null) active.release();
        active = null;
        draggingBar = false;
        return true;
    }

    public boolean scroll(double mx, double my, double amount) {
        if (mx >= listX0 && mx < listX1) {
            scrollTarget = (float) Math.max(0, Math.min(maxScroll(), scrollTarget - amount * 28));
            return true;
        }
        return false;
    }

    public boolean keyPressed(int key, boolean ctrl, boolean shift) {
        if (key == me.bbijabnpobatejb.fancyhealth.compat.Keys.LSHIFT || key == me.bbijabnpobatejb.fancyhealth.compat.Keys.RSHIFT) shiftHeld = true;
        if (editing != null) {
            if (ctrl && key == me.bbijabnpobatejb.fancyhealth.compat.Keys.A) {
                editing.selectAll();
                return true;
            }
            if (ctrl && key == me.bbijabnpobatejb.fancyhealth.compat.Keys.C) {
                Mc.setClipboard(editing.editText());
                return true;
            }
            if (ctrl && key == me.bbijabnpobatejb.fancyhealth.compat.Keys.V) {
                Mc.clipboard().trim().codePoints().forEach(cp -> editing.charTyped(this, cp));
                return true;
            }
            editing.keyPressed(this, key);
            if (!editing.editing()) editing = null;
            return true;
        }
        if (popup != null && key == me.bbijabnpobatejb.fancyhealth.compat.Keys.ESCAPE) {
            popup = null;
            return true;
        }
        if (ctrl && key == me.bbijabnpobatejb.fancyhealth.compat.Keys.F) {
            searchFocus = true;
            field.selectAll();
            Sounds.play("click", 1.3f);
            return true;
        }
        if (key == me.bbijabnpobatejb.fancyhealth.compat.Keys.ESCAPE && (searchFocus || searching())) {
            if (searching()) setQuery("");
            else searchFocus = false;
            return true;
        }
        if (searchFocus) {
            if (field.key(key, ctrl, shift)) {
                applyField();
                return true;
            }
            if (key == me.bbijabnpobatejb.fancyhealth.compat.Keys.RETURN || key == me.bbijabnpobatejb.fancyhealth.compat.Keys.NUMPAD_ENTER) {
                searchFocus = false;
                return true;
            }
            return key != me.bbijabnpobatejb.fancyhealth.compat.Keys.TAB;
        }
        return false;
    }

    public void keyReleased(int key) {
        if (key == me.bbijabnpobatejb.fancyhealth.compat.Keys.LSHIFT || key == me.bbijabnpobatejb.fancyhealth.compat.Keys.RSHIFT) shiftHeld = false;
    }

    public boolean charTyped(int cp) {
        if (editing != null) {
            editing.charTyped(this, cp);
            return true;
        }
        if (popup != null || !searchFocus) return false;
        if (!field.type(cp)) return false;
        applyField();
        return true;
    }

    public void open() {
        Sounds.play("open");
        UpdateCheck.start();
    }

    private static String k(String s) {
        return "fancyhealth.option." + s;
    }

    private List<Rows.Row> build(Tab t) {
        List<Rows.Row> r = new ArrayList<>();
        switch (t) {
            case PRESETS -> {
                for (String id : Presets.ids()) r.add(new PresetRow(id));
            }
            case OVERHEAD -> {
                r.add(new Rows.Header(k("section.placement")));
                r.add(new Rows.Toggle(k("enabled"), c -> c.overhead.enabled, (c, v) -> c.overhead.enabled = v));
                r.add(new Rows.Cycle<>(k("anchor"), Anchor.values(), c -> c.overhead.anchor, (c, v) -> c.overhead.anchor = v));
                r.add(new Rows.Slider(k("y_offset"), -1f, 2f, 0.05f, "", c -> c.overhead.yOffset, (c, v) -> c.overhead.yOffset = v));
                r.add(new Rows.Cycle<>(k("size_mode"), SizeMode.values(), c -> c.overhead.sizeMode, (c, v) -> c.overhead.sizeMode = v));
                r.add(new Rows.Slider(k("scale"), 0.3f, 3f, 0.05f, "x", c -> c.overhead.scale, (c, v) -> c.overhead.scale = v));
                r.add(new Rows.Cycle<>(k("show_when"), ShowWhen.values(), c -> c.overhead.showWhen, (c, v) -> c.overhead.showWhen = v));
                r.add(new Rows.Slider(k("linger"), 1f, 30f, 0.5f, "s", c -> c.overhead.lingerSeconds, (c, v) -> c.overhead.lingerSeconds = v).needs(k("show_when"), c -> c.overhead.showWhen == ShowWhen.DAMAGED || c.overhead.showWhen == ShowWhen.RECENT, c -> c.overhead.showWhen = ShowWhen.RECENT));
                r.add(new Rows.Slider(k("max_distance"), 4f, 128f, 1f, "m", c -> c.overhead.maxDistance, (c, v) -> c.overhead.maxDistance = v));
                look(r, c -> c.overhead.look);
            }
            case CROSSHAIR -> {
                r.add(new Rows.Header(k("section.placement")));
                r.add(new Rows.Toggle(k("enabled"), c -> c.crosshair.enabled, (c, v) -> c.crosshair.enabled = v));
                r.add(new Rows.Slider(k("offset_y"), -80f, 120f, 1f, "px", c -> (float) c.crosshair.offsetY, (c, v) -> c.crosshair.offsetY = Math.round(v)));
                r.add(new Rows.Slider(k("scale"), 0.3f, 3f, 0.05f, "x", c -> c.crosshair.scale, (c, v) -> c.crosshair.scale = v));
                r.add(new Rows.Slider(k("range"), 3f, 128f, 1f, "m", c -> c.crosshair.range, (c, v) -> c.crosshair.range = v));
                r.add(new Rows.Slider(k("linger"), 0f, 10f, 0.1f, "s", c -> c.crosshair.lingerSeconds, (c, v) -> c.crosshair.lingerSeconds = v));
                look(r, c -> c.crosshair.look);
            }
            case TARGET -> {
                r.add(new Rows.Header(k("section.placement")));
                r.add(new Rows.Toggle(k("enabled"), c -> c.target.enabled, (c, v) -> c.target.enabled = v));
                r.add(new Rows.Cycle<>(k("target_style"), TargetStyle.values(), c -> c.target.style, (c, v) -> c.target.style = v));
                r.add(new Rows.Cycle<>(k("position"), HudPos.values(), c -> c.target.position, (c, v) -> c.target.position = v));
                r.add(new Rows.Slider(k("offset_x"), -300f, 300f, 1f, "px", c -> (float) c.target.offsetX, (c, v) -> c.target.offsetX = Math.round(v)));
                r.add(new Rows.Slider(k("offset_y"), 0f, 200f, 1f, "px", c -> (float) c.target.offsetY, (c, v) -> c.target.offsetY = Math.round(v)));
                r.add(new Rows.Slider(k("scale"), 0.4f, 3f, 0.05f, "x", c -> c.target.scale, (c, v) -> c.target.scale = v));
                r.add(new Rows.Toggle(k("portrait"), c -> c.target.portrait, (c, v) -> c.target.portrait = v));
                r.add(new Rows.Toggle(k("show_distance"), c -> c.target.showDistance, (c, v) -> c.target.showDistance = v));
                r.add(new Rows.Cycle<>(k("source"), TargetSource.values(), c -> c.target.source, (c, v) -> c.target.source = v));
                r.add(new Rows.Slider(k("range"), 3f, 128f, 1f, "m", c -> c.target.range, (c, v) -> c.target.range = v));
                r.add(new Rows.Slider(k("linger"), 0f, 20f, 0.5f, "s", c -> c.target.lingerSeconds, (c, v) -> c.target.lingerSeconds = v));
                look(r, c -> c.target.look);
            }
            case NUMBERS -> {
                r.add(new Rows.Header(k("section.animation")));
                r.add(new Rows.Toggle(k("enabled"), c -> c.numbers.enabled, (c, v) -> c.numbers.enabled = v));
                r.add(new Rows.Cycle<>(k("animation"), Animation.values(), c -> c.numbers.animation, (c, v) -> c.numbers.animation = v));
                r.add(new Rows.Cycle<>(k("crit_animation"), Animation.values(), c -> c.numbers.critAnimation, (c, v) -> c.numbers.critAnimation = v));
                r.add(new Rows.Slider(k("lifetime"), 0.3f, 3f, 0.05f, "s", c -> c.numbers.lifetime, (c, v) -> c.numbers.lifetime = v));
                r.add(new Rows.Slider(k("merge"), 0f, 1000f, 25f, "ms", c -> (float) c.numbers.mergeMillis, (c, v) -> c.numbers.mergeMillis = Math.round(v)));
                r.add(new Rows.Header(k("section.text")));
                r.add(new Rows.Cycle<>(k("font"), FontChoice.values(), c -> c.numbers.font, (c, v) -> c.numbers.font = v));
                r.add(new Rows.Cycle<>(k("text_effect"), TextEffect.values(), c -> c.numbers.effect, (c, v) -> c.numbers.effect = v));
                r.add(new Rows.ColorRow(k("outline_color"), c -> c.numbers.outlineColor, (c, v) -> c.numbers.outlineColor = v));
                r.add(new Rows.Cycle<>(k("format"), NumberFormat.values(), c -> c.numbers.format, (c, v) -> c.numbers.format = v));
                r.add(new Rows.Cycle<>(k("crit_mark"), CritMark.values(), c -> c.numbers.critMark, (c, v) -> c.numbers.critMark = v));
                r.add(new Rows.Slider(k("scale"), 0.4f, 4f, 0.05f, "x", c -> c.numbers.scale, (c, v) -> c.numbers.scale = v));
                r.add(new Rows.Slider(k("crit_scale"), 1f, 3f, 0.05f, "x", c -> c.numbers.critScale, (c, v) -> c.numbers.critScale = v));
                r.add(new Rows.Slider(k("distance_scaling"), 0f, 1f, 0.05f, "", c -> c.numbers.distanceScaling, (c, v) -> c.numbers.distanceScaling = v));
                r.add(new Rows.Toggle(k("scale_by_damage"), c -> c.numbers.scaleByDamage, (c, v) -> c.numbers.scaleByDamage = v));
                r.add(new Rows.Header(k("section.colors")));
                r.add(new Rows.Toggle(k("color_by_type"), c -> c.numbers.colorByType, (c, v) -> c.numbers.colorByType = v));
                r.add(new Rows.ColorRow(k("damage_color"), c -> c.numbers.damageColor, (c, v) -> c.numbers.damageColor = v).needs(k("color_by_type"), c -> !c.numbers.colorByType, c -> c.numbers.colorByType = false));
                r.add(new Rows.ColorRow(k("crit_color"), c -> c.numbers.critColor, (c, v) -> c.numbers.critColor = v));
                r.add(new Rows.ColorRow(k("heal_color"), c -> c.numbers.healColor, (c, v) -> c.numbers.healColor = v).needs(k("show_heal"), c -> c.numbers.showHeal, c -> c.numbers.showHeal = true));
                r.add(new Rows.Header(k("section.filter")));
                r.add(new Rows.Toggle(k("show_heal"), c -> c.numbers.showHeal, (c, v) -> c.numbers.showHeal = v));
                r.add(new Rows.Slider(k("min_heal"), 0f, 5f, 0.5f, "", c -> c.numbers.minHeal, (c, v) -> c.numbers.minHeal = v).needs(k("show_heal"), c -> c.numbers.showHeal, c -> c.numbers.showHeal = true));
                r.add(new Rows.Toggle(k("only_mine"), c -> c.numbers.onlyMine, (c, v) -> c.numbers.onlyMine = v));
                r.add(new Rows.Toggle(k("show_self"), c -> c.numbers.showSelf, (c, v) -> c.numbers.showSelf = v));
                r.add(new Rows.Slider(k("max_distance"), 4f, 128f, 1f, "m", c -> c.numbers.maxDistance, (c, v) -> c.numbers.maxDistance = v));
                r.add(new Rows.Slider(k("max_count"), 10f, 300f, 5f, "", c -> (float) c.numbers.maxCount, (c, v) -> c.numbers.maxCount = Math.round(v)));
            }
            case FILTERS -> {
                r.add(new Rows.Header(k("section.entities")));
                r.add(new Rows.Toggle(k("players"), c -> c.filters.players, (c, v) -> c.filters.players = v));
                r.add(new Rows.Toggle(k("hostile"), c -> c.filters.hostile, (c, v) -> c.filters.hostile = v));
                r.add(new Rows.Toggle(k("neutral"), c -> c.filters.neutral, (c, v) -> c.filters.neutral = v));
                r.add(new Rows.Toggle(k("passive"), c -> c.filters.passive, (c, v) -> c.filters.passive = v));
                r.add(new Rows.Toggle(k("bosses"), c -> c.filters.bosses, (c, v) -> c.filters.bosses = v));
                r.add(new Rows.Toggle(k("npcs"), c -> c.filters.npcs, (c, v) -> c.filters.npcs = v));
                r.add(new Rows.Toggle(k("npc_numbers"), c -> c.filters.npcNumbers, (c, v) -> c.filters.npcNumbers = v));
                r.add(new Rows.Toggle(k("armor_stands"), c -> c.filters.armorStands, (c, v) -> c.filters.armorStands = v));
                r.add(new Rows.Toggle(k("self_third_person"), c -> c.general.selfInThirdPerson, (c, v) -> c.general.selfInThirdPerson = v));
            }
            case GENERAL -> {
                r.add(new Rows.Header(k("section.general")));
                r.add(new Rows.Toggle(k("hide_f1"), c -> c.general.hideWithF1, (c, v) -> c.general.hideWithF1 = v));
                r.add(new Rows.Cycle<>(k("occlusion"), Occlusion.values(), c -> c.general.occlusion, (c, v) -> c.general.occlusion = v));
                r.add(new Rows.Slider(k("fade_speed"), 1f, 20f, 0.5f, "", c -> c.general.fadeSpeed, (c, v) -> c.general.fadeSpeed = v));
                r.add(new Rows.Header(k("section.interface")));
                r.add(new Rows.Toggle(k("ui_sounds"), c -> c.general.uiSounds, (c, v) -> c.general.uiSounds = v));
                r.add(new Rows.Toggle(k("update_check"), c -> c.general.updateCheck, (c, v) -> c.general.updateCheck = v));
                r.add(new Rows.Slider(k("ui_volume"), 0f, 1f, 0.05f, "", c -> c.general.uiVolume, (c, v) -> c.general.uiVolume = v));
                r.add(new Rows.Header(k("section.compat")));
                r.add(new Rows.Toggle(k("server_nametags"), c -> c.general.serverNametags, (c, v) -> c.general.serverNametags = v));
                r.add(new Rows.Toggle(k("tab_health"), c -> c.general.tabHealth, (c, v) -> c.general.tabHealth = v));
            }
        }
        return r;
    }

    private void look(List<Rows.Row> r, Function<FhConfig, FhConfig.BarLook> l) {
        r.add(new Rows.Header(k("section.look")));
        r.add(new Rows.Cycle<>(k("style"), BarStyle.values(), c -> l.apply(c).style, (c, v) -> l.apply(c).style = v));
        r.add(new Rows.Slider(k("width"), 10f, 240f, 1f, "px", c -> (float) l.apply(c).width, (c, v) -> l.apply(c).width = Math.round(v)));
        r.add(new Rows.Slider(k("height"), 1f, 16f, 1f, "px", c -> (float) l.apply(c).height, (c, v) -> l.apply(c).height = Math.round(v)));
        r.add(new Rows.Cycle<>(k("many_hearts"), ManyHearts.values(), c -> l.apply(c).manyHearts, (c, v) -> l.apply(c).manyHearts = v).needs(k("style"), c -> l.apply(c).style == BarStyle.HEARTS || l.apply(c).style == BarStyle.GAME_HEARTS, c -> l.apply(c).style = BarStyle.GAME_HEARTS));
        r.add(new Rows.Slider(k("segments"), 2f, 40f, 1f, "", c -> (float) l.apply(c).segments, (c, v) -> l.apply(c).segments = Math.round(v)).needs(k("style"), c -> l.apply(c).style == BarStyle.SEGMENTED, c -> l.apply(c).style = BarStyle.SEGMENTED));
        r.add(new Rows.Header(k("section.text")));
        r.add(new Rows.Toggle(k("show_name"), c -> l.apply(c).showName, (c, v) -> l.apply(c).showName = v));
        r.add(new Rows.Slider(k("name_scale"), 0.5f, 2.5f, 0.05f, "x", c -> l.apply(c).nameScale, (c, v) -> l.apply(c).nameScale = v).needs(k("show_name"), c -> l.apply(c).showName, c -> l.apply(c).showName = true));
        r.add(new Rows.Cycle<>(k("name_font"), FontChoice.values(), c -> l.apply(c).nameFont, (c, v) -> l.apply(c).nameFont = v).needs(k("show_name"), c -> l.apply(c).showName, c -> l.apply(c).showName = true));
        r.add(new Rows.ColorRow(k("name_color"), c -> l.apply(c).nameColor, (c, v) -> l.apply(c).nameColor = v).needs(k("show_name"), c -> l.apply(c).showName, c -> l.apply(c).showName = true));
        r.add(new Rows.Cycle<>(k("name_effect"), TextEffect.values(), c -> l.apply(c).nameEffect, (c, v) -> l.apply(c).nameEffect = v).needs(k("show_name"), c -> l.apply(c).showName, c -> l.apply(c).showName = true));
        r.add(new Rows.Cycle<>(k("value_text"), ValueText.values(), c -> l.apply(c).valueText, (c, v) -> l.apply(c).valueText = v));
        r.add(new Rows.Cycle<>(k("font"), FontChoice.values(), c -> l.apply(c).font, (c, v) -> l.apply(c).font = v));
        r.add(new Rows.Cycle<>(k("text_effect"), TextEffect.values(), c -> l.apply(c).textEffect, (c, v) -> l.apply(c).textEffect = v));
        r.add(new Rows.Toggle(k("show_armor"), c -> l.apply(c).showArmor, (c, v) -> l.apply(c).showArmor = v));
        r.add(new Rows.Toggle(k("show_absorption"), c -> l.apply(c).showAbsorption, (c, v) -> l.apply(c).showAbsorption = v));
        r.add(new Rows.Header(k("section.colors")));
        r.add(new Rows.Cycle<>(k("color_mode"), ColorMode.values(), c -> l.apply(c).colorMode, (c, v) -> l.apply(c).colorMode = v));
        r.add(new Rows.ColorRow(k("fixed_color"), c -> l.apply(c).color, (c, v) -> l.apply(c).color = v).needs(k("color_mode"), c -> l.apply(c).colorMode == ColorMode.FIXED, c -> l.apply(c).colorMode = ColorMode.FIXED));
        r.add(new Rows.ColorRow(k("background"), c -> l.apply(c).background, (c, v) -> l.apply(c).background = v));
        r.add(new Rows.Toggle(k("border"), c -> l.apply(c).border, (c, v) -> l.apply(c).border = v));
        r.add(new Rows.ColorRow(k("border_color"), c -> l.apply(c).borderColor, (c, v) -> l.apply(c).borderColor = v).needs(k("border"), c -> l.apply(c).border, c -> l.apply(c).border = true));
        r.add(new Rows.Toggle(k("low_hp_pulse"), c -> l.apply(c).lowHpPulse, (c, v) -> l.apply(c).lowHpPulse = v));
        r.add(new Rows.Header(k("section.trail")));
        r.add(new Rows.Toggle(k("trail"), c -> l.apply(c).trail, (c, v) -> l.apply(c).trail = v));
        r.add(new Rows.ColorRow(k("trail_color"), c -> l.apply(c).trailColor, (c, v) -> l.apply(c).trailColor = v).needs(k("trail"), c -> l.apply(c).trail, c -> l.apply(c).trail = true));
        r.add(new Rows.Slider(k("trail_hold"), 0f, 3f, 0.05f, "s", c -> l.apply(c).trailHold, (c, v) -> l.apply(c).trailHold = v).needs(k("trail"), c -> l.apply(c).trail, c -> l.apply(c).trail = true));
        r.add(new Rows.Slider(k("trail_speed"), 0.2f, 4f, 0.1f, "x", c -> l.apply(c).trailSpeed, (c, v) -> l.apply(c).trailSpeed = v).needs(k("trail"), c -> l.apply(c).trail, c -> l.apply(c).trail = true));
        r.add(new Rows.Toggle(k("hit_flash"), c -> l.apply(c).hitFlash, (c, v) -> l.apply(c).hitFlash = v));
        r.add(new Rows.ColorRow(k("flash_color"), c -> l.apply(c).flashColor, (c, v) -> l.apply(c).flashColor = v).needs(k("hit_flash"), c -> l.apply(c).hitFlash, c -> l.apply(c).hitFlash = true));
    }
}
