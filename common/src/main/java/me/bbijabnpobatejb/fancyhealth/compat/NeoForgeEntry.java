//? if neoforge
package me.bbijabnpobatejb.fancyhealth.compat;

import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.nio.file.Path;

@Mod(value = "fancyhealth", dist = Dist.CLIENT)
public class NeoForgeEntry extends BasePlatform {

    public NeoForgeEntry(IEventBus bus, ModContainer container) {
        FancyHealth.init(this);
        container.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> new FhScreen(parent));
    }

    @Override
    public String modVersion() {
        return ModList.get().getModContainerById("fancyhealth").map(c -> c.getModInfo().getVersion().toString()).orElse("0");
    }

    @Override
    public String minecraftVersion() {
        return ModList.get().getModContainerById("minecraft").map(c -> c.getModInfo().getVersion().toString()).orElse("");
    }

    @Override
    public String loader() {
        return "neoforge";
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
