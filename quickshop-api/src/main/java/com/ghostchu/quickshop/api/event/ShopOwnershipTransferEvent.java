package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Fire when a shop ownership was transferred.
 */
public class ShopOwnershipTransferEvent extends AbstractQSEvent implements QSCancellable {
    private final Shop shop;
    private final UUID oldOwner;
    private final UUID newOwner;
    private boolean cancelled;
    private Component reason;

    /**
     * Create a new ShopOwnershipTransferEvent.
     *
     * @param shop     The shop.
     * @param oldOwner The old owner.
     * @param newOwner The new owner.
     */
    public ShopOwnershipTransferEvent(@NotNull Shop shop, @NotNull UUID oldOwner, @NotNull UUID newOwner) {
        this.shop = shop;
        this.oldOwner = oldOwner;
        this.newOwner = newOwner;
    }

    @Override
    public @Nullable Component getCancelReason() {
        return reason;
    }

    @Override
    public void setCancelled(boolean cancel, @Nullable Component reason) {
        this.cancelled = cancel;
        this.reason = reason;
    }

    /**
     * Gets the new owner will transfer to.
     *
     * @return The new owner.
     */
    @NotNull
    public UUID getNewOwner() {
        return newOwner;
    }

    /**
     * Gets the old owner that transfer from.
     *
     * @return The old owner.
     */
    @NotNull
    public UUID getOldOwner() {
        return oldOwner;
    }

    /**
     * Gets the shop related to this event.
     *
     * @return The shop.
     */
    @NotNull
    public Shop getShop() {
        return shop;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
