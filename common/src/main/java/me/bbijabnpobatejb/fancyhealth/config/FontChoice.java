package me.bbijabnpobatejb.fancyhealth.config;

public enum FontChoice implements Labeled {
    DEFAULT(null), UNIFORM("minecraft:uniform"), CHUNKY("fancyhealth:chunky"), ROUND("fancyhealth:round"),
    DIGITAL("fancyhealth:digital"), SLIM("fancyhealth:slim"), COMIC("fancyhealth:comic"),
    MODERN("fancyhealth:modern"), HEAVY("fancyhealth:heavy"), SPORT("fancyhealth:sport"), LIGHT("fancyhealth:light"), TINY("fancyhealth:tiny");

    public final String id;

    FontChoice(String id) {
        this.id = id;
    }
}
