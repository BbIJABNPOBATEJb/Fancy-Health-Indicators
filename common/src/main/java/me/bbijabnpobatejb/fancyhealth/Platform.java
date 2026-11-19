package me.bbijabnpobatejb.fancyhealth;

import com.mojang.math.Transformation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface Platform {

    Screen screen();

    void setScreen(Screen screen);

    Screen configScreen(Screen parent);

    boolean hudHidden();

    Vec3 cameraPos();

    void overlayMessage(Component message);

    List<EntityType<? extends LivingEntity>> previewTypes();

    float[] displayOffset(Transformation transformation);

    void openUrl(String url);

    String modVersion();

    String minecraftVersion();

    String loader();

    int heartType(net.minecraft.world.entity.LivingEntity entity);
}
