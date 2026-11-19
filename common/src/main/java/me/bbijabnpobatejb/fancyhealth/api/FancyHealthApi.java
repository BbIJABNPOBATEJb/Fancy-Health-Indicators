package me.bbijabnpobatejb.fancyhealth.api;

import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class FancyHealthApi {

    public final String SERVER = "server";
    public final String RESET_CODE = "§h§p§z";
    private final Map<String, Set<Restriction>> OWNERS = new LinkedHashMap<>();
    private final Set<Restriction> ACTIVE = EnumSet.noneOf(Restriction.class);

    public synchronized void restrict(String owner, Restriction... restrictions) {
        Set<Restriction> set = OWNERS.computeIfAbsent(owner, o -> EnumSet.noneOf(Restriction.class));
        Collections.addAll(set, restrictions);
        rebuild();
    }

    public synchronized void clear(String owner) {
        if (OWNERS.remove(owner) != null) rebuild();
    }

    public synchronized boolean allowed(Restriction restriction) {
        return !ACTIVE.contains(Restriction.ALL) && !ACTIVE.contains(restriction);
    }

    public synchronized Set<Restriction> active() {
        return EnumSet.copyOf(ACTIVE.isEmpty() ? EnumSet.noneOf(Restriction.class) : ACTIVE);
    }

    public boolean readCodes(String text) {
        if (text == null || !text.contains("§h§p§")) return false;
        if (text.contains(RESET_CODE)) clear(SERVER);
        for (Restriction r : Restriction.values()) {
            if (text.contains(r.chatCode())) restrict(SERVER, r);
        }
        return true;
    }

    private void rebuild() {
        ACTIVE.clear();
        for (Set<Restriction> set : OWNERS.values()) ACTIVE.addAll(set);
    }
}
