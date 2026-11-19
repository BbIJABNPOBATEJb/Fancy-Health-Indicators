package me.bbijabnpobatejb.fancyhealth.render;

import lombok.experimental.UtilityClass;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@UtilityClass
public class Projection {

    private final Matrix4f VIEW_PROJ = new Matrix4f();
    private final Vector4f TMP = new Vector4f();
    private Vec3 camera = Vec3.ZERO;
    private final float BASE_M11 = 1.4281480f;
    private boolean valid;
    private float zoom = 1f;

    public void update(Matrix4f projection, Matrix4f view, Vec3 cam) {
        VIEW_PROJ.set(projection).mul(view);
        camera = cam;
        zoom = Math.abs(projection.m11()) / BASE_M11;
        valid = true;
    }

    public void update(Matrix4f viewProjection, Vec3 cam, float m11) {
        VIEW_PROJ.set(viewProjection);
        camera = cam;
        zoom = Math.abs(m11) / BASE_M11;
        valid = true;
    }

    public float zoom() {
        return zoom > 0.01f ? zoom : 1f;
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

    public boolean project(double x, double y, double z, float w, float h, Vector3f out) {
        TMP.set((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z), 1f);
        VIEW_PROJ.transform(TMP);
        if (TMP.w <= 0.05f) return false;
        float nx = TMP.x / TMP.w, ny = TMP.y / TMP.w;
        out.set((nx * 0.5f + 0.5f) * w, (0.5f - ny * 0.5f) * h, TMP.w);
        return true;
    }
}
