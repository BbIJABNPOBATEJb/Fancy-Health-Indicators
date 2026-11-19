//? if >=26.1
package me.bbijabnpobatejb.fancyhealth.mixin;

import me.bbijabnpobatejb.fancyhealth.compat.GuiCanvas;
import me.bbijabnpobatejb.fancyhealth.hud.HudRenderer;
import me.bbijabnpobatejb.fancyhealth.render.Projection;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public abstract class HudMixin {

    @Unique
    private final Matrix4f fancyhealth$matrix = new Matrix4f();

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void fancyhealth$world(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
        //? if >=26.2 {
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        //?} else {
        /*Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        *///?}
        if (camera.isInitialized()) {
            Projection.update(camera.getViewRotationProjectionMatrix(fancyhealth$matrix).get(new float[16]), camera.position(),
                    1f / (float) Math.tan(Math.toRadians(camera.getFov()) / 2.0));
        }
        HudRenderer.renderWorld(new GuiCanvas(g), delta.getGameTimeDeltaPartialTick(true));
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void fancyhealth$overlay(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
        HudRenderer.renderOverlay(new GuiCanvas(g), delta.getGameTimeDeltaPartialTick(true));
    }
}
