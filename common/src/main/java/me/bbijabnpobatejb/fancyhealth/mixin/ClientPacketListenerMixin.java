package me.bbijabnpobatejb.fancyhealth.mixin;

import me.bbijabnpobatejb.fancyhealth.track.HealthTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleAnimate", at = @At("TAIL"))
    private void fancyhealth$crit(ClientboundAnimatePacket packet, CallbackInfo ci) {
        int action = packet.getAction();
        if (action != 4 && action != 5) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity e = mc.level.getEntity(packet.getId());
        if (e != null && action == 4) HealthTracker.onCrit(e);
    }
}
