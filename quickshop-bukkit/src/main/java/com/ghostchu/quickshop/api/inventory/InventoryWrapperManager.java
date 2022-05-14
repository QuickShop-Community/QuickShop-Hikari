/*
 *  This file is a part of project QuickShop, the name is InventoryWrapperManager.java
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

package com.ghostchu.quickshop.api.inventory;

import org.jetbrains.annotations.NotNull;

/**
 * Manager for InventoryWrappers
 */
public interface InventoryWrapperManager {
    /**
     * Create a symbol link for storage.
     * @param wrapper Storage wrapper
     * @exception IllegalArgumentException If cannot create symbol link for target Inventory.
     * @return Symbol Link that used for locate the Inventory
     */
    @NotNull
    String mklink(@NotNull InventoryWrapper wrapper) throws IllegalArgumentException;

    /**
     * Locate an Inventory with symbol link
     * NOTICE: This can be call multiple times, and maybe multiple InventoryWrapper will exist in same time.
     * You must be sure all wrapper can be process any request in any time.
     * @param symbolLink symbol link that created by InventoryWrapperManager#mklink
     * @return Symbol link
     * @throws IllegalArgumentException If symbol link invalid
     */
    @NotNull
    InventoryWrapper locate(@NotNull String symbolLink) throws IllegalArgumentException;


}
