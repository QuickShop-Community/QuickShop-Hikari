package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Calling when purchaser prepared to purchase a shop (click the shop)
 */
public class ShopInfoPanelEvent extends AbstractQSEvent implements QSCancellable {

    @NotNull
    private final Shop shop;

    @NotNull
    private final UUID purchaser;
    @Nullable
    private final Player player;

    private boolean cancelled;
    private Component cancelReason;

    /**
     * Builds a new shop info panel event
     *
     * @param shop      The shop bought from
     * @param purchaser The player purchasing, may offline if purchase by plugin
     */
    public ShopInfoPanelEvent(@NotNull Shop shop, @NotNull UUID purchaser) {
        this.shop = shop;
        this.purchaser = purchaser;
        this.player = Bukkit.getPlayer(purchaser);
    }

    /**
     * Gets the purchaser
     *
     * @return Player or null if purchaser is offline/virtual player.
     */
    public @Nullable Player getPlayer() {
        return this.player;
    }

    /**
     * Gets the purchaser, that maybe is an online/offline/virtual player.
     *
     * @return The purchaser uuid
     */
    public @NotNull UUID getPurchaser() {
        return this.purchaser;
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
    public @Nullable Component getCancelReason() {
        return this.cancelReason;
    }

    @Override
    public void setCancelled(boolean cancel, @Nullable Component reason) {
        this.cancelled = cancel;
        this.cancelReason = reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
