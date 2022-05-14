/*
 *  This file is a part of project QuickShop, the name is ShopAction.java
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

package com.ghostchu.quickshop.api.shop;

/**
 * The actions about player and shop
 */
public enum ShopAction {
    // buy = trading create = creating shop cancelled = stopped
    PURCHASE_BUY,
    PURCHASE_SELL,
    CREATE_SELL,
    CREATE_BUY,
    CANCELLED;

    public boolean isTrading() {
        return this == PURCHASE_BUY || this == PURCHASE_SELL;
    }

    public boolean isCreating() {
        return this == CREATE_SELL || this == CREATE_BUY;
    }
}
