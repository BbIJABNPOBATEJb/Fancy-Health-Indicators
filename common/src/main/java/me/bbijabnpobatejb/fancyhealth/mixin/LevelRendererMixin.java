//? if <26.1
package me.bbijabnpobatejb.fancyhealth.mixin;

import me.bbijabnpobatejb.fancyhealth.render.Projection;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.21.6 {
import com.mojang.blaze3d.buffers.GpuBufferSlice;
//?}
//? if >=1.21.2 {
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
//?}
//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?}
//? if >=1.19.3 {
import org.joml.Matrix4f;
import org.joml.Vector4f;
//?} else {
/*import com.mojang.math.Matrix4f;
*///?}
//? if <1.21.6 {
/*import net.minecraft.client.renderer.GameRenderer;
*///?}
//? if <1.21.4 {
/*import net.minecraft.client.renderer.LightTexture;
*///?}
//? if <1.20.5 {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    //? if >=1.21.9 {
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void fancyhealth$matrices(GraphicsResourceAllocator allocator, DeltaTracker delta, boolean outline, Camera camera,
                                      Matrix4f view, Matrix4f projection, Matrix4f culling, GpuBufferSlice fog, Vector4f fogColor,
                                      boolean sky, CallbackInfo ci) {
        Projection.update(projection.get(new float[16]), view.get(new float[16]), camera.position());
    }
    //?} else if >=1.21.6 {
    /*@Inject(method = "renderLevel", at = @At("HEAD"))
    private void fancyhealth$matrices(GraphicsResourceAllocator allocator, DeltaTracker delta, boolean outline, Camera camera,
                                      Matrix4f view, Matrix4f projection, GpuBufferSlice fog, Vector4f fogColor, boolean sky, CallbackInfo ci) {
        Projection.update(projection.get(new float[16]), view.get(new float[16]), camera.getPosition());
    }
    *///?} else if >=1.21.4 {
    /*@Inject(method = "renderLevel", at = @At("HEAD"))
    private void fancyhealth$matrices(GraphicsResourceAllocator allocator, DeltaTracker delta, boolean outline, Camera camera,
                                      GameRenderer renderer, Matrix4f view, Matrix4f projection, CallbackInfo ci) {
        Projection.update(projection.get(new float[16]), view.get(new float[16]), camera.getPosition());
    }
    *///?} else if >=1.21.2 {
    /*@Inject(method = "renderLevel", at = @At("HEAD"))
    private void fancyhealth$matrices(GraphicsResourceAllocator allocator, DeltaTracker delta, boolean outline, Camera camera,
                                      GameRenderer renderer, LightTexture light, Matrix4f view, Matrix4f projection, CallbackInfo ci) {
        Projection.update(projection.get(new float[16]), view.get(new float[16]), camera.getPosition());
    }
    *///?} else if >=1.21 {
    /*@Inject(method = "renderLevel", at = @At("HEAD"))
    private void fancyhealth$matrices(DeltaTracker delta, boolean outline, Camera camera, GameRenderer renderer, LightTexture light,
                                      Matrix4f view, Matrix4f projection, CallbackInfo ci) {
        Projection.update(projection.get(new float[16]), view.get(new float[16]), camera.getPosition());
    }
    *///?} else if >=1.20.5 {
    /*@Inject(method = "renderLevel", at = @At("HEAD"))
    private void fancyhealth$matrices(float partialTick, long nanos, boolean outline, Camera camera, GameRenderer renderer, LightTexture light,
                                      Matrix4f view, Matrix4f projection, CallbackInfo ci) {
        Projection.update(projection.get(new float[16]), view.get(new float[16]), camera.getPosition());
    }
    *///?} else if >=1.19.3 {
    /*@Inject(method = "renderLevel", at = @At("HEAD"))
    private void fancyhealth$matrices(PoseStack pose, float partialTick, long nanos, boolean outline, Camera camera, GameRenderer renderer,
                                      LightTexture light, Matrix4f projection, CallbackInfo ci) {
        Projection.update(projection.get(new float[16]), pose.last().pose().get(new float[16]), camera.getPosition());
    }
    *///?} else {
    /*@Inject(method = "renderLevel", at = @At("HEAD"))
    private void fancyhealth$matrices(PoseStack pose, float partialTick, long nanos, boolean outline, Camera camera, GameRenderer renderer,
                                      LightTexture light, Matrix4f projection, CallbackInfo ci) {
        Projection.update(fancyhealth$array(projection), fancyhealth$array(pose.last().pose()), camera.getPosition());
    }

    private static float[] fancyhealth$array(Matrix4f matrix) {
        java.nio.FloatBuffer buffer = java.nio.FloatBuffer.allocate(16);
        matrix.store(buffer);
        return buffer.array();
    }
    *///?}
}
