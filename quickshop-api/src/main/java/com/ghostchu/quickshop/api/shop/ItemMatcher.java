package com.ghostchu.quickshop.api.shop;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A custom impl that matching items between two ItemStack
 */
public interface ItemMatcher {
    /**
     * Gets the ItemMatcher provider name
     *
     * @return Provider name
     */
    @NotNull String getName();

    /**
     * Gets the ItemMatcher provider plugin instance
     *
     * @return Provider Plugin instance
     */
    @NotNull Plugin getPlugin();

    /**
     * Tests ItemStacks is matches
     * BEWARE: Different order of itemstacks might get different results
     *
     * @param original The original ItemStack
     * @param tester   The ItemStack will test matches with original itemstack.
     * @return The result of tests
     */
    boolean matches(@Nullable ItemStack original, @Nullable ItemStack tester);
}
