package me.bbijabnpobatejb.fancyhealth.compat;

import com.mojang.math.Transformation;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import me.bbijabnpobatejb.fancyhealth.Platform;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FancyHealthClient implements ClientModInitializer, Platform {

    @Override
    public void onInitializeClient() {
        FancyHealth.init(this);
    }

    @Override
    public Screen screen() {
        return Minecraft.getInstance().gui.screen();
    }

    @Override
    public void setScreen(Screen screen) {
        Minecraft.getInstance().gui.setScreen(screen);
    }

    @Override
    public Screen configScreen(Screen parent) {
        return new FhScreen(parent);
    }

    @Override
    public boolean hudHidden() {
        return Minecraft.getInstance().gui.hud.isHidden();
    }

    @Override
    public Vec3 cameraPos() {
        return Minecraft.getInstance().gameRenderer.mainCamera().position();
    }

    @Override
    public void overlayMessage(Component message) {
        if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.sendOverlayMessage(message);
    }

    @Override
    public List<EntityType<? extends LivingEntity>> previewTypes() {
        return List.of(EntityTypes.ZOMBIE, EntityTypes.SKELETON, EntityTypes.CREEPER, EntityTypes.PIG, EntityTypes.IRON_GOLEM, EntityTypes.ENDERMAN);
    }

    @Override
    public float[] displayOffset(Transformation t) {
        return new float[]{t.translation().y(), t.scale().y()};
    }

    @Override
    public int heartType(LivingEntity entity) {
        if (entity.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) return 1;
        if (entity.hasEffect(net.minecraft.world.effect.MobEffects.WITHER)) return 2;
        return entity.isFullyFrozen() ? 3 : 0;
    }

    @Override
    public String modVersion() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("fancyhealth").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("0");
    }

    @Override
    public String minecraftVersion() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("minecraft").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
    }

    @Override
    public String loader() {
        return "fabric";
    }

    @Override
    public void openUrl(String url) {
        try {
            Class.forName("com.mojang.blaze3d.Blaze3D").getMethod("openUri", java.net.URI.class).invoke(null, java.net.URI.create(url));
        } catch (ReflectiveOperationException e) {
            try {
                Object os = Class.forName("net.minecraft.util.Util").getMethod("getPlatform").invoke(null);
                os.getClass().getMethod("openUri", String.class).invoke(os, url);
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }
}
