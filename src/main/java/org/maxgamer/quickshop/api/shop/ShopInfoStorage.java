/*
 * This file is a part of project QuickShop, the name is ShopInfoStorage.java
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

package org.maxgamer.quickshop.api.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import me.lucko.helper.serialize.BlockPosition;

import java.util.UUID;

/**
 * TODO This class used for storage the shop
 */
@AllArgsConstructor
@Data
@Builder
public class ShopInfoStorage {
    private final String world;
    private final BlockPosition position;
    private final String moderator;
    private final double price;
    private final String item;
    private final int unlimited;
    private final int shopType;
    private final String extra;
    private final String currency;
    private final boolean disableDisplay;
    private final UUID taxAccount;
    private final String inventoryWrapperName;
    private final String symbolLink;
}
