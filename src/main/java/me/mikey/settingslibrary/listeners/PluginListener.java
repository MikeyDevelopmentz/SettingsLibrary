package me.mikey.settingslibrary.listeners;

import me.mikey.settingslibrary.SettingsLibraryPlugin;
import me.mikey.settingslibrary.api.event.SettingsLibraryReadyEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class PluginListener implements Listener {

    private final SettingsLibraryPlugin plugin;

    public PluginListener(SettingsLibraryPlugin plugin) {
        this.plugin = plugin;
    }

    public void onSettingsLibraryReady() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            SettingsLibraryReadyEvent readyEvent = new SettingsLibraryReadyEvent(plugin.getAPI());
            Bukkit.getPluginManager().callEvent(readyEvent);

            plugin.getLogger().info("SettingsLibrary is ready! Fired SettingsLibraryReadyEvent.");
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin enabledPlugin = event.getPlugin();

        if (enabledPlugin.equals(plugin)) {
            return;
        }

        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("Plugin enabled: " + enabledPlugin.getName() +
                    " - checking for SettingsLibrary integration");
        }

        if (dependsOnSettingsLibrary(enabledPlugin)) {
            plugin.getLogger().info("Plugin " + enabledPlugin.getName() +
                    " depends on SettingsLibrary - waiting for it to register");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin disabledPlugin = event.getPlugin();

        if (disabledPlugin.equals(plugin)) {
            return;
        }

        int unregistered = plugin.getIntegrationManager().unregisterAll(disabledPlugin);

        if (unregistered > 0 && plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("Auto-unregistered " + unregistered +
                    " provider(s) from disabled plugin: " + disabledPlugin.getName());
        }
    }

    private boolean dependsOnSettingsLibrary(Plugin checkPlugin) {
        java.util.List<String> depend = checkPlugin.getDescription().getDepend();
        java.util.List<String> softDepend = checkPlugin.getDescription().getSoftDepend();

        return depend.contains("SettingsLibrary") || softDepend.contains("SettingsLibrary");
    }
}
