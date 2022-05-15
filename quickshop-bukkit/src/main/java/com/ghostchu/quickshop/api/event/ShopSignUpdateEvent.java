/*
 *  This file is a part of project QuickShop, the name is ShopSignUpdateEvent.java
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

package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;

/**
 * Calling when shop sign update, Can't cancel
 */
public class ShopSignUpdateEvent extends AbstractQSEvent {

    @NotNull
    private final Shop shop;

    @NotNull
    private final Sign sign;

    /**
     * Will call when shop sign was updated.
     *
     * @param shop Target shop
     * @param sign Updated sign
     */
    public ShopSignUpdateEvent(@NotNull Shop shop, @NotNull Sign sign) {
        this.shop = shop;
        this.sign = sign;
    }

    /**
     * Gets the shop
     *
     * @return the shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }

    /**
     * Gets the sign
     *
     * @return the sign
     */
    public @NotNull Sign getSign() {
        return this.sign;
    }
}
