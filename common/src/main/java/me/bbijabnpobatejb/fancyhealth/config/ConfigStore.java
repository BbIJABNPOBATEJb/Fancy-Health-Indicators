package me.bbijabnpobatejb.fancyhealth.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class ConfigStore {

    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private FhConfig current = Presets.create("classic");

    public FhConfig get() {
        return current;
    }

    public void set(FhConfig config) {
        current = config;
    }

    public Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("fancyhealth.json");
    }

    public void load() {
        Path file = path();
        if (!Files.exists(file)) {
            save();
            return;
        }
        try {
            FhConfig read = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), FhConfig.class);
            if (read != null) current = fill(read);
        } catch (Exception e) {
            System.err.println("[FancyHealth] Broken config, using defaults: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(current), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[FancyHealth] Could not save config: " + e.getMessage());
        }
    }

    public FhConfig copy(FhConfig config) {
        return fill(GSON.fromJson(GSON.toJson(config), FhConfig.class));
    }

    public String toJson(Object o) {
        return GSON.toJson(o);
    }

    public FhConfig fromJson(String json) {
        return fill(GSON.fromJson(json, FhConfig.class));
    }

    private FhConfig fill(FhConfig c) {
        FhConfig d = new FhConfig();
        if (c.general == null) c.general = d.general;
        if (c.filters == null) c.filters = d.filters;
        if (c.filters.blacklist == null) c.filters.blacklist = d.filters.blacklist;
        if (c.overhead == null) c.overhead = d.overhead;
        if (c.overhead.look == null) c.overhead.look = d.overhead.look;
        if (c.crosshair == null) c.crosshair = d.crosshair;
        if (c.crosshair.look == null) c.crosshair.look = d.crosshair.look;
        if (c.target == null) c.target = d.target;
        if (c.target.look == null) c.target.look = d.target.look;
        if (c.numbers == null) c.numbers = d.numbers;
        fixLook(c.overhead.look, d.overhead.look);
        fixLook(c.crosshair.look, d.crosshair.look);
        fixLook(c.target.look, d.target.look);
        FhConfig.Numbers n = c.numbers;
        if (n.animation == null) n.animation = d.numbers.animation;
        if (n.critAnimation == null) n.critAnimation = d.numbers.critAnimation;
        if (n.font == null) n.font = d.numbers.font;
        if (n.effect == null) n.effect = d.numbers.effect;
        if (n.format == null) n.format = d.numbers.format;
        if (n.critMark == null) n.critMark = d.numbers.critMark;
        if (c.overhead.anchor == null) c.overhead.anchor = d.overhead.anchor;
        if (c.overhead.sizeMode == null) c.overhead.sizeMode = d.overhead.sizeMode;
        if (c.overhead.showWhen == null) c.overhead.showWhen = d.overhead.showWhen;
        if (c.general.occlusion == null) c.general.occlusion = d.general.occlusion;
        if (c.target.style == null) c.target.style = d.target.style;
        if (c.target.position == null) c.target.position = d.target.position;
        if (c.target.source == null) c.target.source = d.target.source;
        if (c.preset == null) c.preset = "custom";
        if (c.base == null || !Presets.ids().contains(c.base)) c.base = Presets.ids().contains(c.preset) ? c.preset : "classic";
        return c;
    }

    private void fixLook(FhConfig.BarLook l, FhConfig.BarLook d) {
        if (l.style == null) l.style = d.style;
        if (l.colorMode == null) l.colorMode = d.colorMode;
        if (l.valueText == null) l.valueText = d.valueText;
        if (l.font == null) l.font = d.font;
        if (l.textEffect == null) l.textEffect = d.textEffect;
    }
}
