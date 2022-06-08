/*
 *  This file is a part of project QuickShop, the name is ShopControlPanelManager.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.api.shop;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Shop Control Manager and registry
 */
public interface ShopControlPanelManager {
    /**
     * Register a {@link ShopControlPanel} to the manager
     *
     * @param panel the panel to register
     */
    void register(@NotNull ShopControlPanel panel);

    /**
     * Unregister a {@link ShopControlPanel} from the manager
     *
     * @param panel the panel to unregister
     */

    void unregister(@NotNull ShopControlPanel panel);

    /**
     * Unregister all {@link ShopControlPanel} from the manager that registered by specified plugin
     *
     * @param plugin the plugin to unregister
     */
    void unregister(@NotNull Plugin plugin);

    /**
     * Open ShopControlPanels for the player about specified shop
     *
     * @param player the player to open
     * @param shop   the shop to open
     */

    void openControlPanel(@NotNull Player player, @NotNull Shop shop);
}
