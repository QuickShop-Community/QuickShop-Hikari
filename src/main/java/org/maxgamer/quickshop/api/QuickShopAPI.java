/*
 * This file is a part of project QuickShop, the name is QuickShopAPI.java
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

package org.maxgamer.quickshop.api;

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.api.command.CommandManager;
import org.maxgamer.quickshop.api.compatibility.CompatibilityManager;
import org.maxgamer.quickshop.api.database.DatabaseHelper;
import org.maxgamer.quickshop.api.localization.text.TextManager;
import org.maxgamer.quickshop.api.shop.ItemMatcher;
import org.maxgamer.quickshop.api.shop.ShopManager;
import org.maxgamer.quickshop.shop.inventory.InventoryWrapperRegistry;
import org.maxgamer.quickshop.util.GameVersion;

import java.util.Map;

/**
 * The unique entry point to allow you to access most features of QuickShop
 */
public interface QuickShopAPI {
    /**
     * Getting Compatibility Manager (usually used for anti-cheat)
     *
     * @return Compatibility Manager
     */
    CompatibilityManager getCompatibilityManager();

    /**
     * Getting Shop Manager which managing most of shops
     *
     * @return Shop manager
     */
    ShopManager getShopManager();

    /**
     * Getting QuickShop current stacking item support status
     *
     * @return Stacking Item support enabled
     */
    boolean isAllowStack();

    /**
     * Getting QuickShop current display item support status
     *
     * @return Display item enabled
     */
    boolean isDisplayEnabled();

    /**
     * Getting shop limit system status
     * false if limit system is disabled
     *
     * @return Limit enabled
     */
    boolean isLimit();

    /**
     * Getting the mapping of permission to shop amounts
     *
     * @return Permissions <-> Shop Amounts mapping
     */
    Map<String, Integer> getLimits();

    /**
     * Getting the helper to directly access the database
     *
     * @return The database helper
     */
    DatabaseHelper getDatabaseHelper();

    /**
     * Getting text manager that allow addon to create a user language locale based message
     *
     * @return The text maanger
     */
    TextManager getTextManager();

    /**
     * Getting current using ItemMatcher impl
     *
     * @return The item matcher
     */
    ItemMatcher getItemMatcher();

    /**
     * Check if fee required for changing shop price
     *
     * @return requires fee
     */
    boolean isPriceChangeRequiresFee();

    /**
     * Getting command manager that allow addon direct access QuickShop sub-command system
     *
     * @return The command manager
     */
    CommandManager getCommandManager();

    /**
     * Getting this server game version
     *
     * @return Game version
     */
    GameVersion getGameVersion();

    /**
     * Gets registry of InventoryWrappers
     * @return registry
     */
    @NotNull
    InventoryWrapperRegistry getInventoryWrapperRegistry();

}
