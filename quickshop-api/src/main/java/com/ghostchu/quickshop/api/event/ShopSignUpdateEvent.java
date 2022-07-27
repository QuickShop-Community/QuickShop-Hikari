package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;

/**
 * Calling when shop sign update, Can't cancel
 */
public class ShopSignUpdateEvent extends AbstractQSEvent {

    @NotNull
    private final Shop shop;

    @NotNull
    private final Sign sign;

    /**
     * Will call when shop sign was updated.
     *
     * @param shop Target shop
     * @param sign Updated sign
     */
    public ShopSignUpdateEvent(@NotNull Shop shop, @NotNull Sign sign) {
        this.shop = shop;
        this.sign = sign;
    }

    /**
     * Gets the shop
     *
     * @return the shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }

    /**
     * Gets the sign
     *
     * @return the sign
     */
    public @NotNull Sign getSign() {
        return this.sign;
    }
}
