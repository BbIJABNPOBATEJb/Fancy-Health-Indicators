//? if forge
package me.bbijabnpobatejb.fancyhealth.compat;

import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import me.bbijabnpobatejb.fancyhealth.hud.HudRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

@Mod("fancyhealth")
public class ForgeEntry extends BasePlatform {

    public ForgeEntry() {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        FancyHealth.init(this);
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new FhScreen(parent)));
        MinecraftForge.EVENT_BUS.addListener((RenderGuiEvent.Pre e) -> HudRenderer.renderWorld(new PoseCanvas(e.getGuiGraphics()), e.getPartialTick()));
        MinecraftForge.EVENT_BUS.addListener((RenderGuiEvent.Post e) -> HudRenderer.renderOverlay(new PoseCanvas(e.getGuiGraphics()), e.getPartialTick()));
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
        return "forge";
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
