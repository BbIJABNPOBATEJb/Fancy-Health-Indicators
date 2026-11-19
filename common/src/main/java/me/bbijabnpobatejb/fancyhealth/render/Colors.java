package me.bbijabnpobatejb.fancyhealth.render;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Colors {

    public int alpha(int argb, float a) {
        int al = Math.round(((argb >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, a)));
        return (al << 24) | (argb & 0xFFFFFF);
    }

    public int withAlpha(int rgb, int a) {
        return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF);
    }

    public int mul(int a, int b) {
        int r = (a >> 16 & 255) * (b >> 16 & 255) / 255, g = (a >> 8 & 255) * (b >> 8 & 255) / 255, bl = (a & 255) * (b & 255) / 255;
        return (a & 0xFF000000) | r << 16 | g << 8 | bl;
    }

    public int mix(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = a >>> 24, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = b >>> 24, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return (Math.round(aa + (ba - aa) * t) << 24) | (Math.round(ar + (br - ar) * t) << 16)
                | (Math.round(ag + (bg - ag) * t) << 8) | Math.round(ab + (bb - ab) * t);
    }

    public int lighten(int c, float t) {
        return mix(c, 0xFFFFFF | (c & 0xFF000000), t);
    }

    public int darken(int c, float t) {
        return mix(c, c & 0xFF000000, t);
    }

    public int health(float pct) {
        if (pct > 0.5f) return mix(0xFFFFD23A, 0xFF46D35C, (pct - 0.5f) * 2f);
        return mix(0xFFFF3B30, 0xFFFFD23A, pct * 2f);
    }

    public int hsv(float h, float s, float v) {
        int rgb = java.awt.Color.HSBtoRGB(h, s, v);
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    public float[] toHsv(int argb) {
        return java.awt.Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, null);
    }
}
