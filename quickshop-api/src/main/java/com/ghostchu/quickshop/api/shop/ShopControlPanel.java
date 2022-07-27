package com.ghostchu.quickshop.api.shop;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The shop control panel impl.
 */
public interface ShopControlPanel {
    /**
     * Usually you don't need touch this :)
     *
     * @return The internal usage priority.
     */
    default int getInternalPriority() {
        return getPriority().getPriority();
    }

    /**
     * The shop control panel impl's plugin instance.
     *
     * @return Your plugin instance;
     */
    @NotNull Plugin getPlugin();

    /**
     * The shop control panel's priority.
     * HIGH = Earlier shown
     * LOW = Later shown
     *
     * @return The priority.
     */
    @NotNull
    ShopControlPanelPriority getPriority();

    /**
     * Generate components for the shop control panel.
     *
     * @param player The player
     * @param shop   The shop
     * @return The components, or empty list if nothing to show. Every component will be shown in a new line.
     */
    @NotNull List<Component> generate(@NotNull Player player, @NotNull Shop shop);
}
