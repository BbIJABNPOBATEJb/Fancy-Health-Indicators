//? if fabric
package me.bbijabnpobatejb.fancyhealth.compat;

import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FancyHealthClient extends BasePlatform implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FancyHealth.init(this);
    }

    @Override
    public String modVersion() {
        return FabricLoader.getInstance().getModContainer("fancyhealth").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("0");
    }

    @Override
    public String minecraftVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
    }

    @Override
    public String loader() {
        return FabricLoader.getInstance().isModLoaded("quilt_loader") ? "quilt" : "fabric";
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
