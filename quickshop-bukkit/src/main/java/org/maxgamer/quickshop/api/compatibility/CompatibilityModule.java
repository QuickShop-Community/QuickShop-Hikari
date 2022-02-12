/*
 * This file is a part of project QuickShop, the name is CompatibilityModule.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.api.compatibility;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface CompatibilityModule {
    /**
     * Gets the CompatibilityModule provider name
     *
     * @return Provider name
     */
    @NotNull String getName();

    /**
     * Gets the CompatibilityModule provider plugin instance
     *
     * @return Provider Plugin instance
     */
    @NotNull Plugin getPlugin();

    /**
     * Calls CompatibilityModule to toggle the detection status for player between on and off
     *
     * @param player The player
     * @param status On or Off
     */
    void toggle(@NotNull Player player, boolean status);
}
