package com.ghostchu.quickshop.api.inventory;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * InventoryWrapper for countable Inventory
 */
public interface CountableInventoryWrapper extends InventoryWrapper {
    /**
     * Counting the spaces
     *
     * @param predicate {@link ItemPredicate}
     * @return the space
     */
    int countSpace(@NotNull ItemPredicate predicate);

    /**
     * Counting the items
     *
     * @param predicate {@link ItemPredicate}
     * @return the items
     */
    int countItem(@NotNull ItemPredicate predicate);

    /**
     * The item predicate for calculating
     */
    interface ItemPredicate {
        /**
         * Check if the item match the predicate
         *
         * @param input the item want to check
         * @return if the item match the predicate
         */
        boolean isMatch(@NotNull ItemStack input);
    }
}
