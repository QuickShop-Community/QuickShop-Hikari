/*
 * This file is a part of project QuickShop, the name is ShopPersistentData.java
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
package org.maxgamer.quickshop.shop;

import com.google.gson.annotations.Expose;
import lombok.Getter;

//TODO
@Getter
public class ShopPersistentData {
    @Expose
    private final String world;
    @Expose
    private final int x;
    @Expose
    private final int y;
    @Expose
    private final int z;
    private final boolean setup;

    public ShopPersistentData(int x, int y, int z, String world, boolean setup) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.setup = setup;
    }
}
