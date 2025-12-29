package me.mikey.settingslibrary.api;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

public interface SettingsProvider {

    Plugin getPlugin();

    String getSettingsId();

    default String getDisplayName() {
        return getPlugin().getName();
    }

    ItemStack getDisplayItem();

    void onClick(Player player, ClickType clickType);

    default List<SettingEntry> getSettings() {
        return null;
    }

    default boolean hasSubMenu() {
        List<SettingEntry> settings = getSettings();
        return settings != null && !settings.isEmpty();
    }

    default int getPriority() {
        return 100;
    }

    default boolean isEnabled() {
        return true;
    }

    default void onMenuOpen(Player player) {
    }

    default void onMenuClose(Player player) {
    }
}
