package me.bbijabnpobatejb.fancyhealth.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;

public class ModMenuEntry implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> FancyHealth.platform().configScreen(parent);
    }
}
