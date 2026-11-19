package me.bbijabnpobatejb.fancyhealth.render;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Crisp {

    public final String[] FAMILIES = {"ui", "ui_bold", "modern", "heavy", "sport", "light"};
    public final float[] STEPS = steps();

    private final Map<String, FontDescription[]> VARIANTS = new HashMap<>();

    private float[] steps() {
        float[] out = new float[25 + 16 + 8];
        int n = 0;
        for (int i = 0; i <= 24; i++) out[n++] = 1f + i * 0.125f;
        for (int i = 1; i <= 16; i++) out[n++] = 4f + i * 0.25f;
        for (int i = 1; i <= 8; i++) out[n++] = 8f + i * 0.5f;
        return out;
    }

    public String suffix(float step) {
        return "_x" + Math.round(step * 1000);
    }

    public int stepIndex(float deviceScale) {
        int best = 0;
        float bestErr = Float.MAX_VALUE;
        for (int i = 0; i < STEPS.length; i++) {
            float err = Math.abs(STEPS[i] - deviceScale);
            if (err < bestErr) {
                bestErr = err;
                best = i;
            }
        }
        return best;
    }

    public double guiScale() {
        return Minecraft.getInstance().getWindow().getGuiScale();
    }

    private FontDescription variant(FontDescription font, int step) {
        if (!(font instanceof FontDescription.Resource r) || !"fancyhealth".equals(r.id().getNamespace())) return null;
        FontDescription[] arr = VARIANTS.computeIfAbsent(r.id().getPath(), path -> {
            for (String f : FAMILIES) if (f.equals(path)) return new FontDescription[STEPS.length];
            return null;
        });
        if (arr == null) return null;
        FontDescription d = arr[step];
        if (d == null) arr[step] = d = new FontDescription.Resource(Identifier.fromNamespaceAndPath("fancyhealth", "crisp/" + r.id().getPath() + suffix(STEPS[step])));
        return d;
    }

    public FormattedCharSequence sequence(Component text, float poseScale) {
        FormattedCharSequence base = text.getVisualOrderText();
        int step = stepIndex((float) (poseScale * guiScale()));
        return sink -> {
            Style[] cache = new Style[2];
            return base.accept((i, style, cp) -> {
                if (style != cache[0]) {
                    cache[0] = style;
                    FontDescription v = variant(style.getFont(), step);
                    cache[1] = v == null ? style : style.withFont(v);
                }
                return sink.accept(i, cache[1], cp);
            });
        };
    }
}
