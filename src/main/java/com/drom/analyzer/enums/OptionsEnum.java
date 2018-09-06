package com.drom.analyzer.enums;

/**
 * @author Ilya Savchenko
 * @email ilyasavchenko1990@gmail.com
 * @date 03.09.2018
 */
public enum OptionsEnum {
    t("seconds", "minimal allowable response time"),
    u("percent", "minimal allowable accessibility"),
    f("file path", "read from file"),
    i("nT", "set custom analyze interval where 'n' - interval number and 'T' - " +
            "one of available interval types(h - hours, m - minutes, s - seconds). Example: '3h' - 3 hours interval"),
    nw(null, "no waiting for new data if reading from input thread"),
    in(null, "read from input (default)"),
    h(null, "print help");

    private String argName;
    private String description;

    OptionsEnum(String argName, String description) {
        this.argName = argName;
        this.description = description;
    }

    public String getArgName() {
        return argName;
    }

    public String getDescription() {
        return description;
    }
}
