package me.mikey.settingslibrary.util;

import me.mikey.settingslibrary.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class MessageUtils {

    private MessageUtils() {
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(ConfigManager.colorize(message));
    }

    public static void send(CommandSender sender, String prefix, String message) {
        sender.sendMessage(ConfigManager.colorize(prefix + message));
    }

    public static void send(CommandSender sender, String... messages) {
        for (String message : messages) {
            send(sender, message);
        }
    }

    public static void broadcast(String message) {
        Bukkit.broadcastMessage(ConfigManager.colorize(message));
    }

    public static void broadcast(String message, String permission) {
        String colorized = ConfigManager.colorize(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(colorized);
            }
        }
    }

    public static void send(Collection<? extends Player> players, String message) {
        String colorized = ConfigManager.colorize(message);
        for (Player player : players) {
            player.sendMessage(colorized);
        }
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(ConfigManager.colorize(message)));
    }

    public static void sendTitle(Player player, String title, String subtitle,
                                 int fadeIn, int stay, int fadeOut) {
        player.sendTitle(
                ConfigManager.colorize(title),
                ConfigManager.colorize(subtitle),
                fadeIn,
                stay,
                fadeOut
        );
    }

    public static void logInfo(String message) {
        Bukkit.getLogger().info("[SettingsLibrary] " + message);
    }

    public static void logWarning(String message) {
        Bukkit.getLogger().warning("[SettingsLibrary] " + message);
    }

    public static void logError(String message) {
        Bukkit.getLogger().severe("[SettingsLibrary] " + message);
    }
}
