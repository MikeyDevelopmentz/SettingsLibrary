package me.mikey.settingslibrary.api.event;

import me.mikey.settingslibrary.api.SettingsProvider;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SettingsProviderRegisterEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final SettingsProvider provider;
    private boolean cancelled;
    private String cancelReason;

    public SettingsProviderRegisterEvent(SettingsProvider provider) {
        this.provider = provider;
        this.cancelled = false;
    }

    public SettingsProvider getProvider() {
        return provider;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String reason) {
        this.cancelReason = reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
