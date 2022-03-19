/*
 *  This file is a part of project QuickShop, the name is ShopDisplayItemSafeGuardEvent.java
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
import org.bukkit.entity.Item;
import org.jetbrains.annotations.NotNull;

/**
 * This event called after QuickShop safe guarded (+protection flags and attributes) a display item.
 */
public class ShopDisplayItemSafeGuardEvent extends AbstractQSEvent {
    @NotNull
    private final Shop shop;

    @NotNull
    private final Item entity;

    /**
     * This event called after QuickShop safe guarded (+protection flags and attributes) a display item.
     * @param shop the shop
     * @param entity the display item
     */
    public ShopDisplayItemSafeGuardEvent(@NotNull Shop shop, @NotNull Item entity) {
        this.shop = shop;
        this.entity = entity;
    }

    /**
     * Gets the shop that the display item belongs to.
     * @return the shop
     */
    @NotNull
    public Shop getShop() {
        return shop;
    }

    /**
     * Gets the display item.
     * @return the display item
     */
    @NotNull
    public Item getEntity() {
        return entity;
    }
}
