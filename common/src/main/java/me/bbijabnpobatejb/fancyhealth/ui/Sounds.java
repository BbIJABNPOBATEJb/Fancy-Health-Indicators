package me.bbijabnpobatejb.fancyhealth.ui;

import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Sounds {

    private final Map<String, SoundEvent> EVENTS = new HashMap<>();
    private final Map<String, Long> LAST = new HashMap<>();

    public void play(String name) {
        play(name, 1f);
    }

    public void play(String name, float pitch) {
        FhConfig c = FancyHealth.config();
        if (!c.general.uiSounds || c.general.uiVolume <= 0.01f) return;
        long now = System.currentTimeMillis();
        long gap = switch (name) {
            case "hover" -> 45;
            case "tick" -> 35;
            default -> 0;
        };
        Long last = LAST.get(name);
        if (gap > 0 && last != null && now - last < gap) return;
        LAST.put(name, now);
        SoundEvent ev = EVENTS.computeIfAbsent(name, n -> me.bbijabnpobatejb.fancyhealth.compat.Mc.sound(FancyHealth.id("ui." + n)));
        float vol = c.general.uiVolume * (name.equals("hover") || name.equals("tick") ? 0.55f : 1f);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ev, pitch, vol));
    }
}
