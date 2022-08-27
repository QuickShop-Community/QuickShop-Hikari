package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This event is called after DisplayItem removed
 */
public class ShopDisplayItemDespawnEvent extends AbstractQSEvent implements QSCancellable {

    @NotNull
    private final Shop shop;

    @NotNull
    private final ItemStack itemStack;

    @NotNull
    private final DisplayType displayType;

    private boolean cancelled;
    private @Nullable Component cancelReason;

    /**
     * This event is called before the shop display item created
     *
     * @param shop        Target shop
     * @param itemStack   Target itemstacck
     * @param displayType The displayType
     */
    public ShopDisplayItemDespawnEvent(
            @NotNull Shop shop, @NotNull ItemStack itemStack, @NotNull DisplayType displayType) {
        this.shop = shop;
        this.itemStack = itemStack;
        this.displayType = displayType;
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
     * Gets the current display type
     *
     * @return Display type that current using
     */
    public @NotNull DisplayType getDisplayType() {
        return this.displayType;
    }

    /**
     * Gets the ItemStack that display using
     *
     * @return The ItemStack that display using
     */
    public @NotNull ItemStack getItemStack() {
        return this.itemStack;
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
