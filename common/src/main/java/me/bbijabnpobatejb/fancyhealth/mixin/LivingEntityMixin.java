package me.bbijabnpobatejb.fancyhealth.mixin;

import me.bbijabnpobatejb.fancyhealth.track.HealthTracker;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.19.4 {
import net.minecraft.world.damagesource.DamageSource;
//?} else {
/*import me.bbijabnpobatejb.fancyhealth.track.DamageKind;
*///?}

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    //? if >=1.19.4 {
    @Inject(method = "handleDamageEvent", at = @At("HEAD"))
    private void fancyhealth$damage(DamageSource source, CallbackInfo ci) {
        HealthTracker.onDamageEvent((LivingEntity) (Object) this, source);
    }
    //?} else {
    /*@Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void fancyhealth$damage(byte id, CallbackInfo ci) {
        DamageKind kind = DamageKind.ofEvent(id);
        if (kind != null) HealthTracker.onDamageKind((LivingEntity) (Object) this, kind);
    }
    *///?}
}
