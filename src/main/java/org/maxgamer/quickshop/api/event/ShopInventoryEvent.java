/*
 * This file is a part of project QuickShop, the name is ShopInventoryEvent.java
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

package org.maxgamer.quickshop.api.event;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.api.shop.Shop;

/**
 * Calling when shop inventory be got and for using
 */
public class ShopInventoryEvent extends AbstractQSEvent {

    @NotNull
    private final Shop shop;
    @NotNull
    private Inventory inventory;

    /**
     * Call when shop was clicked.
     *
     * @param shop The shop bought from
     */
    public ShopInventoryEvent(@NotNull Shop shop, @NotNull Inventory inventory) {
        this.shop = shop;
        this.inventory = inventory;

    }

    /**
     * Getting the Inventory that shop be used
     *
     * @return The inventory
     */
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Sets the Inventory
     *
     * @param inventory new inventory for shop for this time.
     *                  It is not persis.
     */
    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Getting the shops that clicked
     *
     * @return Clicked shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }
}
