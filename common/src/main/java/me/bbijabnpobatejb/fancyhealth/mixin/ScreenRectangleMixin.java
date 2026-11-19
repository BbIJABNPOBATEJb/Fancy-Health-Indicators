package me.bbijabnpobatejb.fancyhealth.mixin;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenRectangle.class)
public abstract class ScreenRectangleMixin {

    @Inject(method = "transformMaxBounds", at = @At("HEAD"), cancellable = true)
    private void fancyhealth$fullBounds(Matrix3x2fc m, CallbackInfoReturnable<ScreenRectangle> cir) {
        ScreenRectangle self = (ScreenRectangle) (Object) this;
        Vector2f a = m.transformPosition(self.left(), self.top(), new Vector2f());
        Vector2f b = m.transformPosition(self.right(), self.top(), new Vector2f());
        Vector2f c = m.transformPosition(self.left(), self.bottom(), new Vector2f());
        Vector2f d = m.transformPosition(self.right(), self.bottom(), new Vector2f());
        float minX = Math.min(Math.min(a.x, b.x), Math.min(c.x, d.x)), maxX = Math.max(Math.max(a.x, b.x), Math.max(c.x, d.x));
        float minY = Math.min(Math.min(a.y, b.y), Math.min(c.y, d.y)), maxY = Math.max(Math.max(a.y, b.y), Math.max(c.y, d.y));
        int x0 = Mth.floor(minX), y0 = Mth.floor(minY);
        cir.setReturnValue(new ScreenRectangle(x0, y0, Mth.ceil(maxX) - x0, Mth.ceil(maxY) - y0));
    }
}
