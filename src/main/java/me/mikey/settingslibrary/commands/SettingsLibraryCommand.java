package me.mikey.settingslibrary.commands;

import me.mikey.settingslibrary.SettingsLibraryPlugin;
import me.mikey.settingslibrary.api.SettingsProvider;
import me.mikey.settingslibrary.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class SettingsLibraryCommand implements CommandExecutor, TabCompleter {

    private final SettingsLibraryPlugin plugin;
    private final ConfigManager configManager;

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "list", "reload", "info", "debug", "refresh", "help");

    public SettingsLibraryCommand(SettingsLibraryPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = configManager.getMessagePrefix();

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            case "info" -> handleInfo(sender, args);
            case "debug" -> handleDebug(sender);
            case "refresh" -> handleRefresh(sender);
            case "help" -> sendHelp(sender, label);
            default -> sender.sendMessage(
                    ConfigManager.colorize(prefix + "&cUnknown subcommand. Use &f/" + label + " help &cfor usage."));
        }

        return true;
    }

    private void handleList(CommandSender sender) {
        String prefix = configManager.getMessagePrefix();
        Collection<SettingsProvider> providers = plugin.getIntegrationManager().getProviders();

        if (providers.isEmpty()) {
            sender.sendMessage(prefix + configManager.getMessage("list-empty"));
            return;
        }

        sender.sendMessage(prefix + configManager.getMessage("list-header"));

        for (SettingsProvider provider : providers) {
            String version = provider.getPlugin().getDescription().getVersion();
            String entry = configManager.getMessage("list-entry")
                    .replace("{plugin}", provider.getDisplayName())
                    .replace("{version}", version);
            sender.sendMessage(ConfigManager.colorize(entry));
        }

        sender.sendMessage(
                ConfigManager.colorize(prefix + "&7Total: &f" + providers.size() + " &7integrated plugin(s)"));
    }

    private void handleReload(CommandSender sender) {
        String prefix = configManager.getMessagePrefix();

        if (!sender.hasPermission("settingslibrary.reload")) {
            sender.sendMessage(prefix + configManager.getMessage("no-permission"));
            return;
        }

        configManager.reloadAll();
        plugin.getGUIManager().rebuildDecoratorCache();
        plugin.getGUIManager().refreshAllMenus();

        sender.sendMessage(prefix + configManager.getMessage("reload-success"));
    }

    private void handleInfo(CommandSender sender, String[] args) {
        String prefix = configManager.getMessagePrefix();

        if (args.length < 2) {
            sender.sendMessage(ConfigManager.colorize(prefix + "&cUsage: /settingslibrary info <plugin-id>"));
            return;
        }

        String settingsId = args[1].toLowerCase();
        Optional<SettingsProvider> providerOpt = plugin.getIntegrationManager().getProvider(settingsId);

        if (providerOpt.isEmpty()) {
            sender.sendMessage(prefix + configManager.getMessage("plugin-not-found")
                    .replace("{plugin}", settingsId));
            return;
        }

        SettingsProvider provider = providerOpt.get();

        sender.sendMessage("");
        sender.sendMessage(ConfigManager.colorize("&b&l" + provider.getDisplayName() + " &7Settings Info"));
        sender.sendMessage(ConfigManager.colorize("&8&m                                        "));
        sender.sendMessage(ConfigManager.colorize("&7Plugin: &f" + provider.getPlugin().getName()));
        sender.sendMessage(
                ConfigManager.colorize("&7Version: &f" + provider.getPlugin().getDescription().getVersion()));
        sender.sendMessage(ConfigManager.colorize("&7Settings ID: &f" + provider.getSettingsId()));
        sender.sendMessage(ConfigManager.colorize("&7Priority: &f" + provider.getPriority()));
        sender.sendMessage(ConfigManager.colorize("&7Has Sub-Menu: &f" + (provider.hasSubMenu() ? "Yes" : "No")));
        sender.sendMessage(ConfigManager.colorize("&7Enabled: &f" + (provider.isEnabled() ? "Yes" : "No")));

        int configuredSlot = configManager.getPluginSlot(provider.getSettingsId());
        sender.sendMessage(ConfigManager.colorize("&7Configured Slot: &f" +
                (configuredSlot >= 0 ? configuredSlot : "Auto")));

        sender.sendMessage(ConfigManager.colorize("&8&m                                        "));
        sender.sendMessage("");
    }

    private void handleDebug(CommandSender sender) {
        String prefix = configManager.getMessagePrefix();

        if (!sender.hasPermission("settingslibrary.admin")) {
            sender.sendMessage(prefix + configManager.getMessage("no-permission"));
            return;
        }

        boolean current = configManager.isDebugMode();
        sender.sendMessage(
                ConfigManager.colorize(prefix + "&7Debug mode is currently: &f" + (current ? "enabled" : "disabled")));
    }

    private void handleRefresh(CommandSender sender) {
        String prefix = configManager.getMessagePrefix();

        if (!sender.hasPermission("settingslibrary.admin")) {
            sender.sendMessage(prefix + configManager.getMessage("no-permission"));
            return;
        }

        int count = plugin.getGUIManager().getOpenMenuCount();
        plugin.getGUIManager().refreshAllMenus();

        sender.sendMessage(ConfigManager.colorize(prefix + "&aRefreshed &f" + count + " &aopen menu(s)."));
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("");
        sender.sendMessage(
                ConfigManager.colorize("&b&lSettingsLibrary &8- &7v" + plugin.getDescription().getVersion()));
        sender.sendMessage(ConfigManager.colorize("&8&m                                        "));
        sender.sendMessage(ConfigManager.colorize("&f/" + label + " list &8- &7List integrated plugins"));
        sender.sendMessage(ConfigManager.colorize("&f/" + label + " info <plugin> &8- &7Show plugin info"));
        sender.sendMessage(ConfigManager.colorize("&f/" + label + " reload &8- &7Reload configuration"));
        sender.sendMessage(ConfigManager.colorize("&f/" + label + " refresh &8- &7Refresh open menus"));
        sender.sendMessage(ConfigManager.colorize("&f/" + label + " debug &8- &7Toggle debug mode"));
        sender.sendMessage(ConfigManager.colorize("&f/" + label + " help &8- &7Show this help"));
        sender.sendMessage(ConfigManager.colorize("&8&m                                        "));
        sender.sendMessage(ConfigManager.colorize("&f/settings &8- &7Open the settings menu"));
        sender.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> filtered = new ArrayList<>();
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(prefix)) {
                    filtered.add(sub);
                }
            }
            return filtered;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            List<String> suggestions = new ArrayList<>(plugin.getIntegrationManager().getProviderIds());
            String prefix = args[1].toLowerCase();
            suggestions.removeIf(s -> !s.toLowerCase().startsWith(prefix));
            Collections.sort(suggestions);
            return suggestions;
        }

        return Collections.emptyList();
    }
}
