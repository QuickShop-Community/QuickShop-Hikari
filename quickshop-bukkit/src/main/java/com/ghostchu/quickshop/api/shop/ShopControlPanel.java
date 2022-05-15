/*
 *  This file is a part of project QuickShop, the name is ShopControlPanel.java
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
