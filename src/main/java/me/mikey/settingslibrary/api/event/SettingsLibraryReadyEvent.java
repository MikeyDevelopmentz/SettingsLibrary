package me.mikey.settingslibrary.api.event;

import me.mikey.settingslibrary.api.SettingsLibraryAPI;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SettingsLibraryReadyEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final SettingsLibraryAPI api;

    public SettingsLibraryReadyEvent(SettingsLibraryAPI api) {
        this.api = api;
    }

    public SettingsLibraryAPI getAPI() {
        return api;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
