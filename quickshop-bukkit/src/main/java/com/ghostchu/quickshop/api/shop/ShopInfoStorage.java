/*
 *  This file is a part of project QuickShop, the name is ShopInfoStorage.java
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

import com.ghostchu.quickshop.util.serialize.BlockPos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

/**
 * Minimal information about a shop.
 */
@AllArgsConstructor
@Data
@Builder
public class ShopInfoStorage {
    private final String world;
    private final BlockPos position;
    private final UUID owner;
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
    private final Map<UUID, String> permission;
}
