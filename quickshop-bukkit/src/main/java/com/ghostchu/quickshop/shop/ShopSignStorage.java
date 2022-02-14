/*
 *  This file is a part of project QuickShop, the name is ShopSignStorage.java
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

package com.ghostchu.quickshop.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/**
 * TODO This class used for storage the shop sign
 */
@AllArgsConstructor
@Data
@Builder
public class ShopSignStorage {
    private static final boolean SHOP_SIGN = true;
    private String world;
    private int x;
    private int y;
    private int z;

    public boolean equals(String world, int x, int y, int z) {
        return Objects.equals(this.world, world) && this.x == x && this.y == y && this.z == z;
    }
}
