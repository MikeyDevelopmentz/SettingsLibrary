package me.mikey.settingslibrary.api;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SettingEntry {

    private final String id;
    private final SettingType type;
    private final Supplier<ItemStack> itemSupplier;
    private final SettingClickHandler clickHandler;
    private final String permission;
    private final int slot;

    private SettingEntry(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.itemSupplier = builder.itemSupplier;
        this.clickHandler = builder.clickHandler;
        this.permission = builder.permission;
        this.slot = builder.slot;
    }

    public static SettingEntry toggle(String id, Supplier<ItemStack> itemSupplier,
                                       Supplier<Boolean> getter, BiConsumer<Player, Boolean> setter) {
        return new Builder(id, SettingType.TOGGLE)
                .itemSupplier(itemSupplier)
                .clickHandler((player, clickType) -> {
                    boolean newValue = !getter.get();
                    setter.accept(player, newValue);
                })
                .build();
    }

    public static SettingEntry action(String id, ItemStack item, BiConsumer<Player, ClickType> action) {
        return new Builder(id, SettingType.ACTION)
                .item(item)
                .clickHandler(action::accept)
                .build();
    }

    public static SettingEntry display(String id, Supplier<ItemStack> itemSupplier) {
        return new Builder(id, SettingType.DISPLAY)
                .itemSupplier(itemSupplier)
                .build();
    }

    public static SettingEntry custom(String id, Supplier<ItemStack> itemSupplier, SettingClickHandler handler) {
        return new Builder(id, SettingType.CUSTOM)
                .itemSupplier(itemSupplier)
                .clickHandler(handler)
                .build();
    }

    public static Builder builder(String id, SettingType type) {
        return new Builder(id, type);
    }

    public String getId() {
        return id;
    }

    public SettingType getType() {
        return type;
    }

    public ItemStack getItem() {
        return itemSupplier.get();
    }

    public Supplier<ItemStack> getItemSupplier() {
        return itemSupplier;
    }

    public SettingClickHandler getClickHandler() {
        return clickHandler;
    }

    public String getPermission() {
        return permission;
    }

    public int getSlot() {
        return slot;
    }

    public boolean hasPermission(Player player) {
        return permission == null || player.hasPermission(permission);
    }

    public void handleClick(Player player, ClickType clickType) {
        if (clickHandler != null) {
            clickHandler.onClick(player, clickType);
        }
    }

    public static class Builder {
        private final String id;
        private final SettingType type;
        private Supplier<ItemStack> itemSupplier;
        private SettingClickHandler clickHandler;
        private String permission;
        private int slot = -1;

        public Builder(String id, SettingType type) {
            this.id = id;
            this.type = type;
        }

        public Builder item(ItemStack item) {
            this.itemSupplier = () -> item;
            return this;
        }

        public Builder itemSupplier(Supplier<ItemStack> supplier) {
            this.itemSupplier = supplier;
            return this;
        }

        public Builder clickHandler(SettingClickHandler handler) {
            this.clickHandler = handler;
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder slot(int slot) {
            this.slot = slot;
            return this;
        }

        public SettingEntry build() {
            if (itemSupplier == null) {
                throw new IllegalStateException("Item or ItemSupplier must be set");
            }
            return new SettingEntry(this);
        }
    }

    @FunctionalInterface
    public interface SettingClickHandler {
        void onClick(Player player, ClickType clickType);
    }
}
