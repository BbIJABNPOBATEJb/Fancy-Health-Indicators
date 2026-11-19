package me.bbijabnpobatejb.fancyhealth.mixin;

import me.bbijabnpobatejb.fancyhealth.compat.GuiCanvas;
import me.bbijabnpobatejb.fancyhealth.hud.HudRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void fancyhealth$world(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        HudRenderer.renderWorld(new GuiCanvas(g), delta.getGameTimeDeltaPartialTick(true));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void fancyhealth$overlay(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        HudRenderer.renderOverlay(new GuiCanvas(g), delta.getGameTimeDeltaPartialTick(true));
    }
}
