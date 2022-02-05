/*
 * This file is a part of project QuickShop, the name is ShopPriceChangeEvent.java
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
 * Calling when shop price was changed
 */
public class ShopPriceChangeEvent extends AbstractQSEvent implements Cancellable {

    private final double newPrice;

    private final double oldPrice;

    @NotNull
    private final Shop shop;

    private boolean cancelled;

    /**
     * Will call when shop price was changed.
     *
     * @param shop     Target shop
     * @param oldPrice The old shop price
     * @param newPrice The new shop price
     */
    public ShopPriceChangeEvent(@NotNull Shop shop, double oldPrice, double newPrice) {
        this.shop = shop;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
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
     * Gets the price that shop will use
     *
     * @return new price
     */
    public double getNewPrice() {
        return this.newPrice;
    }

    /**
     * Gets the price that shop used before
     *
     * @return old price
     */
    public double getOldPrice() {
        return this.oldPrice;
    }

    /**
     * Gets the shop
     *
     * @return the shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }
}
