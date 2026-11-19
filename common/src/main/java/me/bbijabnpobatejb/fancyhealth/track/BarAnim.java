package me.bbijabnpobatejb.fancyhealth.track;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class BarAnim {

    private static final float FLASH = 0.35f;

    public float shown;
    public float trail;
    public float target;
    public float flash;
    float trailStart;
    float age = 100f;
    float hold = 0.6f;
    float speed = 1f;

    public void reset(float hp) {
        shown = trail = target = trailStart = hp;
        age = 100f;
        flash = 0;
    }

    public void set(float hp) {
        if (hp < target - 0.001f) {
            trailStart = Math.max(trail(hold, speed), Math.max(shown, target));
            age = 0f;
        }
        target = hp;
    }

    public void freeze(float hp, float from, float age) {
        shown = target = hp;
        trailStart = from;
        this.age = age;
        trail = trail(hold, speed);
        flash = flash();
    }

    public void update(float dt) {
        shown += (target - shown) * Math.min(1f, dt * 14f);
        if (Math.abs(target - shown) < 0.01f) shown = target;
        age += dt;
        trail = trail(hold, speed);
        flash = flash();
    }

    public float trail(float hold, float speed) {
        this.hold = hold;
        this.speed = speed;
        float base = Math.min(shown, target);
        if (trailStart <= base) return base;
        if (age < hold) return trailStart;
        float k = (float) Math.exp(-(age - hold) * Math.max(0.05f, speed) * 4f);
        float v = base + (trailStart - base) * k;
        return v - base < 0.02f ? base : v;
    }

    public float flash() {
        return Math.max(0f, 1f - age / FLASH);
    }
}
