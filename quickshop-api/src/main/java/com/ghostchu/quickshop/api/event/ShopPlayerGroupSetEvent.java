package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

/**
 * Fire when a player's group is set or changed.
 */
public class ShopPlayerGroupSetEvent extends AbstractQSEvent {
    private final Shop shop;
    private final String oldGroup;
    private final String newGroup;

    /**
     * Constructor.
     *
     * @param shop     The shop.
     * @param oldGroup The old group.
     * @param newGroup The new group.
     */
    public ShopPlayerGroupSetEvent(@NotNull Shop shop, @NotNull String oldGroup, @NotNull String newGroup) {
        this.shop = shop;
        this.oldGroup = oldGroup;
        this.newGroup = newGroup;
    }

    /**
     * Get the new group.
     *
     * @return The new group. ({@link com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup#EVERYONE} if removing)
     */
    @NotNull
    public String getNewGroup() {
        return newGroup;
    }

    /**
     * Get the old group.
     *
     * @return The old group. ({@link com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup#EVERYONE} if adding)
     */
    @NotNull
    public String getOldGroup() {
        return oldGroup;
    }

    /**
     * Get the shop that the player's group was set in.
     *
     * @return The shop.
     */
    @NotNull
    public Shop getShop() {
        return shop;
    }
}
