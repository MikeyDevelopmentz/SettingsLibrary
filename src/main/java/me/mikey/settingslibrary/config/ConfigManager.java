package me.mikey.settingslibrary.config;

import me.mikey.settingslibrary.SettingsLibraryPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ConfigManager {

    private final SettingsLibraryPlugin plugin;

    private FileConfiguration mainConfig;
    private FileConfiguration templatesConfig;
    private FileConfiguration positionsConfig;

    private File templatesFile;
    private File positionsFile;

    private String menuTitle;
    private int menuSize;
    private String activeTemplate;
    private boolean debugMode;
    private Map<String, DecoratorItem> decorators;
    private Map<String, NavigationItem> navigationItems;
    private SoundConfig openSound;
    private SoundConfig clickSound;

    public ConfigManager(SettingsLibraryPlugin plugin) {
        this.plugin = plugin;
        this.decorators = new HashMap<>();
        this.navigationItems = new HashMap<>();
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        saveDefaultConfig("templates.yml");
        saveDefaultConfig("positions.yml");

        plugin.reloadConfig();
        mainConfig = plugin.getConfig();

        templatesFile = new File(plugin.getDataFolder(), "templates.yml");
        templatesConfig = YamlConfiguration.loadConfiguration(templatesFile);

        positionsFile = new File(plugin.getDataFolder(), "positions.yml");
        positionsConfig = YamlConfiguration.loadConfiguration(positionsFile);

        parseMainConfig();

        plugin.getLogger().info("Configuration loaded successfully!");
    }

    public void reloadAll() {
        loadAll();
    }

    private void saveDefaultConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
    }

    private void parseMainConfig() {
        debugMode = mainConfig.getBoolean("general.debug", false);

        menuTitle = colorize(mainConfig.getString("gui.title", "&8&l✦ &b&lSettings &8&l✦"));
        menuSize = mainConfig.getInt("gui.size", 54);

        if (menuSize % 9 != 0 || menuSize < 9 || menuSize > 54) {
            plugin.getLogger().warning("Invalid GUI size: " + menuSize + ". Defaulting to 54.");
            menuSize = 54;
        }

        activeTemplate = mainConfig.getString("template.active", "default");

        openSound = parseSoundConfig(mainConfig.getConfigurationSection("gui.open-sound"));
        clickSound = parseSoundConfig(mainConfig.getConfigurationSection("gui.click-sound"));

        parseDecorators();
        parseNavigationItems();
    }

    private void parseDecorators() {
        decorators.clear();
        ConfigurationSection section = mainConfig.getConfigurationSection("decorators");
        if (section == null)
            return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection != null) {
                decorators.put(key, parseDecoratorItem(itemSection));
            }
        }
    }

    private void parseNavigationItems() {
        navigationItems.clear();
        ConfigurationSection section = mainConfig.getConfigurationSection("navigation");
        if (section == null)
            return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection != null) {
                navigationItems.put(key, parseNavigationItem(itemSection));
            }
        }
    }

    private DecoratorItem parseDecoratorItem(ConfigurationSection section) {
        Material material = Material.matchMaterial(section.getString("material", "GRAY_STAINED_GLASS_PANE"));
        if (material == null)
            material = Material.GRAY_STAINED_GLASS_PANE;

        String name = colorize(section.getString("name", " "));
        int customModelData = section.getInt("custom-model-data", -1);

        return new DecoratorItem(material, name, customModelData);
    }

    private NavigationItem parseNavigationItem(ConfigurationSection section) {
        boolean enabled = section.getBoolean("enabled", true);
        int slot = section.getInt("slot", 0);

        Material material = Material.matchMaterial(section.getString("material", "BARRIER"));
        if (material == null)
            material = Material.BARRIER;

        String name = colorize(section.getString("name", ""));
        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) {
            lore.add(colorize(line));
        }

        return new NavigationItem(enabled, slot, material, name, lore);
    }

    private SoundConfig parseSoundConfig(ConfigurationSection section) {
        if (section == null)
            return new SoundConfig(false, null, 1.0f, 1.0f);

        boolean enabled = section.getBoolean("enabled", true);
        Sound sound = null;
        try {
            sound = Sound.valueOf(section.getString("sound", "UI_BUTTON_CLICK"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + section.getString("sound"));
        }
        float volume = (float) section.getDouble("volume", 1.0);
        float pitch = (float) section.getDouble("pitch", 1.0);

        return new SoundConfig(enabled, sound, volume, pitch);
    }

    public int getPluginSlot(String settingsId) {
        return positionsConfig.getInt("plugins." + settingsId + ".slot", -1);
    }

    public int getPluginPriority(String settingsId) {
        return positionsConfig.getInt("plugins." + settingsId + ".priority", 100);
    }

    public boolean isPluginEnabled(String settingsId) {
        return positionsConfig.getBoolean("plugins." + settingsId + ".enabled", true);
    }

    public void setPluginPosition(String settingsId, int slot, int priority) {
        String path = "plugins." + settingsId;
        positionsConfig.set(path + ".slot", slot);
        positionsConfig.set(path + ".priority", priority);
        positionsConfig.set(path + ".page", 1);
        positionsConfig.set(path + ".enabled", true);
        savePositionsConfig();
    }

    public boolean addPluginIfAbsent(String settingsId) {
        String path = "plugins." + settingsId;
        if (positionsConfig.contains(path)) {
            return false;
        }

        int basePriority = positionsConfig.getInt("auto-assignment.default-priority", 100);
        int increment = positionsConfig.getInt("auto-assignment.priority-increment", 10);

        ConfigurationSection plugins = positionsConfig.getConfigurationSection("plugins");
        int maxPriority = basePriority;
        if (plugins != null) {
            for (String key : plugins.getKeys(false)) {
                int priority = plugins.getInt(key + ".priority", basePriority);
                if (priority >= maxPriority) {
                    maxPriority = priority + increment;
                }
            }
        }

        positionsConfig.set(path + ".slot", -1);
        positionsConfig.set(path + ".priority", maxPriority);
        positionsConfig.set(path + ".page", 1);
        positionsConfig.set(path + ".enabled", true);
        savePositionsConfig();

        return true;
    }

    public void savePositionsConfig() {
        try {
            positionsConfig.save(positionsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save positions.yml", e);
        }
    }

    public GUITemplate getTemplate(String name) {
        ConfigurationSection section = templatesConfig.getConfigurationSection("templates." + name);
        if (section == null)
            return null;

        String displayName = section.getString("name", name);
        String description = section.getString("description", "");
        int size = section.getInt("size", menuSize);
        List<String> pattern = section.getStringList("pattern");

        return new GUITemplate(name, displayName, description, size, pattern);
    }

    public GUITemplate getActiveTemplate() {
        GUITemplate template = getTemplate(activeTemplate);
        if (template == null) {
            template = getTemplate("default");
        }
        if (template == null) {
            template = createFallbackTemplate();
        }
        return template;
    }

    private GUITemplate createFallbackTemplate() {
        List<String> pattern = Arrays.asList(
                "B B B B I B B B B",
                "B P P P P P P P B",
                "B P P P P P P P B",
                "B P P P P P P P B",
                "B P P P P P P P B",
                "B B B B C B B B B");
        return new GUITemplate("fallback", "Fallback", "Default fallback template", 54, pattern);
    }

    public Map<String, DecoratorItem> getTemplateDecorators(String templateName) {
        Map<String, DecoratorItem> overrides = new HashMap<>();
        ConfigurationSection section = templatesConfig.getConfigurationSection("template-decorators." + templateName);
        if (section == null)
            return overrides;

        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection != null) {
                overrides.put(key, parseDecoratorItem(itemSection));
            }
        }
        return overrides;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public int getMenuSize() {
        return menuSize;
    }

    public String getActiveTemplateName() {
        return activeTemplate;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public DecoratorItem getDecorator(String key) {
        return decorators.get(key);
    }

    public Map<String, DecoratorItem> getDecorators() {
        return Collections.unmodifiableMap(decorators);
    }

    public NavigationItem getNavigationItem(String key) {
        return navigationItems.get(key);
    }

    public Map<String, NavigationItem> getNavigationItems() {
        return Collections.unmodifiableMap(navigationItems);
    }

    public SoundConfig getOpenSound() {
        return openSound;
    }

    public SoundConfig getClickSound() {
        return clickSound;
    }

    public boolean isAutoArrangeEnabled() {
        return mainConfig.getBoolean("template.auto-arrange", true);
    }

    public int getAutoArrangeStart() {
        return mainConfig.getInt("template.auto-arrange-start", 10);
    }

    public List<Integer> getAutoArrangeSkipSlots() {
        return mainConfig.getIntegerList("template.auto-arrange-skip");
    }

    public String getMessage(String key) {
        String message = mainConfig.getString("messages." + key, "");
        return colorize(message);
    }

    public String getMessagePrefix() {
        return getMessage("prefix");
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getTemplatesConfig() {
        return templatesConfig;
    }

    public FileConfiguration getPositionsConfig() {
        return positionsConfig;
    }

    public static String colorize(String text) {
        if (text == null)
            return "";
        if (SettingsLibraryPlugin.getInstance() != null) {
            return SettingsLibraryPlugin.getInstance().getTextColorManager().process(text);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public record DecoratorItem(Material material, String name, int customModelData) {
    }

    public record NavigationItem(boolean enabled, int slot, Material material, String name, List<String> lore) {
    }

    public record SoundConfig(boolean enabled, Sound sound, float volume, float pitch) {
    }

    public record GUITemplate(String id, String displayName, String description, int size, List<String> pattern) {

        public Map<Integer, Character> parsePattern() {
            Map<Integer, Character> slots = new HashMap<>();
            int slot = 0;

            for (String row : pattern) {
                String[] chars = row.trim().split("\\s+");
                for (String c : chars) {
                    if (!c.isEmpty()) {
                        slots.put(slot, c.charAt(0));
                        slot++;
                    }
                }
            }

            return slots;
        }

        public List<Integer> getSlotsForPattern(char patternChar) {
            List<Integer> result = new ArrayList<>();
            Map<Integer, Character> parsed = parsePattern();

            for (Map.Entry<Integer, Character> entry : parsed.entrySet()) {
                if (entry.getValue() == patternChar) {
                    result.add(entry.getKey());
                }
            }

            return result;
        }

        public List<Integer> getPluginSlots() {
            return getSlotsForPattern('P');
        }
    }
}
