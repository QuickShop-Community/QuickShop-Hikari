package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Fire when watcher processing the shop ongoing fee
 */
public class ShopOngoingFeeEvent extends AbstractQSEvent implements QSCancellable {
    private final UUID player;

    private final Shop shop;

    private double cost;
    private boolean cancelled;
    private @Nullable Component cancelReason;

    public ShopOngoingFeeEvent(Shop shop, UUID player, double cost) {
        this.shop = shop;
        this.player = player;
        this.cost = cost;
    }

    /**
     * Getting the cost in this event
     *
     * @return The ongoing fee
     */
    public double getCost() {
        return cost;
    }

    /**
     * Sets the ongoing fee to replace old one
     *
     * @param cost The ongoing fee
     */
    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * Getting related shop in this event
     *
     * @return The shop triggered ongoing fee event
     */
    public Shop getShop() {
        return shop;
    }

    /**
     * Getting related player in this event
     *
     * @return The player triggered ongoing fee event
     */
    public UUID getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
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
}
