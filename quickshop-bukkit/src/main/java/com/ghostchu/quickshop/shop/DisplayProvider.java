package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Display Provider can be registered to Bukkit Services Manager
 * and replace the shop displays if user set display-type to CUSTOM
 */
public interface DisplayProvider {
    /**
     * Gets the Display Provider's plugin instance.
     *
     * @return The plugin instance (provider).
     */
    @NotNull
    Plugin getProvider();

    /**
     * Provide a display item impl for specified shop.
     *
     * @param shop The shop to provide display item for.
     * @return The display item.
     */
    @Nullable
    AbstractDisplayItem provide(@NotNull Shop shop);
}
