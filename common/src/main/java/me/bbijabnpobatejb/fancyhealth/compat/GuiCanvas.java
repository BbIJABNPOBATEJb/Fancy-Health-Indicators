//? if >=1.21.6
package me.bbijabnpobatejb.fancyhealth.compat;

import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import net.minecraft.client.gui.Font;
//? if <26.1 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?}
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

//? if <26.1 {
public record GuiCanvas(GuiGraphics g) implements Canvas {
//?} else {
/*public record GuiCanvas(GuiGraphicsExtractor g) implements Canvas {
*///?}

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
        g.pose().pushMatrix();
    }

    @Override
    public void pop() {
        g.pose().popMatrix();
    }

    @Override
    public void translate(float x, float y) {
        g.pose().translate(x, y);
    }

    @Override
    public void scale(float x, float y) {
        g.pose().scale(x, y);
    }

    @Override
    public void identity() {
        g.pose().identity();
    }

    @Override
    public void rotate(float radians) {
        g.pose().rotate(radians);
    }

    @Override
    public void shear(float x) {
        g.pose().mul(new Matrix3x2f(1, 0, x, 1, 0, 0));
    }

    @Override
    public void fill(int x1, int y1, int x2, int y2, int argb) {
        if (x2 <= x1 || y2 <= y1 || (argb >>> 24) == 0) return;
        boolean pushed = pixelSnap();
        g.fill(x1, y1, x2, y2, argb);
        if (pushed) g.pose().popMatrix();
    }

    @Override
    public void gradient(int x1, int y1, int x2, int y2, int top, int bottom) {
        if (x2 > x1 && y2 > y1) g.fillGradient(x1, y1, x2, y2, top, bottom);
    }

    @Override
    public void text(Font font, Component text, int x, int y, int argb, boolean shadow) {
        org.joml.Matrix3x2f m = g.pose();
        float ps = (float) Math.sqrt(Math.abs(m.determinant()));
        double gs = me.bbijabnpobatejb.fancyhealth.render.Crisp.guiScale();
        float ox = (m.m00() * x + m.m10() * y + m.m20()), oy = (m.m01() * x + m.m11() * y + m.m21());
        float dx = (float) ((Math.round(ox * gs) - ox * gs) / gs), dy = (float) ((Math.round(oy * gs) - oy * gs) / gs);
        g.pose().pushMatrix();
        m.translateLocal(dx, dy);
        //? if <26.1 {
        g.drawString(font, me.bbijabnpobatejb.fancyhealth.render.Crisp.sequence(text, ps), x, y, argb, shadow);
        //?} else {
        /*g.text(font, me.bbijabnpobatejb.fancyhealth.render.Crisp.sequence(text, ps), x, y, argb, shadow);
        *///?}
        g.pose().popMatrix();
    }

    @Override
    public void sprite(String texture, int x, int y, int w, int h, int u, int v, int texW, int texH, int argb) {
        boolean pushed = pixelSnap();
        g.blit(RenderPipelines.GUI_TEXTURED, Mc.id(texture), x, y, u, v, w, h, texW, texH, argb);
        if (pushed) g.pose().popMatrix();
    }

    @Override
    public void guiSprite(String id, int x, int y, int w, int h, int argb) {
        boolean pushed = pixelSnap();
        g.blitSprite(RenderPipelines.GUI_TEXTURED, Mc.id(id), x, y, w, h, argb);
        if (pushed) g.pose().popMatrix();
    }

    @Override
    public void entity(LivingEntity entity, int x1, int y1, int x2, int y2, int size, float yOffset, float mouseX, float mouseY) {
        Matrix3x2f m = g.pose();
        Vector2f a = m.transformPosition(x1, y1, new Vector2f());
        Vector2f b = m.transformPosition(x2, y2, new Vector2f());
        Vector2f mouse = m.transformPosition(mouseX, mouseY, new Vector2f());
        float s = (float) Math.sqrt(Math.abs(m.determinant()));
        //? if <26.1 {
        InventoryScreen.renderEntityInInventoryFollowsMouse(g, Math.round(a.x), Math.round(a.y), Math.round(b.x), Math.round(b.y),
        //?} else {
        /*InventoryScreen.extractEntityInInventoryFollowsMouse(g, Math.round(a.x), Math.round(a.y), Math.round(b.x), Math.round(b.y),
        *///?}
                Math.max(1, Math.round(size * s)), yOffset, mouse.x, mouse.y, entity);
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
        org.joml.Matrix3x2f m = g.pose();
        if (Math.abs(m.m01()) > 1e-5f || Math.abs(m.m10()) > 1e-5f) return false;
        double gs = me.bbijabnpobatejb.fancyhealth.render.Crisp.guiScale();
        double sx = m.m00() * gs, sy = m.m11() * gs;
        if (Math.abs(sx - Math.rint(sx)) > 1e-3 || Math.abs(sy - Math.rint(sy)) > 1e-3 || Math.rint(sx) < 1 || Math.rint(sy) < 1) return false;
        double tx = m.m20() * gs, ty = m.m21() * gs;
        float dx = (float) ((Math.rint(tx) - tx) / gs), dy = (float) ((Math.rint(ty) - ty) / gs);
        if (Math.abs(dx) < 1e-6f && Math.abs(dy) < 1e-6f) return false;
        g.pose().pushMatrix();
        m.translateLocal(dx, dy);
        return true;
    }
}
