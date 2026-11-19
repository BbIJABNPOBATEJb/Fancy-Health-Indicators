package me.bbijabnpobatejb.fancyhealth.render;

import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.config.FontChoice;
import me.bbijabnpobatejb.fancyhealth.config.TextEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.util.EnumMap;
import java.util.Map;

@UtilityClass
public class TextFx {

    private final Map<FontChoice, FontDescription> FONTS = new EnumMap<>(FontChoice.class);
    private final int[][] RING1 = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};
    private final int[][] RING2 = {{-2, -1}, {-2, 0}, {-2, 1}, {2, -1}, {2, 0}, {2, 1}, {-1, -2}, {0, -2}, {1, -2}, {-1, 2}, {0, 2}, {1, 2}, {-1, -1}, {1, -1}, {-1, 1}, {1, 1}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public Font font() {
        return Minecraft.getInstance().font;
    }

    public MutableComponent styled(String text, FontChoice font) {
        MutableComponent c = Component.literal(text);
        if (font != null && font.id != null) {
            FontDescription d = FONTS.computeIfAbsent(font, f -> new FontDescription.Resource(Identifier.parse(f.id)));
            c.setStyle(Style.EMPTY.withFont(d));
        }
        return c;
    }

    public Component plain(Component c) {
        return Component.literal(c.getString()).setStyle(Style.EMPTY.withFont(c.getStyle().getFont()));
    }

    public int width(Component c) {
        return font().width(c);
    }

    public void draw(Canvas canvas, Component text, int x, int y, int argb, TextEffect fx, int outline) {
        Font font = font();
        float a = (argb >>> 24) / 255f;
        if (a <= 0.02f) return;
        switch (fx) {
            case NONE -> canvas.text(font, text, x, y, argb, false);
            case SHADOW -> canvas.text(font, text, x, y, argb, true);
            case OUTLINE -> {
                Component p = plain(text);
                int oc = Colors.alpha(outline, a * a);
                for (int[] o : RING1) canvas.text(font, p, x + o[0], y + o[1], oc, false);
                canvas.text(font, text, x, y, argb, false);
            }
            case THICK -> {
                Component p = plain(text);
                int oc = Colors.alpha(outline, a * a);
                for (int[] o : RING2) canvas.text(font, p, x + o[0], y + o[1], oc, false);
                canvas.text(font, text, x, y, argb, false);
            }
            case GLOW -> {
                Component p = plain(text);
                int g1 = Colors.alpha(argb, 0.22f * a), dark = Colors.alpha(Colors.darken(argb, 0.8f), 0.75f * a * a);
                for (int[] o : RING2) {
                    if (Math.abs(o[0]) == 2 || Math.abs(o[1]) == 2) canvas.text(font, p, x + o[0], y + o[1], g1, false);
                }
                for (int[] o : RING1) canvas.text(font, p, x + o[0], y + o[1], dark, false);
                canvas.text(font, text, x, y, Colors.lighten(argb, 0.2f), false);
            }
        }
    }

    public void centered(Canvas canvas, Component text, int cx, int y, int argb, TextEffect fx, int outline) {
        draw(canvas, text, cx - width(text) / 2, y, argb, fx, outline);
    }
}
