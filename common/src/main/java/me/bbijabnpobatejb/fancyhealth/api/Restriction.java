package me.bbijabnpobatejb.fancyhealth.api;

public enum Restriction {
    ALL("j"), BARS("g"), NUMBERS("q"), PLAYERS("s"), WALLS("w");

    public final String code;

    Restriction(String code) {
        this.code = code;
    }

    public String chatCode() {
        return "§h§p§" + code;
    }
}
