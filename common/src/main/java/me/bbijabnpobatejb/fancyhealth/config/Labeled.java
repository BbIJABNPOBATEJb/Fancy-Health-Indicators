package me.bbijabnpobatejb.fancyhealth.config;

public interface Labeled {

    String name();

    default String key() {
        return "fancyhealth.enum." + getClass().getSimpleName().toLowerCase() + "." + name().toLowerCase();
    }
}
