package me.bbijabnpobatejb.fancyhealth.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import me.bbijabnpobatejb.fancyhealth.render.Projection;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void fancyhealth$matrices(GraphicsResourceAllocator allocator, DeltaTracker delta, boolean outline, Camera camera,
                                      Matrix4f view, Matrix4f projection, Matrix4f culling, GpuBufferSlice fog, Vector4f fogColor,
                                      boolean sky, CallbackInfo ci) {
        Projection.update(projection, view, camera.position());
    }
}
