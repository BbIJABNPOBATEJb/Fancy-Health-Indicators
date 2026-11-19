//? if >=1.20 && <1.21.6
package me.bbijabnpobatejb.fancyhealth.compat;

import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

public record PoseCanvas(GuiGraphics g) implements Canvas {

    @Override
    public int width() {
        return g.guiWidth();
    }

    @Override
    public int height() {
        return g.guiHeight();
    }

    @Override
    public void push() {
        g.pose().pushPose();
    }

    @Override
    public void pop() {
        g.pose().popPose();
    }

    @Override
    public void translate(float x, float y) {
        g.pose().translate(x, y, 0);
    }

    @Override
    public void scale(float x, float y) {
        g.pose().scale(x, y, 1);
    }

    @Override
    public void identity() {
        g.pose().setIdentity();
    }

    @Override
    public void rotate(float radians) {
        g.pose().mulPose(com.mojang.math.Axis.ZP.rotation(radians));
    }

    @Override
    public void shear(float x) {
        g.pose().last().pose().mul(new Matrix4f(1, 0, 0, 0, x, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1));
    }

    @Override
    public void fill(int x1, int y1, int x2, int y2, int argb) {
        if (x2 <= x1 || y2 <= y1 || (argb >>> 24) == 0) return;
        boolean pushed = pixelSnap();
        g.fill(x1, y1, x2, y2, argb);
        if (pushed) g.pose().popPose();
    }

    @Override
    public void gradient(int x1, int y1, int x2, int y2, int top, int bottom) {
        if (x2 > x1 && y2 > y1) g.fillGradient(x1, y1, x2, y2, top, bottom);
    }

    @Override
    public void text(Font font, Component text, int x, int y, int argb, boolean shadow) {
        Matrix4f m = g.pose().last().pose();
        float ps = (float) Math.sqrt(Math.abs(m.m00() * m.m11() - m.m01() * m.m10()));
        double gs = me.bbijabnpobatejb.fancyhealth.render.Crisp.guiScale();
        float ox = m.m00() * x + m.m10() * y + m.m30(), oy = m.m01() * x + m.m11() * y + m.m31();
        float dx = (float) ((Math.round(ox * gs) - ox * gs) / gs), dy = (float) ((Math.round(oy * gs) - oy * gs) / gs);
        g.pose().pushPose();
        g.pose().last().pose().translateLocal(dx, dy, 0);
        g.drawString(font, me.bbijabnpobatejb.fancyhealth.render.Crisp.sequence(text, ps), x, y, argb, shadow);
        g.pose().popPose();
    }

    @Override
    public void sprite(String texture, int x, int y, int w, int h, int u, int v, int texW, int texH, int argb) {
        boolean pushed = pixelSnap();
        //? if >=1.21.2 {
        g.blit(net.minecraft.client.renderer.RenderType::guiTextured, Mc.id(texture), x, y, u, v, w, h, texW, texH, argb);
        //?} else {
        /*com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        g.setColor((argb >> 16 & 255) / 255f, (argb >> 8 & 255) / 255f, (argb & 255) / 255f, (argb >>> 24) / 255f);
        g.blit(Mc.id(texture), x, y, (float) u, (float) v, w, h, texW, texH);
        g.setColor(1, 1, 1, 1);
        *///?}
        if (pushed) g.pose().popPose();
    }

    @Override
    public void guiSprite(String id, int x, int y, int w, int h, int argb) {
        //? if >=1.21.2 {
        boolean pushed = pixelSnap();
        g.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, Mc.id(id), x, y, w, h, argb);
        if (pushed) g.pose().popPose();
        //?} else if >=1.20.2 {
        /*boolean pushed = pixelSnap();
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        g.setColor((argb >> 16 & 255) / 255f, (argb >> 8 & 255) / 255f, (argb & 255) / 255f, (argb >>> 24) / 255f);
        g.blitSprite(Mc.id(id), x, y, w, h);
        g.setColor(1, 1, 1, 1);
        if (pushed) g.pose().popPose();
        *///?} else {
        /*int[] uv = Mc.legacyHeart(id);
        if (uv != null) sprite("minecraft:textures/gui/icons.png", x, y, w, h, uv[0], uv[1], 256, 256, argb);
        *///?}
    }

    @Override
    public void entity(LivingEntity entity, int x1, int y1, int x2, int y2, int size, float yOffset, float mouseX, float mouseY) {
        Matrix4f m = g.pose().last().pose();
        float ax = m.m00() * x1 + m.m10() * y1 + m.m30(), ay = m.m01() * x1 + m.m11() * y1 + m.m31();
        float bx = m.m00() * x2 + m.m10() * y2 + m.m30(), by = m.m01() * x2 + m.m11() * y2 + m.m31();
        float mx = m.m00() * mouseX + m.m10() * mouseY + m.m30(), my = m.m01() * mouseX + m.m11() * mouseY + m.m31();
        float s = (float) Math.sqrt(Math.abs(m.m00() * m.m11() - m.m01() * m.m10()));
        g.pose().pushPose();
        g.pose().setIdentity();
        //? if >=1.20.2 {
        InventoryScreen.renderEntityInInventoryFollowsMouse(g, Math.round(ax), Math.round(ay), Math.round(bx), Math.round(by),
                Math.max(1, Math.round(size * s)), yOffset, mx, my, entity);
        //?} else {
        /*InventoryScreen.renderEntityInInventoryFollowsMouse(g, Math.round((ax + bx) / 2), Math.round(by - 4 * s - yOffset * size * s),
                Math.max(1, Math.round(size * s)), (ax + bx) / 2 - mx, by - 4 * s - (by - ay) / 2 - my, entity);
        *///?}
        g.pose().popPose();
    }

    @Override
    public void scissor(int x1, int y1, int x2, int y2) {
        g.enableScissor(x1, y1, x2, y2);
    }

    @Override
    public void endScissor() {
        g.disableScissor();
    }

    private boolean pixelSnap() {
        Matrix4f m = g.pose().last().pose();
        if (Math.abs(m.m01()) > 1e-5f || Math.abs(m.m10()) > 1e-5f) return false;
        double gs = me.bbijabnpobatejb.fancyhealth.render.Crisp.guiScale();
        double sx = m.m00() * gs, sy = m.m11() * gs;
        if (Math.abs(sx - Math.rint(sx)) > 1e-3 || Math.abs(sy - Math.rint(sy)) > 1e-3 || Math.rint(sx) < 1 || Math.rint(sy) < 1) return false;
        double tx = m.m30() * gs, ty = m.m31() * gs;
        float dx = (float) ((Math.rint(tx) - tx) / gs), dy = (float) ((Math.rint(ty) - ty) / gs);
        if (Math.abs(dx) < 1e-6f && Math.abs(dy) < 1e-6f) return false;
        g.pose().pushPose();
        g.pose().last().pose().translateLocal(dx, dy, 0);
        return true;
    }
}
