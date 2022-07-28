package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.entity.Item;
import org.jetbrains.annotations.NotNull;

/**
 * This event called after QuickShop safe guarded (+protection flags and attributes) a display item.
 */
public class ShopDisplayItemSafeGuardEvent extends AbstractQSEvent {
    @NotNull
    private final Shop shop;

    @NotNull
    private final Item entity;

    /**
     * This event called after QuickShop safe guarded (+protection flags and attributes) a display item.
     *
     * @param shop   the shop
     * @param entity the display item
     */
    public ShopDisplayItemSafeGuardEvent(@NotNull Shop shop, @NotNull Item entity) {
        this.shop = shop;
        this.entity = entity;
    }

    /**
     * Gets the shop that the display item belongs to.
     *
     * @return the shop
     */
    @NotNull
    public Shop getShop() {
        return shop;
    }

    /**
     * Gets the display item.
     *
     * @return the display item
     */
    @NotNull
    public Item getEntity() {
        return entity;
    }
}
