package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This event is called before the shop display item created
 */
public class ShopDisplayItemSpawnEvent extends AbstractQSEvent implements QSCancellable {

    @NotNull
    private final DisplayType displayType;

    @NotNull
    private final ItemStack itemStack;

    @NotNull
    private final Shop shop;

    private boolean cancelled;
    private @Nullable Component cancelReason;
    /**
     * This event is called before the shop display item created
     *
     * @param shop        Target shop
     * @param displayType The displayType
     * @param itemStack   Target ItemStack
     */
    public ShopDisplayItemSpawnEvent(
            @NotNull Shop shop, @NotNull ItemStack itemStack, @NotNull DisplayType displayType) {
        this.shop = shop;
        this.itemStack = itemStack;
        this.displayType = displayType;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel, @Nullable Component reason) {
        this.cancelled = cancel;
        this.cancelReason = reason;
    }

    @Override
    public @Nullable Component getCancelReason() {
        return this.cancelReason;
    }

    /**
     * Gets the current display type
     *
     * @return DisplayType
     */
    public @NotNull DisplayType getDisplayType() {
        return this.displayType;
    }

    /**
     * Gets the ItemStack used for display
     *
     * @return The display ItemStack
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
}
