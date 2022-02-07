/*
 * This file is a part of project QuickShop, the name is ShopClickEvent.java
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
 * Calling when shop clicked
 */
public class ShopClickEvent extends AbstractQSEvent implements Cancellable {

    @NotNull
    private final Shop shop;

    private boolean cancelled;

    /**
     * Call when shop was clicked.
     *
     * @param shop The shop bought from
     */
    public ShopClickEvent(@NotNull Shop shop) {
        this.shop = shop;
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
     * Getting the shops that clicked
     *
     * @return Clicked shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }
}
