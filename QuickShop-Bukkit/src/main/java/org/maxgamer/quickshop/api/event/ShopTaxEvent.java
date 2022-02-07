/*
 * This file is a part of project QuickShop, the name is ShopTaxEvent.java
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

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.UUID;

/**
 * Calling when shop tax calcing
 */
public class ShopTaxEvent extends AbstractQSEvent {
    private final UUID user;
    private final Shop shop;
    private double tax;

    /**
     * Call when shop calc shop tax that will pay to system account and remove from shop owner/player received money
     *
     * @param shop The shop
     * @param tax  The tax
     * @param user The user (buyer/seller)
     */
    public ShopTaxEvent(@NotNull Shop shop, double tax, @NotNull UUID user) {
        this.shop = shop;
        this.tax = tax;
        this.user = user;
    }

    /**
     * Gets the user (buyer or seller)
     *
     * @return User
     */
    public UUID getUser() {
        return this.user;
    }

    /**
     * Gets the shop
     *
     * @return the shop
     */
    public Shop getShop() {
        return this.shop;
    }

    /**
     * Gets the tax in purchase
     *
     * @return tax
     */
    public double getTax() {
        return this.tax;
    }

    /**
     * Sets the new tax in purchase
     *
     * @param tax New tax
     */
    public void setTax(double tax) {
        this.tax = tax;
    }
}
