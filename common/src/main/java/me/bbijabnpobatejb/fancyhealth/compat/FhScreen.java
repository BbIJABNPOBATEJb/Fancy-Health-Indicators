package me.bbijabnpobatejb.fancyhealth.compat;

import me.bbijabnpobatejb.fancyhealth.ui.FhScreenBase;
import net.minecraft.client.gui.screens.Screen;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else if >=1.20 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

public class FhScreen extends FhScreenBase {

    public FhScreen(Screen parent) {
        super(parent);
    }

    //? if >=26.1 {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        ui.render(new GuiCanvas(g), mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        if (minecraft.level == null) g.fillGradient(0, 0, width, height, 0xFF101218, 0xFF07080B);
    }
    *///?} else if >=1.21.6 {
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ui.render(new GuiCanvas(g), mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (minecraft.level == null) g.fillGradient(0, 0, width, height, 0xFF101218, 0xFF07080B);
    }
    //?} else if >=1.20.2 {
    /*@Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ui.render(new PoseCanvas(g), mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (minecraft.level == null) g.fillGradient(0, 0, width, height, 0xFF101218, 0xFF07080B);
    }
    *///?} else if >=1.20 {
    /*@Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (minecraft.level == null) g.fillGradient(0, 0, width, height, 0xFF101218, 0xFF07080B);
        ui.render(new PoseCanvas(g), mouseX, mouseY);
    }
    *///?} else {
    /*@Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        if (minecraft.level == null) fillGradient(pose, 0, 0, width, height, 0xFF101218, 0xFF07080B);
        ui.render(new LegacyCanvas(pose), mouseX, mouseY);
    }
    *///?}
}
