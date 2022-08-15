package com.ghostchu.quickshop.api.shop;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Shop Control Manager and registry
 */
public interface ShopControlPanelManager {
    /**
     * Open ShopControlPanels for the player about specified shop
     *
     * @param player the player to open
     * @param shop   the shop to open
     */

    void openControlPanel(@NotNull Player player, @NotNull Shop shop);

    /**
     * Register a {@link ShopControlPanel} to the manager
     *
     * @param panel the panel to register
     */
    void register(@NotNull ShopControlPanel panel);

    /**
     * Unregister all {@link ShopControlPanel} from the manager that registered by specified plugin
     *
     * @param plugin the plugin to unregister
     */
    void unregister(@NotNull Plugin plugin);

    /**
     * Unregister a {@link ShopControlPanel} from the manager
     *
     * @param panel the panel to unregister
     */

    void unregister(@NotNull ShopControlPanel panel);
}
