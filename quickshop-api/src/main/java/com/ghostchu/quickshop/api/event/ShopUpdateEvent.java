package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when shop update to database
 */
public class ShopUpdateEvent extends AbstractQSEvent implements QSCancellable {

    @NotNull
    private final Shop shop;

    private boolean cancelled;
    private @Nullable Component cancelReason;

    /**
     * Call when shop is trying updated to database
     *
     * @param shop The shop bought from
     */
    public ShopUpdateEvent(@NotNull Shop shop) {
        super();
        this.shop = shop;
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
