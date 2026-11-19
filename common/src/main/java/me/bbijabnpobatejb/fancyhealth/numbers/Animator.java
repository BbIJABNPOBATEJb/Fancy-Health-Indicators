package me.bbijabnpobatejb.fancyhealth.numbers;

import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.config.Animation;

@UtilityClass
public class Animator {

    public static class Frame {
        public float dx;
        public float dy;
        public float scale;
        public float alpha;
        public float rot;
    }

    private float clamp(float v) {
        return v < 0 ? 0 : Math.min(v, 1);
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private float outCubic(float t) {
        float f = 1 - t;
        return 1 - f * f * f;
    }

    private float outBack(float t) {
        float c1 = 1.70158f, c3 = c1 + 1;
        float f = t - 1;
        return 1 + c3 * f * f * f + c1 * f * f;
    }

    public void compute(Animation anim, float age, float life, float s1, float s2, int stack, Frame f) {
        float t = clamp(age / life);
        f.dx = 0;
        f.dy = 0;
        f.scale = 1;
        f.rot = 0;
        float fadeStart = 0.68f;
        f.alpha = t < fadeStart ? 1f : 1f - (t - fadeStart) / (1f - fadeStart);
        if (age < 0.04f) f.alpha *= age / 0.04f;
        switch (anim) {
            case POP -> {
                f.scale = age < 0.09f ? lerp(0.2f, 1.55f, age / 0.09f) : age < 0.24f ? lerp(1.55f, 1f, outCubic((age - 0.09f) / 0.15f)) : 1f;
                f.dx = s1 * 7f;
                f.dy = -6f - 16f * outCubic(t);
            }
            case RISE -> {
                f.dx = s1 * 4f;
                f.dy = -4f - 34f * outCubic(t);
                f.scale = age < 0.08f ? lerp(0.6f, 1f, age / 0.08f) : 1f;
            }
            case FOUNTAIN -> {
                float vx = s1 * 46f, vy = -92f - s2 * 22f, g = 250f;
                f.dx = vx * age;
                f.dy = vy * age + 0.5f * g * age * age;
                f.scale = 1f - 0.25f * t;
                f.rot = s1 * 0.25f * t;
            }
            case BOUNCE -> {
                float k = (float) Math.exp(-age * 3.2f);
                f.dy = -Math.abs((float) Math.sin(age * 8.5f + 0.25f)) * 30f * k - 4f;
                f.dx = s1 * 26f * outCubic(t);
                f.scale = 1f + 0.12f * k;
            }
            case ZOOM -> {
                if (age < 0.11f) {
                    float p = age / 0.11f;
                    f.scale = lerp(3.4f, 0.92f, p * p);
                    f.alpha *= p;
                } else if (age < 0.22f) {
                    f.scale = lerp(0.92f, 1.12f, outCubic((age - 0.11f) / 0.11f));
                } else {
                    f.scale = 1.12f;
                }
                f.dx = s1 * 5f;
                f.dy = -8f - (t > 0.5f ? (t - 0.5f) * 30f : 0f);
            }
            case SHAKE -> {
                float amp = 3.2f * (float) Math.exp(-age * 4.5f);
                f.dx = (float) Math.sin(age * 83f + s1 * 9f) * amp + s1 * 6f;
                f.dy = (float) Math.cos(age * 67f + s2 * 9f) * amp - 10f - 8f * t;
                f.scale = age < 0.08f ? lerp(1.9f, 1.25f, age / 0.08f) : lerp(1.25f, 1f, clamp((age - 0.08f) / 0.3f));
                f.rot = (float) Math.sin(age * 50f) * 0.12f * (float) Math.exp(-age * 5f);
            }
            case SLIDE -> {
                float dir = s1 >= 0 ? 1f : -1f;
                f.dx = dir * (14f + 30f * outCubic(clamp(age / 0.45f)));
                f.dy = -10f - 10f * t + s2 * 4f;
                f.scale = age < 0.1f ? lerp(0.5f, 1.1f, age / 0.1f) : lerp(1.1f, 1f, clamp((age - 0.1f) / 0.2f));
                f.rot = -dir * 0.08f * (1 - t);
            }
            case SPIRAL -> {
                float a = age * 6.5f + s1 * 3.14f;
                float r = 10f + 14f * t;
                f.dx = (float) Math.cos(a) * r;
                f.dy = (float) Math.sin(a) * r * 0.55f - 30f * t - 6f;
                f.scale = age < 0.1f ? outBack(age / 0.1f) : 1f;
                f.rot = (float) Math.sin(a) * 0.15f;
            }
            case STACK -> {
                float in = clamp(age / 0.12f);
                f.dx = 18f + (1 - outCubic(in)) * 16f;
                f.dy = -stack * 10f;
                f.alpha *= in;
                f.scale = 1f;
            }
        }
    }
}
