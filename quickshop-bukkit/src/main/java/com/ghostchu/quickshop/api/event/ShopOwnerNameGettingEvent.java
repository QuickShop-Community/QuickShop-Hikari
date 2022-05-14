/*
 *  This file is a part of project QuickShop, the name is ShopOwnerNameGettingEvent.java
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
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;

import java.util.UUID;
@AllArgsConstructor
public class ShopOwnerNameGettingEvent extends AbstractQSEvent {
    private final Shop shop;
    private final UUID owner;
    private Component name;

    /**
     * Getting the shop that trying getting the shop owner name
     *
     * @return The shop
     */
    public Shop getShop() {
        return shop;
    }

    /**
     * Getting the shop owner unique id
     *
     * @return The shop owner unique id
     */
    public UUID getOwner() {
        return owner;
    }

    /**
     * Getting the shop owner display name
     *
     * @return The shop owner display name
     */
    public Component getName() {
        return name;
    }

    /**
     * Sets the shop owner display name
     *
     * @param name New shop owner display name, just display, won't change actual shop owner
     */
    public void setName(Component name) {
        this.name = name;
    }
}
