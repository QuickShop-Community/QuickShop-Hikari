/*
 * This file is a part of project QuickShop, the name is ShopDeleteEvent.java
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

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.api.shop.Shop;

/**
 * Calling when shop deleting
 */
public class ShopDeleteEvent extends AbstractQSEvent implements Cancellable {

    private final boolean fromMemory;

    @NotNull
    private final Shop shop;

    private boolean cancelled;

    /**
     * Call the event when shop is deleting. The ShopUnloadEvent will call after ShopDeleteEvent
     *
     * @param shop       Target shop
     * @param fromMemory Only delete from the memory? false = delete both in memory and database
     */
    public ShopDeleteEvent(@NotNull Shop shop, boolean fromMemory) {
        this.shop = shop;
        this.fromMemory = fromMemory;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Gets the delete is from memory or also database
     *
     * @return only from memory
     */
    public boolean isFromMemory() {
        return this.fromMemory;
    }

    /**
     * Gets the shop that deleted
     *
     * @return The shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }
}
