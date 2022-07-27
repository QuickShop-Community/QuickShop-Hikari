package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when shop clicked
 */
public class ShopClickEvent extends AbstractQSEvent implements QSCancellable {

    @NotNull
    private final Shop shop;
    private final Player player;

    private boolean cancelled;

    private @Nullable Component cancelReason;

    /**
     * Call when shop was clicked.
     *
     * @param shop The shop bought from
     */
    public ShopClickEvent(@NotNull Shop shop, @NotNull Player player) {
        this.shop = shop;
        this.player = player;
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
     * Getting the player who clicked shop
     *
     * @return The player
     */
    public @NotNull Player getClicker() {
        return this.player;
    }


}
