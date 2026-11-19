package me.bbijabnpobatejb.fancyhealth.mixin;

import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {

    @Shadow
    @Final
    public KeyMapping[] keyMappings;

    @Inject(method = "load", at = @At("HEAD"))
    private void fancyhealth$keys(CallbackInfo ci) {
        KeyMapping[] own = FancyHealth.keyMappings();
        if (ArrayUtils.contains(keyMappings, own[0])) return;
        ((OptionsAccessor) this).fancyhealth$setKeyMappings(ArrayUtils.addAll(keyMappings, own));
    }
}
