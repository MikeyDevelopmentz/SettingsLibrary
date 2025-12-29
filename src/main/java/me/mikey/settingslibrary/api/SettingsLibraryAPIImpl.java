package me.mikey.settingslibrary.api;

import me.mikey.settingslibrary.SettingsLibraryPlugin;
import me.mikey.settingslibrary.manager.PluginIntegrationManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Optional;

public class SettingsLibraryAPIImpl implements SettingsLibraryAPI {

    private final SettingsLibraryPlugin plugin;
    private final PluginIntegrationManager integrationManager;

    public SettingsLibraryAPIImpl(SettingsLibraryPlugin plugin) {
        this.plugin = plugin;
        this.integrationManager = plugin.getIntegrationManager();
    }

    @Override
    public boolean registerProvider(SettingsProvider provider) {
        return integrationManager.registerProvider(provider);
    }

    @Override
    public boolean unregisterProvider(SettingsProvider provider) {
        return integrationManager.unregisterProvider(provider);
    }

    @Override
    public int unregisterAll(Plugin ownerPlugin) {
        return integrationManager.unregisterAll(ownerPlugin);
    }

    @Override
    public Optional<SettingsProvider> getProvider(String settingsId) {
        return integrationManager.getProvider(settingsId);
    }

    @Override
    public Collection<SettingsProvider> getProviders() {
        return integrationManager.getProviders();
    }

    @Override
    public int getProviderCount() {
        return integrationManager.getProviderCount();
    }

    @Override
    public void openSettingsMenu(Player player) {
        plugin.getGUIManager().openMainMenu(player);
    }

    @Override
    public boolean openPluginSettings(Player player, String settingsId) {
        Optional<SettingsProvider> provider = integrationManager.getProvider(settingsId);
        if (provider.isPresent()) {
            plugin.getGUIManager().openSubMenu(player, provider.get());
            return true;
        }
        return false;
    }

    @Override
    public void refreshMenus() {
        plugin.getGUIManager().refreshAllMenus();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
}
