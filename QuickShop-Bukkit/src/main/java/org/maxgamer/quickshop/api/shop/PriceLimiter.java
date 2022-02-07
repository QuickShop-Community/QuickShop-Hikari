/*
 * This file is a part of project QuickShop, the name is PriceLimiter.java
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
import org.jetbrains.annotations.NotNull;

/**
 * Utility used for shop price validating
 */
public interface PriceLimiter {
    /**
     * Checks a stack with a price is allowed
     *
     * @param stack The item
     * @param price The price
     * @return Allowed
     */
    @NotNull
    PriceLimiterCheckResult check(@NotNull ItemStack stack, double price);

}
