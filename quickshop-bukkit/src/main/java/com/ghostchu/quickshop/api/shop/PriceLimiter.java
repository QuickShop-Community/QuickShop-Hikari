/*
 *  This file is a part of project QuickShop, the name is PriceLimiter.java
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

import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility used for shop price validating
 */
public interface PriceLimiter {
    /**
     * Check the price restriction rules
     *
     * @param sender   the sender
     * @param stack    the item to check
     * @param currency the currency
     * @param price    the price
     * @return the result
     */
    @NotNull PriceLimiterCheckResult check(@NotNull CommandSender sender, @NotNull ItemStack stack, @Nullable String currency, double price);
}
