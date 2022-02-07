/*
 * This file is a part of project QuickShop, the name is ShopOngoingFeeEvent.java
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
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.UUID;

public class ShopOngoingFeeEvent extends AbstractQSEvent implements Cancellable {
    private final UUID player;

    private final Shop shop;

    private double cost;
    private boolean cancelled;

    public ShopOngoingFeeEvent(Shop shop, UUID player, double cost) {
        this.shop = shop;
        this.player = player;
        this.cost = cost;
    }

    /**
     * Getting the cost in this event
     *
     * @return The ongoing fee
     */
    public double getCost() {
        return cost;
    }

    /**
     * Sets the ongoing fee to replace old one
     *
     * @param cost The ongoing fee
     */
    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * Getting related shop in this event
     *
     * @return The shop triggered ongoing fee event
     */
    public Shop getShop() {
        return shop;
    }

    /**
     * Getting related player in this event
     *
     * @return The player triggered ongoing fee event
     */
    public UUID getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
