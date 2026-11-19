package me.bbijabnpobatejb.fancyhealth.ui;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class FhScreenBase extends Screen {

    protected final Screen parent;
    protected final ConfigUi ui;
    boolean opened;

    protected FhScreenBase(Screen parent) {
        super(Component.translatable("fancyhealth.title"));
        this.parent = parent;
        this.ui = new ConfigUi(this::onClose);
    }

    public ConfigUi ui() {
        return ui;
    }

    @Override
    protected void init() {
        ui.resize(width, height);
        if (!opened) {
            opened = true;
            ui.open();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return ui.mouseDown(event.x(), event.y(), event.button(), event.hasShiftDown());
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return ui.mouseUp(event.x(), event.y(), event.button());
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return ui.mouseDrag(event.x(), event.y());
    }

    @Override
    public boolean mouseScrolled(double x, double y, double h, double v) {
        return ui.scroll(x, y, v);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (ui.keyPressed(event.key(), event.hasControlDown())) return true;
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        ui.keyReleased(event.key());
        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return ui.charTyped(event.codepoint()) || super.charTyped(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        ui.close();
        FancyHealth.platform().setScreen(parent);
    }
}
