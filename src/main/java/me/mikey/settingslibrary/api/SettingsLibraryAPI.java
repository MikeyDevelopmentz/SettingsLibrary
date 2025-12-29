package me.mikey.settingslibrary.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

import java.util.Collection;
import java.util.Optional;

public interface SettingsLibraryAPI {

    static SettingsLibraryAPI get() {
        ServicesManager servicesManager = Bukkit.getServicesManager();
        RegisteredServiceProvider<SettingsLibraryAPI> provider =
                servicesManager.getRegistration(SettingsLibraryAPI.class);
        return provider != null ? provider.getProvider() : null;
    }

    static boolean isAvailable() {
        return get() != null;
    }

    static boolean ifAvailable(java.util.function.Consumer<SettingsLibraryAPI> action) {
        SettingsLibraryAPI api = get();
        if (api != null) {
            action.accept(api);
            return true;
        }
        return false;
    }

    boolean registerProvider(SettingsProvider provider);

    boolean unregisterProvider(SettingsProvider provider);

    int unregisterAll(Plugin plugin);

    Optional<SettingsProvider> getProvider(String settingsId);

    Collection<SettingsProvider> getProviders();

    int getProviderCount();

    void openSettingsMenu(Player player);

    boolean openPluginSettings(Player player, String settingsId);

    void refreshMenus();

    String getVersion();
}
