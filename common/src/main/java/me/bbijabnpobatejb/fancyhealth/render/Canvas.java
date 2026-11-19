package me.bbijabnpobatejb.fancyhealth.render;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public interface Canvas {

    int width();

    int height();

    void push();

    void pop();

    void translate(float x, float y);

    void scale(float x, float y);

    void rotate(float radians);

    void shear(float x);

    void fill(int x1, int y1, int x2, int y2, int argb);

    void gradient(int x1, int y1, int x2, int y2, int top, int bottom);

    void text(Font font, Component text, int x, int y, int argb, boolean shadow);

    void sprite(Identifier texture, int x, int y, int w, int h, int u, int v, int texW, int texH, int argb);

    void entity(LivingEntity entity, int x1, int y1, int x2, int y2, int size, float yOffset, float mouseX, float mouseY);

    void scissor(int x1, int y1, int x2, int y2);

    void endScissor();

    void identity();

    void guiSprite(String id, int x, int y, int w, int h, int argb);

    default void scale(float s) {
        scale(s, s);
    }
}
