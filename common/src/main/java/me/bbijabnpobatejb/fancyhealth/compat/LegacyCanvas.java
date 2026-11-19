//? if <1.20
package me.bbijabnpobatejb.fancyhealth.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.bbijabnpobatejb.fancyhealth.render.Canvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
//? if >=1.19.3 {
import org.joml.Matrix4f;
//?} else {
/*import com.mojang.math.Matrix4f;
*///?}

public record LegacyCanvas(PoseStack pose) implements Canvas {

    private static final Painter PAINTER = new Painter();

    private static final class Painter extends GuiComponent {
        void gradient(PoseStack pose, int x1, int y1, int x2, int y2, int top, int bottom) {
            fillGradient(pose, x1, y1, x2, y2, top, bottom);
        }
    }

    private float[] matrix() {
        //? if >=1.19.3 {
        return pose.last().pose().get(new float[16]);
        //?} else {
        /*java.nio.FloatBuffer buffer = java.nio.FloatBuffer.allocate(16);
        pose.last().pose().store(buffer);
        return buffer.array();
        *///?}
    }

    @Override
    public int width() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    @Override
    public int height() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    @Override
    public void push() {
        pose.pushPose();
    }

    @Override
    public void pop() {
        pose.popPose();
    }

    @Override
    public void translate(float x, float y) {
        pose.translate(x, y, 0);
    }

    @Override
    public void scale(float x, float y) {
        pose.scale(x, y, 1);
    }

    @Override
    public void identity() {
        //? if >=1.19.3 {
        pose.last().pose().identity();
        //?} else {
        /*pose.last().pose().setIdentity();
        *///?}
    }

    @Override
    public void rotate(float radians) {
        //? if >=1.19.3 {
        pose.mulPose(com.mojang.math.Axis.ZP.rotation(radians));
        //?} else {
        /*pose.mulPose(com.mojang.math.Vector3f.ZP.rotation(radians));
        *///?}
    }

    @Override
    public void shear(float x) {
        //? if >=1.19.3 {
        pose.last().pose().mul(new Matrix4f(1, 0, 0, 0, x, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1));
        //?} else if <1.17 {
        /*return;
        *///?} else {
        /*Matrix4f m = new Matrix4f();
        m.setIdentity();
        java.nio.FloatBuffer buffer = java.nio.FloatBuffer.wrap(new float[]{1, 0, 0, 0, x, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});
        m.load(buffer);
        pose.last().pose().multiply(m);
        *///?}
    }

    @Override
    public void fill(int x1, int y1, int x2, int y2, int argb) {
        if (x2 <= x1 || y2 <= y1 || (argb >>> 24) == 0) return;
        boolean pushed = pixelSnap();
        GuiComponent.fill(pose, x1, y1, x2, y2, argb);
        if (pushed) pose.popPose();
    }

    @Override
    public void gradient(int x1, int y1, int x2, int y2, int top, int bottom) {
        if (x2 > x1 && y2 > y1) PAINTER.gradient(pose, x1, y1, x2, y2, top, bottom);
    }

    @Override
    public void text(Font font, Component text, int x, int y, int argb, boolean shadow) {
        float[] m = matrix();
        float ps = (float) Math.sqrt(Math.abs(m[0] * m[5] - m[1] * m[4]));
        double gs = me.bbijabnpobatejb.fancyhealth.render.Crisp.guiScale();
        float ox = m[0] * x + m[4] * y + m[12], oy = m[1] * x + m[5] * y + m[13];
        float dx = (float) ((Math.round(ox * gs) - ox * gs) / gs), dy = (float) ((Math.round(oy * gs) - oy * gs) / gs);
        pose.pushPose();
        if (Math.abs(m[0]) > 1e-6f && Math.abs(m[5]) > 1e-6f) pose.translate(dx / m[0], dy / m[5], 0);
        net.minecraft.util.FormattedCharSequence seq = me.bbijabnpobatejb.fancyhealth.render.Crisp.sequence(text, ps);
        RenderSystem.enableBlend();
        if (shadow) font.drawShadow(pose, seq, x, y, argb);
        else font.draw(pose, seq, x, y, argb);
        pose.popPose();
    }

