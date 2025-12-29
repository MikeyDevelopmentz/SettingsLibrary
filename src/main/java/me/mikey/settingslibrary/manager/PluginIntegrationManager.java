package me.mikey.settingslibrary.manager;

import me.mikey.settingslibrary.SettingsLibraryPlugin;
import me.mikey.settingslibrary.api.SettingsProvider;
import me.mikey.settingslibrary.config.ConfigManager;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PluginIntegrationManager {

    private final SettingsLibraryPlugin plugin;
    private final ConfigManager configManager;

    private final Map<String, SettingsProvider> providers;
    private final Map<String, Set<String>> pluginProviders;

    public PluginIntegrationManager(SettingsLibraryPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.providers = new ConcurrentHashMap<>();
        this.pluginProviders = new ConcurrentHashMap<>();
    }

    public boolean registerProvider(SettingsProvider provider) {
        if (provider == null) {
            plugin.getLogger().warning("Attempted to register null provider");
            return false;
        }

        String settingsId = provider.getSettingsId();
        if (settingsId == null || settingsId.isEmpty()) {
            plugin.getLogger().warning("Provider has null or empty settings ID");
            return false;
        }

        settingsId = settingsId.toLowerCase().replace(" ", "-");

        if (providers.containsKey(settingsId)) {
            plugin.getLogger().warning("Provider with ID '" + settingsId + "' is already registered");
            return false;
        }

        if (provider.getPlugin() == null) {
            plugin.getLogger().warning("Provider '" + settingsId + "' has no associated plugin");
            return false;
        }

        if (provider.getDisplayItem() == null) {
            plugin.getLogger().warning("Provider '" + settingsId + "' has no display item");
            return false;
        }

        providers.put(settingsId, provider);

        String pluginName = provider.getPlugin().getName();
        pluginProviders.computeIfAbsent(pluginName, k -> new HashSet<>()).add(settingsId);

        configManager.addPluginIfAbsent(settingsId);

        plugin.getLogger().info("Registered settings provider: " + settingsId + " from " + pluginName);

        if (plugin.getGUIManager() != null) {
            plugin.getGUIManager().refreshAllMenus();
        }

        return true;
    }

    public boolean unregisterProvider(SettingsProvider provider) {
        if (provider == null) {
            return false;
        }

        String settingsId = provider.getSettingsId().toLowerCase().replace(" ", "-");
        return unregisterProvider(settingsId);
    }

    public boolean unregisterProvider(String settingsId) {
        settingsId = settingsId.toLowerCase().replace(" ", "-");

        SettingsProvider removed = providers.remove(settingsId);
        if (removed != null) {
            String pluginName = removed.getPlugin().getName();
            Set<String> pluginIds = pluginProviders.get(pluginName);
            if (pluginIds != null) {
                pluginIds.remove(settingsId);
                if (pluginIds.isEmpty()) {
                    pluginProviders.remove(pluginName);
                }
            }

            plugin.getLogger().info("Unregistered settings provider: " + settingsId);

            if (plugin.getGUIManager() != null) {
                plugin.getGUIManager().refreshAllMenus();
            }

            return true;
        }

        return false;
    }

    public int unregisterAll(Plugin ownerPlugin) {
        if (ownerPlugin == null) {
            return 0;
        }

        String pluginName = ownerPlugin.getName();
        Set<String> ids = pluginProviders.remove(pluginName);

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String id : ids) {
            if (providers.remove(id) != null) {
                count++;
            }
        }

        if (count > 0) {
            plugin.getLogger().info("Unregistered " + count + " provider(s) from " + pluginName);

            if (plugin.getGUIManager() != null) {
                plugin.getGUIManager().refreshAllMenus();
            }
        }

        return count;
    }

    public Optional<SettingsProvider> getProvider(String settingsId) {
        settingsId = settingsId.toLowerCase().replace(" ", "-");
        return Optional.ofNullable(providers.get(settingsId));
    }

    public Collection<SettingsProvider> getProviders() {
        return Collections.unmodifiableCollection(providers.values());
    }

    public Set<String> getProviderIds() {
        return Collections.unmodifiableSet(providers.keySet());
    }

    public int getProviderCount() {
        return providers.size();
    }

    public boolean isRegistered(String settingsId) {
        settingsId = settingsId.toLowerCase().replace(" ", "-");
        return providers.containsKey(settingsId);
    }

    public List<SettingsProvider> getProvidersSortedByPriority() {
        List<SettingsProvider> sorted = new ArrayList<>(providers.values());
        sorted.sort(Comparator.comparingInt(p -> {
            int configPriority = configManager.getPluginPriority(p.getSettingsId());
            return Math.min(p.getPriority(), configPriority);
        }));
        return sorted;
    }

    public Map<String, Integer> getPluginProviderCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : pluginProviders.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
        return counts;
    }

    public Set<String> getProviderIdsForPlugin(String pluginName) {
        Set<String> ids = pluginProviders.get(pluginName);
        return ids != null ? Collections.unmodifiableSet(ids) : Collections.emptySet();
    }
}
