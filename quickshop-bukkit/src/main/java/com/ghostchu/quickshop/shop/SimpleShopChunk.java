/*
 *  This file is a part of project QuickShop, the name is SimpleShopChunk.java
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

import com.ghostchu.quickshop.api.shop.ShopChunk;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class SimpleShopChunk implements ShopChunk {
    @NotNull
    private final String world;

    private final int x;

    private final int z;

    @Override
    public boolean isSame(@NotNull World world, int x, int z) {
        return isSame(world.getName(), x, z);
    }

    @Override
    public boolean isSame(@NotNull String world, int x, int z) {
        return this.x == x && this.z == z && this.world.equals(world);
    }

    @Override
    public @NotNull String getWorld() {
        return world;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }
}
