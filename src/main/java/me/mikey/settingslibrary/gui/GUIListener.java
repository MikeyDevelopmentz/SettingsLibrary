package me.mikey.settingslibrary.gui;

import me.mikey.settingslibrary.SettingsLibraryPlugin;
import me.mikey.settingslibrary.api.SettingsProvider;
import me.mikey.settingslibrary.config.ConfigManager;
import me.mikey.settingslibrary.gui.GUIManager.SettingsMenuHolder;
import me.mikey.settingslibrary.gui.GUIManager.SlotType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GUIListener implements Listener {

    private final SettingsLibraryPlugin plugin;
    private final GUIManager guiManager;
    private final ConfigManager configManager;

    public GUIListener(SettingsLibraryPlugin plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGUIManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof SettingsMenuHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        int slot = event.getSlot();
        SlotType slotType = holder.getSlotType(slot);

        ConfigManager.SoundConfig clickSound = configManager.getClickSound();
        if (clickSound.enabled() && clickSound.sound() != null) {
            player.playSound(player.getLocation(), clickSound.sound(), clickSound.volume(), clickSound.pitch());
        }

        switch (slotType) {
            case PLUGIN -> handlePluginClick(event, holder, slot, player);
            case CLOSE -> player.closeInventory();
            case INFO -> handleInfoClick(event, holder, player);
            case SETTING -> handleSettingClick(event, holder, slot, player);
            case NAVIGATION -> handleNavigationClick(event, holder, slot, player);
            case BORDER, FILLER, SEPARATOR, ACCENT, EMPTY, UNKNOWN -> {}
        }
    }

    private void handlePluginClick(InventoryClickEvent event, SettingsMenuHolder holder, int slot, Player player) {
        SettingsProvider provider = holder.getSlotProvider(slot);
        if (provider == null) {
            return;
        }

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("Player " + player.getName() + " clicked plugin: " + provider.getSettingsId());
        }

        if (provider.hasSubMenu()) {
            guiManager.openSubMenu(player, provider);
        } else {
            provider.onClick(player, event.getClick());
        }
    }

    private void handleInfoClick(InventoryClickEvent event, SettingsMenuHolder holder, Player player) {
    }

    private void handleSettingClick(InventoryClickEvent event, SettingsMenuHolder holder, int slot, Player player) {
    }

    private void handleNavigationClick(InventoryClickEvent event, SettingsMenuHolder holder, int slot, Player player) {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getInventory().getHolder() instanceof SettingsMenuHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (event.getInventory().getHolder() instanceof SettingsMenuHolder) {
            guiManager.closeMenu(player);
        }
    }
}
