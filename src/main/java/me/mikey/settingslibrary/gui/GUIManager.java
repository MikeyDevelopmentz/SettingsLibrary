package me.mikey.settingslibrary.gui;

import me.mikey.settingslibrary.SettingsLibraryPlugin;
import me.mikey.settingslibrary.api.SettingsProvider;
import me.mikey.settingslibrary.config.ConfigManager;
import me.mikey.settingslibrary.config.ConfigManager.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GUIManager {

    private final SettingsLibraryPlugin plugin;
    private final ConfigManager configManager;

    private final Map<UUID, SettingsMenuHolder> openMenus;
    private final Map<String, ItemStack> decoratorCache;

    public GUIManager(SettingsLibraryPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.openMenus = new ConcurrentHashMap<>();
        this.decoratorCache = new HashMap<>();
    }

    public void initialize() {
        rebuildDecoratorCache();
    }

    public void rebuildDecoratorCache() {
        decoratorCache.clear();

        GUITemplate template = configManager.getActiveTemplate();
        Map<String, DecoratorItem> baseDecorators = configManager.getDecorators();
        Map<String, DecoratorItem> templateOverrides = configManager.getTemplateDecorators(template.id());

        Map<String, DecoratorItem> merged = new HashMap<>(baseDecorators);
        merged.putAll(templateOverrides);

        for (Map.Entry<String, DecoratorItem> entry : merged.entrySet()) {
            decoratorCache.put(entry.getKey(), createDecoratorItem(entry.getValue()));
        }
    }

    private ItemStack createDecoratorItem(DecoratorItem decorator) {
        ItemStack item = new ItemStack(decorator.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(decorator.name());
            if (decorator.customModelData() > 0) {
                meta.setCustomModelData(decorator.customModelData());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openMainMenu(Player player) {
        closeMenu(player);

        GUITemplate template = configManager.getActiveTemplate();
        int size = template.size();
        String title = configManager.getMenuTitle();

        title = title.replace("{version}", plugin.getDescription().getVersion())
                .replace("{plugin_count}", String.valueOf(plugin.getIntegrationManager().getProviderCount()));

        SettingsMenuHolder holder = new SettingsMenuHolder(plugin, MenuType.MAIN, null);
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);

        fillInventoryWithTemplate(inventory, template, holder);
        placePluginItems(inventory, template, holder, player);
        placeNavigationItems(inventory, holder);

        openMenus.put(player.getUniqueId(), holder);

        for (SettingsProvider provider : plugin.getIntegrationManager().getProviders()) {
            provider.onMenuOpen(player);
        }

        SoundConfig openSound = configManager.getOpenSound();
        if (openSound.enabled() && openSound.sound() != null) {
            player.playSound(player.getLocation(), openSound.sound(), openSound.volume(), openSound.pitch());
        }

        player.openInventory(inventory);
    }

    public void openSubMenu(Player player, SettingsProvider provider) {
        closeMenu(player);
        provider.onClick(player, null);
    }

    private void fillInventoryWithTemplate(Inventory inventory, GUITemplate template, SettingsMenuHolder holder) {
        Map<Integer, Character> pattern = template.parsePattern();

        for (Map.Entry<Integer, Character> entry : pattern.entrySet()) {
            int slot = entry.getKey();
            char patternChar = entry.getValue();

            if (slot >= inventory.getSize()) continue;

            ItemStack item = getItemForPattern(patternChar);
            if (item != null) {
                inventory.setItem(slot, item);
                holder.setSlotType(slot, getSlotType(patternChar));
            }
        }
    }

    private ItemStack getItemForPattern(char patternChar) {
        return switch (patternChar) {
            case 'B' -> decoratorCache.get("border");
            case 'F' -> decoratorCache.get("filler");
            case 'S' -> decoratorCache.get("separator");
            case 'A' -> decoratorCache.get("accent");
            case 'X' -> null;
            default -> null;
        };
    }

    private SlotType getSlotType(char patternChar) {
        return switch (patternChar) {
            case 'B' -> SlotType.BORDER;
            case 'F' -> SlotType.FILLER;
            case 'S' -> SlotType.SEPARATOR;
            case 'A' -> SlotType.ACCENT;
            case 'P' -> SlotType.PLUGIN;
            case 'N' -> SlotType.NAVIGATION;
            case 'C' -> SlotType.CLOSE;
            case 'I' -> SlotType.INFO;
            case 'X' -> SlotType.EMPTY;
            default -> SlotType.UNKNOWN;
        };
    }

    private void placePluginItems(Inventory inventory, GUITemplate template, SettingsMenuHolder holder, Player player) {
        Collection<SettingsProvider> providers = plugin.getIntegrationManager().getProviders();
        List<Integer> pluginSlots = template.getPluginSlots();

        List<SettingsProvider> sortedProviders = new ArrayList<>(providers);
        sortedProviders.sort(Comparator.comparingInt(p -> {
            int configPriority = configManager.getPluginPriority(p.getSettingsId());
            return Math.min(p.getPriority(), configPriority);
        }));

        sortedProviders.removeIf(p -> !p.isEnabled() || !configManager.isPluginEnabled(p.getSettingsId()));

        int pluginIndex = 0;
        for (SettingsProvider provider : sortedProviders) {
            int configuredSlot = configManager.getPluginSlot(provider.getSettingsId());
            int targetSlot;

            if (configuredSlot >= 0 && configuredSlot < inventory.getSize()) {
                targetSlot = configuredSlot;
            } else if (pluginIndex < pluginSlots.size()) {
                targetSlot = pluginSlots.get(pluginIndex);
                pluginIndex++;
            } else {
                plugin.getLogger().warning("No slot available for plugin: " + provider.getSettingsId());
                continue;
            }

            ItemStack displayItem = provider.getDisplayItem();
            if (displayItem != null) {
                inventory.setItem(targetSlot, displayItem);
                holder.setSlotType(targetSlot, SlotType.PLUGIN);
                holder.setSlotProvider(targetSlot, provider);
            }
        }
    }

    private void placeNavigationItems(Inventory inventory, SettingsMenuHolder holder) {
        NavigationItem closeItem = configManager.getNavigationItem("close");
        if (closeItem != null && closeItem.enabled()) {
            ItemStack item = createNavigationItem(closeItem);
            inventory.setItem(closeItem.slot(), item);
            holder.setSlotType(closeItem.slot(), SlotType.CLOSE);
        }

        NavigationItem infoItem = configManager.getNavigationItem("info");
        if (infoItem != null && infoItem.enabled()) {
            ItemStack item = createNavigationItem(infoItem);

            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasLore()) {
                List<String> lore = meta.getLore();
                List<String> newLore = new ArrayList<>();
                for (String line : lore) {
                    line = line.replace("{version}", plugin.getDescription().getVersion())
                            .replace("{plugin_count}", String.valueOf(plugin.getIntegrationManager().getProviderCount()));
                    newLore.add(line);
                }
                meta.setLore(newLore);
                item.setItemMeta(meta);
            }

            inventory.setItem(infoItem.slot(), item);
            holder.setSlotType(infoItem.slot(), SlotType.INFO);
        }
    }

    private ItemStack createNavigationItem(NavigationItem navItem) {
        ItemStack item = new ItemStack(navItem.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(navItem.name());
            meta.setLore(navItem.lore());
            item.setItemMeta(meta);
        }
        return item;
    }

    public void closeMenu(Player player) {
        SettingsMenuHolder holder = openMenus.remove(player.getUniqueId());
        if (holder != null) {
            for (SettingsProvider provider : plugin.getIntegrationManager().getProviders()) {
                provider.onMenuClose(player);
            }
        }
    }

    public boolean hasMenuOpen(Player player) {
        return openMenus.containsKey(player.getUniqueId());
    }

    public SettingsMenuHolder getMenuHolder(Player player) {
        return openMenus.get(player.getUniqueId());
    }

    public void refreshAllMenus() {
        for (Map.Entry<UUID, SettingsMenuHolder> entry : openMenus.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                openMainMenu(player);
            }
        }
    }

    public int getOpenMenuCount() {
        return openMenus.size();
    }

    public enum MenuType {
        MAIN,
        SUB_MENU,
        PLUGIN_SETTINGS
    }

    public enum SlotType {
        EMPTY,
        BORDER,
        FILLER,
        SEPARATOR,
        ACCENT,
        PLUGIN,
        NAVIGATION,
        CLOSE,
        INFO,
        SETTING,
        UNKNOWN
    }

    public static class SettingsMenuHolder implements InventoryHolder {

        private final SettingsLibraryPlugin plugin;
        private final MenuType menuType;
        private final SettingsProvider subMenuProvider;
        private Inventory inventory;

        private final Map<Integer, SlotType> slotTypes;
        private final Map<Integer, SettingsProvider> slotProviders;

        public SettingsMenuHolder(SettingsLibraryPlugin plugin, MenuType menuType, SettingsProvider subMenuProvider) {
            this.plugin = plugin;
            this.menuType = menuType;
            this.subMenuProvider = subMenuProvider;
            this.slotTypes = new HashMap<>();
            this.slotProviders = new HashMap<>();
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        public SettingsLibraryPlugin getPlugin() {
            return plugin;
        }

        public MenuType getMenuType() {
            return menuType;
        }

        public SettingsProvider getSubMenuProvider() {
            return subMenuProvider;
        }

        public SlotType getSlotType(int slot) {
            return slotTypes.getOrDefault(slot, SlotType.UNKNOWN);
        }

        public void setSlotType(int slot, SlotType type) {
            slotTypes.put(slot, type);
        }

        public SettingsProvider getSlotProvider(int slot) {
            return slotProviders.get(slot);
        }

        public void setSlotProvider(int slot, SettingsProvider provider) {
            slotProviders.put(slot, provider);
        }
    }
}
