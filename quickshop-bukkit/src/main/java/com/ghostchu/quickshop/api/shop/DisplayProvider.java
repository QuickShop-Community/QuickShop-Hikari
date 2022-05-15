/*
 *  This file is a part of project QuickShop, the name is DisplayProvider.java
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