    @Override
    public void sprite(String texture, int x, int y, int w, int h, int u, int v, int texW, int texH, int argb) {
        boolean pushed = pixelSnap();
        float r = (argb >> 16 & 255) / 255f, g = (argb >> 8 & 255) / 255f, b = (argb & 255) / 255f, a = (argb >>> 24) / 255f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        //? if >=1.17 {
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Mc.id(texture));
        RenderSystem.setShaderColor(r, g, b, a);
        GuiComponent.blit(pose, x, y, (float) u, (float) v, w, h, texW, texH);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        //?} else {
        /*Minecraft.getInstance().getTextureManager().bind(Mc.id(texture));
        RenderSystem.color4f(r, g, b, a);
        GuiComponent.blit(pose, x, y, (float) u, (float) v, w, h, texW, texH);
        RenderSystem.color4f(1, 1, 1, 1);
        *///?}
        if (pushed) pose.popPose();
    }

    @Override
    public void guiSprite(String id, int x, int y, int w, int h, int argb) {
        int[] uv = Mc.legacyHeart(id);
        if (uv != null) sprite("minecraft:textures/gui/icons.png", x, y, w, h, uv[0], uv[1], 256, 256, argb);
    }

    @Override
    public void entity(LivingEntity entity, int x1, int y1, int x2, int y2, int size, float yOffset, float mouseX, float mouseY) {
        float[] m = matrix();
        float ax = m[0] * x1 + m[4] * y1 + m[12], ay = m[1] * x1 + m[5] * y1 + m[13];
        float bx = m[0] * x2 + m[4] * y2 + m[12], by = m[1] * x2 + m[5] * y2 + m[13];
        float mx = m[0] * mouseX + m[4] * mouseY + m[12], my = m[1] * mouseX + m[5] * mouseY + m[13];
        float s = (float) Math.sqrt(Math.abs(m[0] * m[5] - m[1] * m[4]));
        int cx = Math.round((ax + bx) / 2), feet = Math.round(by - 4 * s - yOffset * size * s);
        int scale = Math.max(1, Math.round(size * s));
        //? if >=1.19.4 {
        PoseStack identity = new PoseStack();
        InventoryScreen.renderEntityInInventoryFollowsMouse(identity, cx, feet, scale, cx - mx, feet - (by - ay) / 2 - my, entity);
        //?} else {
        /*InventoryScreen.renderEntityInInventory(cx, feet, scale, cx - mx, feet - (by - ay) / 2 - my, entity);
        *///?}
    }

    @Override
    public void scissor(int x1, int y1, int x2, int y2) {
        float[] m = matrix();
        double s = Minecraft.getInstance().getWindow().getGuiScale();
        int wh = Minecraft.getInstance().getWindow().getHeight();
        double ax = (m[0] * x1 + m[12]) * s, ay = (m[5] * y1 + m[13]) * s, bx = (m[0] * x2 + m[12]) * s, by = (m[5] * y2 + m[13]) * s;
        RenderSystem.enableScissor((int) ax, (int) (wh - by), Math.max(0, (int) (bx - ax)), Math.max(0, (int) (by - ay)));
    }

    @Override
    public void endScissor() {
        RenderSystem.disableScissor();
    }

    private boolean pixelSnap() {
        float[] m = matrix();
        if (Math.abs(m[1]) > 1e-5f || Math.abs(m[4]) > 1e-5f) return false;
        double gs = me.bbijabnpobatejb.fancyhealth.render.Crisp.guiScale();
        double sx = m[0] * gs, sy = m[5] * gs;
        if (Math.abs(sx - Math.rint(sx)) > 1e-3 || Math.abs(sy - Math.rint(sy)) > 1e-3 || Math.rint(sx) < 1 || Math.rint(sy) < 1) return false;
        double tx = m[12] * gs, ty = m[13] * gs;
        float dx = (float) ((Math.rint(tx) - tx) / gs), dy = (float) ((Math.rint(ty) - ty) / gs);
        if (Math.abs(dx) < 1e-6f && Math.abs(dy) < 1e-6f) return false;
        pose.pushPose();
        pose.translate(dx / m[0], dy / m[5], 0);
        return true;
    }
}
