/*
 * This file is a part of project QuickShop, the name is Info.java
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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shop simple info, used for shop processing and shop creation.
 */
public interface Info {
    /**
     * @return ShopAction action, Get shop action.
     */
    @NotNull ShopAction getAction();

    void setAction(@NotNull ShopAction action);

    /**
     * @return ItemStack iStack, Get Shop's selling/buying item's ItemStack.
     */
    @NotNull ItemStack getItem();

    /**
     * @return Location loc, Get shop's location,
     */
    @NotNull Location getLocation();

    /**
     * @return Block signBlock, Get block of shop's sign, may return the null.
     */
    @Nullable Block getSignBlock();

    /**
     * Get shop is or not has changed.
     *
     * @param shop, The need checked with this shop.
     * @return hasChanged
     */
    boolean hasChanged(@NotNull Shop shop);

    /**
     * Check if this Info marked as skip protection checks
     *
     * @return Bypassed Protection Checks
     */
    boolean isBypassed();

}
