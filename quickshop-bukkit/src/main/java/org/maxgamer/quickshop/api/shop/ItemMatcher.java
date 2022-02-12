/*
 * This file is a part of project QuickShop, the name is ItemMatcher.java
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

package org.maxgamer.quickshop.api.shop;

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
