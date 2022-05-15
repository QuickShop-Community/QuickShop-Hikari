/*
 *  This file is a part of project QuickShop, the name is BukkitItemMatcherImpl.java
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

package com.ghostchu.quickshop.util.matcher.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.ItemMatcher;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple impl for ItemMatcher
 *
 * @author Ghost_chu
 */
@AllArgsConstructor
public class BukkitItemMatcherImpl implements ItemMatcher {
    private final QuickShop plugin;

    /**
     * Gets the ItemMatcher provider name
     *
     * @return Provider name
     */
    @Override
    public @NotNull String getName() {
        return plugin.getName();
    }

    /**
     * Gets the ItemMatcher provider plugin instance
     *
     * @return Provider Plugin instance
     */
    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    /**
     * Tests ItemStacks is matches
     * BEWARE: Different order of itemstacks you might will got different results
     *
     * @param original The original ItemStack
     * @param tester   The ItemStack will test matches with original itemstack.
     * @return The result of tests
     */
    @Override
    public boolean matches(@Nullable ItemStack original, @Nullable ItemStack tester) {
        if (original == null && tester == null) {
            return true;
        }
        boolean originalNull = original == null;
        boolean testerNull = tester == null;
        if (originalNull || testerNull) {
            return false;
        }

        original = original.clone();
        original.setAmount(1);
        tester = tester.clone();
        tester.setAmount(1);

        String shopIdOrigin = plugin.getPlatform().getItemShopId(original);
        if (shopIdOrigin != null) {
            String shopIdTester = plugin.getPlatform().getItemShopId(tester);
            if (shopIdOrigin.equals(shopIdTester)) {
                return true;
            }
        }
        return tester.isSimilar(original);
    }
}
