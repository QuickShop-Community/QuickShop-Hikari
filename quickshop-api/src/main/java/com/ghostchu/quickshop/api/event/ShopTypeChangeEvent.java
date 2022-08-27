package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when shop item was changed
 */
public class ShopTypeChangeEvent extends AbstractQSEvent implements QSCancellable {
    private final ShopType oldType;

    private final ShopType newType;

    @NotNull
    private final Shop shop;

    private boolean cancelled;
    private @Nullable Component cancelReason;

    /**
     * Will call when shop type was changed.
     *
     * @param shop    Target shop
     * @param oldType The old shop type
     * @param newType The new shop type
     */
    public ShopTypeChangeEvent(@NotNull Shop shop, ShopType oldType, ShopType newType) {
        this.shop = shop;
        this.oldType = oldType;
        this.newType = newType;
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
     * The shop new ShopType
     *
     * @return new type
     */
    public ShopType getNewType() {
        return this.newType;
    }

    /**
     * The shop old ShopType
     *
     * @return old type
     */
    public ShopType getOldType() {
        return this.oldType;
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
