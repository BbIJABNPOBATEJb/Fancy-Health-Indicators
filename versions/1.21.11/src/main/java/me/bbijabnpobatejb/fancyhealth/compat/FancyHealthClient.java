package me.bbijabnpobatejb.fancyhealth.compat;

import com.mojang.math.Transformation;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import me.bbijabnpobatejb.fancyhealth.Platform;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
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
        return Minecraft.getInstance().screen;
    }

    @Override
    public void setScreen(Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    public Screen configScreen(Screen parent) {
        return new FhScreen(parent);
    }

    @Override
    public boolean hudHidden() {
        return Minecraft.getInstance().options.hideGui;
    }

    @Override
    public Vec3 cameraPos() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().position();
    }

    @Override
    public void overlayMessage(Component message) {
        if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.displayClientMessage(message, true);
    }

    @Override
    public List<EntityType<? extends LivingEntity>> previewTypes() {
        return List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.PIG, EntityType.IRON_GOLEM, EntityType.ENDERMAN);
    }

    @Override
    public float[] displayOffset(Transformation t) {
        return new float[]{t.getTranslation().y(), t.getScale().y()};
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
        net.minecraft.util.Util.getPlatform().openUri(url);
    }
}
