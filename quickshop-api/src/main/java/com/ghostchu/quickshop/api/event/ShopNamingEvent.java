package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when shop clicked
 */
public class ShopNamingEvent extends AbstractQSEvent implements QSCancellable {

    @NotNull
    private final Shop shop;

    @Nullable
    private String name;

    private boolean cancelled;

    private @Nullable Component cancelReason;

    /**
     * Call when shop was renaming.
     *
     * @param shop The shop bought from
     */
    public ShopNamingEvent(@NotNull Shop shop, @NotNull String name) {
        this.shop = shop;
        this.name = name;
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
     * Getting the shops that clicked
     *
     * @return Clicked shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }

    /**
     * Gets the new shop name
     *
     * @return The new shop name, null if removing
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Sets the shop new name
     *
     * @param name Shop new name
     */
    public void setName(@Nullable String name) {
        this.name = name;
    }
}
