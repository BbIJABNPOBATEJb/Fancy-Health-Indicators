//? if <26.1 && !forge
package me.bbijabnpobatejb.fancyhealth.mixin;

import me.bbijabnpobatejb.fancyhealth.hud.HudRenderer;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.21.6 {
import me.bbijabnpobatejb.fancyhealth.compat.GuiCanvas;
//?} else if >=1.20 {
/*import me.bbijabnpobatejb.fancyhealth.compat.PoseCanvas;
*///?} else {
/*import me.bbijabnpobatejb.fancyhealth.compat.LegacyCanvas;
import com.mojang.blaze3d.vertex.PoseStack;
*///?}
//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?}
//? if >=1.20 {
import net.minecraft.client.gui.GuiGraphics;
//?}

@Mixin(Gui.class)
public abstract class GuiMixin {

    //? if >=1.21.6 {
    @Inject(method = "render", at = @At("HEAD"))
    private void fancyhealth$world(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        HudRenderer.renderWorld(new GuiCanvas(g), delta.getGameTimeDeltaPartialTick(true));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void fancyhealth$overlay(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        HudRenderer.renderOverlay(new GuiCanvas(g), delta.getGameTimeDeltaPartialTick(true));
    }
    //?} else if >=1.21 {
    /*@Inject(method = "render", at = @At("HEAD"))
    private void fancyhealth$world(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        HudRenderer.renderWorld(new PoseCanvas(g), delta.getGameTimeDeltaPartialTick(true));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void fancyhealth$overlay(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        HudRenderer.renderOverlay(new PoseCanvas(g), delta.getGameTimeDeltaPartialTick(true));
    }
    *///?} else if >=1.20 {
    /*@Inject(method = "render", at = @At("HEAD"))
    private void fancyhealth$world(GuiGraphics g, float partialTick, CallbackInfo ci) {
        HudRenderer.renderWorld(new PoseCanvas(g), partialTick);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void fancyhealth$overlay(GuiGraphics g, float partialTick, CallbackInfo ci) {
        HudRenderer.renderOverlay(new PoseCanvas(g), partialTick);
    }
    *///?} else {
    /*@Inject(method = "render", at = @At("HEAD"))
    private void fancyhealth$world(PoseStack pose, float partialTick, CallbackInfo ci) {
        HudRenderer.renderWorld(new LegacyCanvas(pose), partialTick);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void fancyhealth$overlay(PoseStack pose, float partialTick, CallbackInfo ci) {
        HudRenderer.renderOverlay(new LegacyCanvas(pose), partialTick);
    }
    *///?}
}
