/*
 * This file is a part of project QuickShop, the name is ShopTaxAccountGettingEvent.java
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

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.UUID;

@AllArgsConstructor
public class ShopTaxAccountGettingEvent extends AbstractQSEvent {
    private final Shop shop;
    @Nullable
    private UUID taxAccount;

    /**
     * Getting the tax account
     *
     * @return The tax account, null if tax has been disabled
     */
    @Nullable
    public UUID getTaxAccount() {
        return taxAccount;
    }

    /**
     * Sets the tax account
     *
     * @param taxAccount The tax account
     */
    public void setTaxAccount(@Nullable UUID taxAccount) {
        this.taxAccount = taxAccount;
    }

    /**
     * Gets the shop
     *
     * @return The shop
     */
    public Shop getShop() {
        return shop;
    }
}
