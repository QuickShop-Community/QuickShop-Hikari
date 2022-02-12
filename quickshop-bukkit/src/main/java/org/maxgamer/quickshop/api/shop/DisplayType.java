/*
 * This file is a part of project QuickShop, the name is DisplayType.java
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.shop.RealDisplayItem;
import org.maxgamer.quickshop.shop.VirtualDisplayItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum DisplayType {
    /*
     * REALITEM = USE REAL DROPPED ITEM
     * ARMORSTAND = USE ARMORSTAND DISPLAY
     * VIRTUALITEM = USE VIRTUAL DROPPED ITEM (CLIENT SIDE)
     * */
    UNKNOWN(-1),
    REALITEM(0),
    //  ARMORSTAND(1),
    VIRTUALITEM(2);

    private static final Map<Integer, DisplayType> TYPE_MAP;

    static {
        Map<Integer, DisplayType> map = new HashMap<>(values().length);
        for (DisplayType type : values()) {
            map.put(type.id, type);
        }
        TYPE_MAP = Collections.unmodifiableMap(map);
    }

    private final int id;

    DisplayType(int id) {
        this.id = id;
    }

    public static @NotNull DisplayType fromID(int id) {
        return TYPE_MAP.getOrDefault(id, UNKNOWN);
    }

    public static int toID(@NotNull DisplayType displayType) {
        return displayType.id;
    }

    public static DisplayType typeIs(@Nullable AbstractDisplayItem displayItem) {
        if (displayItem instanceof RealDisplayItem) {
            return REALITEM;
        }
        if (displayItem instanceof VirtualDisplayItem) {
            return VIRTUALITEM;
        }
        return UNKNOWN;
    }

    public int toID() {
        return id;
    }
}
