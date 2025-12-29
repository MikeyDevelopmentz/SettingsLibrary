package me.mikey.settingslibrary;

import me.mikey.settingslibrary.api.SettingsLibraryAPI;
import me.mikey.settingslibrary.api.SettingsLibraryAPIImpl;
import me.mikey.settingslibrary.commands.SettingsCommand;
import me.mikey.settingslibrary.commands.SettingsLibraryCommand;
import me.mikey.settingslibrary.config.ConfigManager;
import me.mikey.settingslibrary.gui.GUIListener;
import me.mikey.settingslibrary.gui.GUIManager;
import me.mikey.settingslibrary.listeners.PluginListener;
import me.mikey.settingslibrary.manager.PluginIntegrationManager;
import me.mikey.settingslibrary.manager.TextColorManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class SettingsLibraryPlugin extends JavaPlugin {

    private static SettingsLibraryPlugin instance;

    private ConfigManager configManager;
    private GUIManager guiManager;
    private PluginIntegrationManager integrationManager;
    private TextColorManager textColorManager;

    private PluginListener pluginListener;
    private GUIListener guiListener;

    private SettingsLibraryAPI api;

    @Override
    public void onEnable() {
        instance = this;

        long startTime = System.currentTimeMillis();

        getLogger().info("╔═══════════════════════════════════════════════════╗");
        getLogger().info("║         SettingsLibrary - Loading...              ║");
        getLogger().info("╚═══════════════════════════════════════════════════╝");

        initializeManagers();
        loadConfiguration();
        initializeGUI();
        registerAPI();
        registerCommands();
        registerListeners();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            pluginListener.onSettingsLibraryReady();
        }, 1L);

        long loadTime = System.currentTimeMillis() - startTime;

        getLogger().info("╔═══════════════════════════════════════════════════╗");
        getLogger().info("║         SettingsLibrary - Enabled!                ║");
        getLogger().info("║         Version: " + padRight(getDescription().getVersion(), 18) + "       ║");
        getLogger().info("║         Load time: " + padRight(loadTime + "ms", 16) + "       ║");
        getLogger().info("╚═══════════════════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        getLogger().info("SettingsLibrary - Disabling...");

        if (api != null) {
            Bukkit.getServicesManager().unregister(SettingsLibraryAPI.class, api);
        }

        if (guiManager != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (guiManager.hasMenuOpen(player)) {
                    player.closeInventory();
                }
            });
        }

        getLogger().info("SettingsLibrary - Disabled successfully!");
        instance = null;
    }

    private void initializeManagers() {
        configManager = new ConfigManager(this);
        textColorManager = new TextColorManager();
        integrationManager = new PluginIntegrationManager(this);
        guiManager = new GUIManager(this);
    }

    private void loadConfiguration() {
        configManager.loadAll();
    }

    private void initializeGUI() {
        guiManager.initialize();
    }

    private void registerAPI() {
        api = new SettingsLibraryAPIImpl(this);
        Bukkit.getServicesManager().register(
                SettingsLibraryAPI.class,
                api,
                this,
                ServicePriority.Normal);
        getLogger().info("API registered with ServicesManager");
    }

    private void registerCommands() {
        PluginCommand settingsCmd = getCommand("settings");
        if (settingsCmd != null) {
            SettingsCommand settingsCommand = new SettingsCommand(this);
            settingsCmd.setExecutor(settingsCommand);
            settingsCmd.setTabCompleter(settingsCommand);
        }

        PluginCommand settingsLibCmd = getCommand("settingslibrary");
        if (settingsLibCmd != null) {
            SettingsLibraryCommand settingsLibraryCommand = new SettingsLibraryCommand(this);
            settingsLibCmd.setExecutor(settingsLibraryCommand);
            settingsLibCmd.setTabCompleter(settingsLibraryCommand);
        }
    }

    private void registerListeners() {
        pluginListener = new PluginListener(this);
        guiListener = new GUIListener(this);

        Bukkit.getPluginManager().registerEvents(pluginListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
    }

    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    public static SettingsLibraryPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public PluginIntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public TextColorManager getTextColorManager() {
        return textColorManager;
    }

    public SettingsLibraryAPI getAPI() {
        return api;
    }
}
