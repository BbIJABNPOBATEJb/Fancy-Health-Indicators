package me.bbijabnpobatejb.fancyhealth.ui;

import me.bbijabnpobatejb.fancyhealth.config.FhConfig;
import me.bbijabnpobatejb.fancyhealth.config.Labeled;
import me.bbijabnpobatejb.fancyhealth.render.Colors;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Search {

    public record Hit(Rows.Row row, ConfigUi.Tab tab, String section, int score, int order) {
    }

    private static final String EN_KEYS = "qwertyuiop[]asdfghjkl;'zxcvbnm,.`";
    private static final String RU_KEYS = "йцукенгшщзхъфывапролджэячсмитьбюё";
    private static final Pattern HEX = Pattern.compile("#?([0-9a-f]{6}|[0-9a-f]{3})");
    private static final String MODIFIED_WORDS = "изменено измененные изменил отличается свои changed modified edited custom";
    private static final String TOGGLE_ON = "вкл включено on enabled";
    private static final String TOGGLE_OFF = "выкл выключено off disabled";

    private static final String[][] HUES = {
            {"red красн алый scarlet", "345", "15"},
            {"orange оранж", "15", "42"},
            {"yellow gold желт золот", "42", "70"},
            {"green lime зелен салат", "70", "160"},
            {"cyan aqua teal голуб бирюз", "160", "200"},
            {"blue син", "200", "250"},
            {"purple violet фиолет пурпур сирен", "250", "290"},
            {"pink magenta розов малин", "290", "345"},
    };
    private static final String[][] NAMED = {
            {"red красн", "FF0000"}, {"orange оранж", "FF8C00"}, {"yellow желт", "FFE000"}, {"gold золот", "FFC21F"},
            {"green зелен", "30D158"}, {"lime салат", "8CFF3C"}, {"cyan aqua голуб бирюз", "40E0FF"}, {"blue син", "0A60FF"},
            {"purple violet фиолет сирен", "A050F0"}, {"pink розов", "FF69B4"}, {"white бел", "FFFFFF"}, {"black черн", "000000"},
            {"gray grey сер", "8E8E93"},
    };

    private Search() {
    }

    public static String norm(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        boolean space = true;
        for (char ch : s.toLowerCase(Locale.ROOT).toCharArray()) {
            if (ch == 'ё') ch = 'е';
            boolean keep = Character.isLetterOrDigit(ch) || ch == '#' || ch == '%' || ch == '.';
            if (keep) {
                sb.append(ch);
                space = false;
            } else if (!space) {
                sb.append(' ');
                space = true;
            }
        }
        return sb.toString().trim();
    }

    private static String swap(String s, String from, String to) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char ch : s.toCharArray()) {
            int i = from.indexOf(ch);
            sb.append(i >= 0 ? to.charAt(i) : ch);
        }
        return sb.toString();
    }

    private static String tr(String key) {
        return Language.getInstance().has(key) ? Component.translatable(key).getString() : "";
    }

    private static String opt(String key) {
        int i = key.lastIndexOf('.');
        return i < 0 ? key : key.substring(i + 1);
    }

    static String colorWords(int argb) {
        int a = argb >>> 24;
        if (a < 0x20) return "transparent прозрач невидим";
        float[] hsv = Colors.toHsv(argb);
        float h = hsv[0] * 360f, sat = hsv[1], v = hsv[2];
        if (v < 0.18f) return "black черн темн dark";
        if (sat < 0.18f) return v > 0.85f ? "white бел светл light" : "gray grey сер";
        StringBuilder sb = new StringBuilder();
        for (String[] hue : HUES) {
            float from = Float.parseFloat(hue[1]), to = Float.parseFloat(hue[2]);
            boolean in = from > to ? h >= from || h < to : h >= from && h < to;
            if (in) sb.append(hue[0]);
        }
        if (v < 0.45f) sb.append(" dark темн");
        else if (sat < 0.45f) sb.append(" light светл pale бледн");
        return sb.toString();
    }

    private static int colorFromToken(String t) {
        Matcher m = HEX.matcher(t);
        if (m.matches() && (t.startsWith("#") || t.length() == 6 || t.chars().anyMatch(ch -> ch >= 'a' && ch <= 'f'))) {
            String hex = m.group(1);
            if (hex.length() == 3) hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
            return 0xFF000000 | Integer.parseInt(hex, 16);
        }
        return 0;
    }

    private static float colorDistance(int a, int b) {
        float dr = ((a >> 16 & 255) - (b >> 16 & 255)) / 255f, dg = ((a >> 8 & 255) - (b >> 8 & 255)) / 255f, db = ((a & 255) - (b & 255)) / 255f;
        return (float) Math.sqrt((dr * dr * 0.3f + dg * dg * 0.59f + db * db * 0.11f));
    }

    private static int lev(String a, String b) {
        int[] prev = new int[b.length() + 1], cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] t = prev;
            prev = cur;
            cur = t;
        }
        return prev[b.length()];
    }

    private static float fieldScore(String token, String text, float w) {
        if (text.isEmpty()) return 0;
        if (text.startsWith(token)) return w;
        float best = 0;
        for (String word : text.split(" ")) {
            if (word.isEmpty()) continue;
            if (word.startsWith(token)) return w * 0.85f;
            int common = 0;
            int n = Math.min(word.length(), token.length());
            while (common < n && word.charAt(common) == token.charAt(common)) common++;
            if (common >= 3 && common >= Math.min(word.length(), token.length()) - (token.length() >= 6 ? 2 : 1)) best = Math.max(best, w * 0.7f);
            if (token.length() >= 4) {
                String head = word.substring(0, Math.min(word.length(), token.length()));
                int d = lev(token, head);
                if (d <= (token.length() >= 7 ? 2 : 1)) best = Math.max(best, w * 0.45f);
            }
        }
        if (best == 0 && token.length() >= 3 && text.contains(token)) best = w * 0.55f;
        return best;
    }

    private record Fields(String label, String tags, String place, String desc, String value, String values, String color, int argb, boolean modified) {
    }

    private static Fields fields(Rows.Row r, String tabText, String sectionText, FhConfig cur, FhConfig def) {
        String o = opt(r.key);
        String label = norm(Component.translatable(r.key).getString());
        String tags = norm(tr("fancyhealth.tags.option." + o) + " " + o.replace('_', ' '));
        String desc = r.descKey() != null ? norm(tr(r.descKey())) : "";
        String value = "", values = "", color = "";
        int argb = 0;
        if (r instanceof Rows.Toggle t) {
            value = norm((Boolean) t.rawGet(cur) ? TOGGLE_ON : TOGGLE_OFF);
        } else if (r instanceof Rows.Cycle<?> c) {
            Object v = c.rawGet(cur);
            if (v instanceof Labeled l) value = norm(tr(l.key()) + " " + tr(l.key().replace("fancyhealth.enum.", "fancyhealth.tags.enum.")));
            StringBuilder sb = new StringBuilder();
            for (Object e : c.values()) {
                if (e instanceof Labeled l) sb.append(tr(l.key())).append(' ').append(tr(l.key().replace("fancyhealth.enum.", "fancyhealth.tags.enum."))).append(' ');
            }
            values = norm(sb.toString());
        } else if (r instanceof Rows.Slider s) {
            float v = (Float) s.rawGet(cur);
            value = Math.abs(v - Math.round(v)) < 0.001f ? String.valueOf(Math.round(v)) : String.format(Locale.ROOT, "%.2f", v);
        } else if (r instanceof Rows.ColorRow c) {
            argb = c.getter().apply(cur);
            color = norm(String.format(Locale.ROOT, "#%06x %06x ", argb & 0xFFFFFF, argb & 0xFFFFFF) + colorWords(argb));
        } else if (r instanceof PresetRow) {
            tags = norm(tr("fancyhealth.tags.option." + o) + " " + o + " preset пресет шаблон");
        }
        boolean modified = r.value() && def != null && r.modified(cur, def);
        return new Fields(label, tags, norm(tabText + " " + sectionText), desc, value, values, color, argb, modified);
    }

    private static float score(List<String> tokens, String phrase, Fields f) {
        float total = 0;
        for (String t : tokens) {
            float best = Math.max(fieldScore(t, f.label, 100), fieldScore(t, f.tags, 60));
            best = Math.max(best, fieldScore(t, f.place, 38));
            best = Math.max(best, fieldScore(t, f.desc, 18));
            best = Math.max(best, fieldScore(t, f.value, 45));
            best = Math.max(best, fieldScore(t, f.values, 28));
            best = Math.max(best, fieldScore(t, f.color, 60));
            if (f.modified && fieldScore(t, MODIFIED_WORDS, 1) >= 0.7f) best = Math.max(best, 90);
            int want = colorFromToken(t);
            if (want == 0) {
                for (String[] named : NAMED) {
                    if (fieldScore(t, named[0], 1) >= 0.7f) want = 0xFF000000 | Integer.parseInt(named[1], 16);
                }
            }
            if (want != 0 && f.color.length() > 0) {
                float d = colorDistance(want, f.argb);
                if (d < 0.3f) best = Math.max(best, 75 * (1 - d / 0.3f));
            }
            if (best <= 0) return 0;
            total += best;
        }
        if (tokens.size() > 1 && f.label.contains(phrase)) total += 50;
        return total;
    }

    public static List<Hit> find(String raw, Map<ConfigUi.Tab, List<Rows.Row>> tabs, FhConfig cur, FhConfig def) {
        String q = norm(raw);
        List<Hit> hits = new ArrayList<>();
        if (q.isEmpty()) return hits;
        String[] variants = {q, norm(swap(q, EN_KEYS, RU_KEYS)), norm(swap(q, RU_KEYS, EN_KEYS))};
        List<List<String>> tokenSets = new ArrayList<>();
        for (int i = 0; i < variants.length; i++) {
            if (i > 0 && variants[i].equals(q)) continue;
            tokenSets.add(List.of(variants[i].split(" ")));
        }
        int order = 0;
        for (ConfigUi.Tab tab : ConfigUi.Tab.values()) {
            String tabText = Component.translatable(tab.key()).getString() + " " + tr("fancyhealth.tags.tab." + tab.name().toLowerCase(Locale.ROOT));
            String section = "", sectionText = "";
            for (Rows.Row r : tabs.get(tab)) {
                order++;
                if (r instanceof Rows.Header) {
                    section = r.key;
                    sectionText = Component.translatable(r.key).getString() + " " + tr(r.key.replace("fancyhealth.option.section.", "fancyhealth.tags.section."));
                    continue;
                }
                if (r.descKey() == null && !(r instanceof PresetRow)) continue;
                Fields f = fields(r, tabText, sectionText, cur, def);
                float best = 0;
                for (int i = 0; i < tokenSets.size(); i++) {
                    List<String> tokens = tokenSets.get(i);
                    best = Math.max(best, score(tokens, String.join(" ", tokens), f) * (i == 0 ? 1f : 0.9f));
                }
                if (best > 0) hits.add(new Hit(r, tab, section, Math.round(best), order));
            }
        }
        int top = hits.stream().mapToInt(Hit::score).max().orElse(0);
        hits.removeIf(h -> h.score < top * 0.3f);
        Map<ConfigUi.Tab, Integer> groupBest = new LinkedHashMap<>();
        for (Hit h : hits) groupBest.merge(h.tab, h.score, Math::max);
        hits.sort(Comparator.<Hit>comparingInt(h -> -groupBest.get(h.tab)).thenComparing(h -> h.tab.ordinal()).thenComparingInt(Hit::order));
        return hits;
    }
}
