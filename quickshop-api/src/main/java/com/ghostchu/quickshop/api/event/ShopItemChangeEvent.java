package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when shop item was changed
 */
public class ShopItemChangeEvent extends AbstractQSEvent implements QSCancellable {
    private final ItemStack oldItem;

    private final ItemStack newItem;

    @NotNull
    private final Shop shop;

    private boolean cancelled;
    private @Nullable Component cancelReason;

    /**
     * Will call when shop price was changed.
     * All the item will be passed with a copy, so you can't change them
     *
     * @param shop    Target shop
     * @param oldItem The old shop item
     * @param newItem The new shop item
     */
    public ShopItemChangeEvent(@NotNull Shop shop, ItemStack oldItem, ItemStack newItem) {
        this.shop = shop;
        this.oldItem = oldItem.clone();
        this.newItem = newItem.clone();
    }

    @Override
    public @Nullable Component getCancelReason() {
        return this.cancelReason;
    }

    @Override
    public void setCancelled(boolean cancel, @Nullable Component reason) {
        this.cancelled = cancel;
        this.cancelReason = reason;
    }

    /**
     * Gets the item that shop will switch to
     *
     * @return NewItem
     */
    public ItemStack getNewItem() {
        return this.newItem;
    }

    /**
     * Gets the item that shop used before
     *
     * @return OldItem
     */
    public ItemStack getOldItem() {
        return this.oldItem;
    }

    /**
     * Gets the shop
     *
     * @return the shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
}
