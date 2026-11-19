package me.bbijabnpobatejb.fancyhealth.compat;

import me.bbijabnpobatejb.fancyhealth.ui.FhScreenBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class FhScreen extends FhScreenBase {

    public FhScreen(Screen parent) {
        super(parent);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ui.render(new GuiCanvas(g), mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (minecraft.level == null) g.fillGradient(0, 0, width, height, 0xFF101218, 0xFF07080B);
    }
}
