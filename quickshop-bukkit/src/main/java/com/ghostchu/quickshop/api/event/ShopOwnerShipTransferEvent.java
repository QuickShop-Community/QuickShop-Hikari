package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ShopOwnerShipTransferEvent extends AbstractQSEvent implements Cancellable {
    private final Shop shop;
    private final UUID oldOwner;
    private final UUID newOwner;
    private boolean cancelled;

    public ShopOwnerShipTransferEvent(Shop shop, UUID oldOwner, UUID newOwner) {
        this.shop = shop;
        this.oldOwner = oldOwner;
        this.newOwner = newOwner;
    }

    @NotNull
    public Shop getShop() {
        return shop;
    }


    @NotNull
    public UUID getNewOwner() {
        return newOwner;
    }

    @NotNull
    public UUID getOldOwner() {
        return oldOwner;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
