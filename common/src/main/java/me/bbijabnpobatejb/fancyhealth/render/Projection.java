package me.bbijabnpobatejb.fancyhealth.render;

import lombok.experimental.UtilityClass;
import net.minecraft.world.phys.Vec3;

@UtilityClass
public class Projection {

    private final float[] VIEW_PROJ = new float[16];
    private final float BASE_M11 = 1.4281480f;
    private Vec3 camera = Vec3.ZERO;
    private boolean valid;
    private float zoom = 1f;

    public void update(float[] projection, float[] view, Vec3 cam) {
        for (int c = 0; c < 4; c++) {
            for (int r = 0; r < 4; r++) {
                float v = 0;
                for (int k = 0; k < 4; k++) v += projection[k * 4 + r] * view[c * 4 + k];
                VIEW_PROJ[c * 4 + r] = v;
            }
        }
        camera = cam;
        zoom = Math.abs(projection[5]) / BASE_M11;
        valid = true;
    }

    public void update(float[] viewProjection, Vec3 cam, float m11) {
        System.arraycopy(viewProjection, 0, VIEW_PROJ, 0, 16);
        camera = cam;
        zoom = Math.abs(m11) / BASE_M11;
        valid = true;
    }

    public float zoom() {
        return zoom > 0.01f ? zoom : 1f;
    }

    public float pixelsPerBlock(double dist, float screenHeight) {
        return (float) (screenHeight / 2f * BASE_M11 * zoom() / Math.max(dist, 0.5));
    }

    public float nearness(double dist, double reference, float min, float max) {
        return (float) Math.max(min, Math.min(max, zoom() * reference / Math.max(dist, 0.5)));
    }

    public boolean valid() {
        return valid;
    }

    public Vec3 camera() {
        return camera;
    }

    public boolean project(double x, double y, double z, float w, float h, float[] out) {
        float dx = (float) (x - camera.x), dy = (float) (y - camera.y), dz = (float) (z - camera.z);
        float[] m = VIEW_PROJ;
        float px = m[0] * dx + m[4] * dy + m[8] * dz + m[12];
        float py = m[1] * dx + m[5] * dy + m[9] * dz + m[13];
        float pw = m[3] * dx + m[7] * dy + m[11] * dz + m[15];
        if (pw <= 0.05f) return false;
        out[0] = (px / pw * 0.5f + 0.5f) * w;
        out[1] = (0.5f - py / pw * 0.5f) * h;
        out[2] = pw;
        return true;
    }
}
