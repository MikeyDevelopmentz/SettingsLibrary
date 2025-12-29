package me.mikey.settingslibrary.commands;

import me.mikey.settingslibrary.SettingsLibraryPlugin;
import me.mikey.settingslibrary.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsCommand implements CommandExecutor, TabCompleter {

    private final SettingsLibraryPlugin plugin;
    private final ConfigManager configManager;

    public SettingsCommand(SettingsLibraryPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager
                    .colorize(configManager.getMessagePrefix() + "This command can only be used by players!"));
            return true;
        }

        if (!player.hasPermission("settingslibrary.use")) {
            player.sendMessage(configManager.getMessagePrefix() + configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length > 0) {
            String settingsId = args[0].toLowerCase();

            if (plugin.getIntegrationManager().isRegistered(settingsId)) {
                plugin.getAPI().openPluginSettings(player, settingsId);
                return true;
            }
        }

        plugin.getGUIManager().openMainMenu(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(plugin.getIntegrationManager().getProviderIds());
            String prefix = args[0].toLowerCase();
            suggestions.removeIf(s -> !s.toLowerCase().startsWith(prefix));
            Collections.sort(suggestions);
            return suggestions;
        }
        return Collections.emptyList();
    }
}
