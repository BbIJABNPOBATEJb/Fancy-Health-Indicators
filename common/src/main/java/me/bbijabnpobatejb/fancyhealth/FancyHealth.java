package me.bbijabnpobatejb.fancyhealth;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.config.ConfigStore;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.numbers.DamageNumbers;
import me.bbijabnpobatejb.fancyhealth.track.HealthTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

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

    public Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    public KeyMapping[] keyMappings() {
        if (keys == null) {
            KeyMapping.Category category = KeyMapping.Category.register(id("main"));
            openKey = new KeyMapping("key.fancyhealth.config", InputConstants.KEY_H, category);
            toggleKey = new KeyMapping("key.fancyhealth.toggle", InputConstants.UNKNOWN.getValue(), category);
            keys = new KeyMapping[]{openKey, toggleKey};
        }
        return keys;
    }

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
                    platform.overlayMessage(Component.translatable(c.general.enabled ? "fancyhealth.toggle.on" : "fancyhealth.toggle.off"));
                }
            }
        }
        HealthTracker.tick(mc);
        DamageNumbers.tick();
    }
}
