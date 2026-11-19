package me.bbijabnpobatejb.fancyhealth;

import me.bbijabnpobatejb.fancyhealth.compat.Mc;
import com.mojang.blaze3d.platform.InputConstants;
import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.config.ConfigStore;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.numbers.DamageNumbers;
import me.bbijabnpobatejb.fancyhealth.track.HealthTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@UtilityClass
public class FancyHealth {

    public final String ID = "fancyhealth";

    private Platform platform;
    private KeyMapping[] keys;
    private KeyMapping openKey;
    private KeyMapping toggleKey;

    public void init(Platform p) {
        platform = p;
        ConfigStore.load();
    }

    public Platform platform() {
        return platform;
    }

    public FhConfig config() {
        return ConfigStore.get();
    }

    public String id(String path) {
        return ID + ":" + path;
    }

    public KeyMapping[] keyMappings() {
        if (keys == null) {
            //? if >=1.21.9 {
            KeyMapping.Category category = KeyMapping.Category.register(me.bbijabnpobatejb.fancyhealth.compat.Mc.id(id("main")));
            //?} else {
            /*String category = "key.category.fancyhealth.main";
            *///?}
            openKey = new KeyMapping("key.fancyhealth.config", me.bbijabnpobatejb.fancyhealth.compat.Keys.H, category);
            toggleKey = new KeyMapping("key.fancyhealth.toggle", InputConstants.UNKNOWN.getValue(), category);
            keys = new KeyMapping[]{openKey, toggleKey};
        }
        return keys;
    }

    private Object lastLevel;

    public void tick(Minecraft mc) {
        if (keys != null) {
            while (openKey.consumeClick()) {
                if (platform.screen() == null) platform.setScreen(platform.configScreen(null));
            }
            while (toggleKey.consumeClick()) {
                FhConfig c = config();
                c.general.enabled = !c.general.enabled;
                ConfigStore.save();
                if (mc.player != null) {
                    platform.overlayMessage(Mc.translatable(c.general.enabled ? "fancyhealth.toggle.on" : "fancyhealth.toggle.off"));
                }
            }
        }
        if (mc.level != lastLevel) {
            lastLevel = mc.level;
            me.bbijabnpobatejb.fancyhealth.api.FancyHealthApi.clear(me.bbijabnpobatejb.fancyhealth.api.FancyHealthApi.SERVER);
            if (mc.level != null && mc.getCurrentServer() != null && mc.getCurrentServer().motd != null) {
                me.bbijabnpobatejb.fancyhealth.api.FancyHealthApi.readCodes(mc.getCurrentServer().motd.getString());
            }
        }
        HealthTracker.tick(mc);
        DamageNumbers.tick();
    }
}
