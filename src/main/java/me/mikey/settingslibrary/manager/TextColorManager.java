package me.mikey.settingslibrary.manager;

import net.md_5.bungee.api.ChatColor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextColorManager {

    private final List<Function<String, String>> formatters;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public TextColorManager() {
        this.formatters = new ArrayList<>();

        // Register default formatters
        // 1. Hex Color Formatter
        registerFormatter(this::formatHex);

        // 2. Legacy Color Formatter (Standard Bukkit/Spigot)
        registerFormatter(text -> ChatColor.translateAlternateColorCodes('&', text));
    }

    public void registerFormatter(Function<String, String> formatter) {
        if (formatter != null) {
            this.formatters.add(formatter);
        }
    }

    public String process(String text) {
        if (text == null)
            return "";

        String result = text;
        for (Function<String, String> formatter : formatters) {
            result = formatter.apply(result);
        }
        return result;
    }

    private String formatHex(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }

        return matcher.appendTail(buffer).toString();
    }
}
