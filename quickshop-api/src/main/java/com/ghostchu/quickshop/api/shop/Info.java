package com.ghostchu.quickshop.api.shop;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shop simple info, used for shop processing and shop creation.
 */
public interface Info {
    /**
     * @return ShopAction action, Get shop action.
     */
    @NotNull ShopAction getAction();

    void setAction(@NotNull ShopAction action);

    /**
     * @return ItemStack iStack, Get Shop's selling/buying item's ItemStack.
     */
    @NotNull ItemStack getItem();

    /**
     * @return Location loc, Get shop's location,
     */
    @NotNull Location getLocation();

    /**
     * @return Block signBlock, Get block of shop's sign, may return the null.
     */
    @Nullable Block getSignBlock();

    /**
     * Get shop is or not has changed.
     *
     * @param shop, The need checked with this shop.
     * @return hasChanged
     */
    boolean hasChanged(@NotNull Shop shop);

    /**
     * Check if this Info marked as skip protection checks
     *
     * @return Bypassed Protection Checks
     */
    boolean isBypassed();

}
