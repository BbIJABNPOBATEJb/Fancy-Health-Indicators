package me.bbijabnpobatejb.fancyhealth.ui;

import me.bbijabnpobatejb.fancyhealth.compat.Mc;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;

@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class FhScreenBase extends Screen {

    protected final Screen parent;
    protected final ConfigUi ui;
    boolean opened;

    protected FhScreenBase(Screen parent) {
        super(Mc.translatable("fancyhealth.title"));
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

    //? if >=1.21.9 {
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean r = ui.mouseDown(event.x(), event.y(), event.button(), event.hasShiftDown());
        ui.syncInput();
        return r;
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
    public boolean keyPressed(KeyEvent event) {
        boolean r = ui.keyPressed(event.key(), event.hasControlDown(), event.hasShiftDown());
        ui.syncInput();
        return r || super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        ui.keyReleased(event.key());
        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        boolean r = ui.charTyped(event.codepoint());
        ui.syncInput();
        return r || super.charTyped(event);
    }
    //?} else {
    /*@Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean r = ui.mouseDown(x, y, button, hasShiftDown());
        ui.syncInput();
        return r;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return ui.mouseUp(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return ui.mouseDrag(x, y);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        boolean r = ui.keyPressed(key, hasControlDown(), hasShiftDown());
        ui.syncInput();
        return r || super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scancode, int modifiers) {
        ui.keyReleased(key);
        return super.keyReleased(key, scancode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        boolean r = ui.charTyped(c);
        ui.syncInput();
        return r || super.charTyped(c, modifiers);
    }
    *///?}

    //? if >=1.20.2 {
    @Override
    public boolean mouseScrolled(double x, double y, double h, double v) {
        return ui.scroll(x, y, v);
    }
    //?} else {
    /*@Override
    public boolean mouseScrolled(double x, double y, double v) {
        return ui.scroll(x, y, v);
    }
    *///?}

    @Override
    public void removed() {
        ui.releaseInput();
        super.removed();
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
