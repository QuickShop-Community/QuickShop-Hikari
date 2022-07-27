package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

/**
 * Getting the unloading shop, Can't cancel.
 */
public class ShopUnloadEvent extends AbstractQSEvent {

    @NotNull
    private final Shop shop;

    /**
     * Getting the unloading shop, Can't cancel.
     *
     * @param shop The shop to unload
     */
    public ShopUnloadEvent(@NotNull Shop shop) {
        this.shop = shop;
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
