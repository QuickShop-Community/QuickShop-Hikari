/*
 * This file is a part of project QuickShop, the name is ShopPersistentDataType.java
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.util.MsgUtil;

public class ShopPersistentDataType implements PersistentDataType<String, ShopPersistentData> {
    static final ShopPersistentDataType INSTANCE = new ShopPersistentDataType();

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<ShopPersistentData> getComplexType() {
        return ShopPersistentData.class;
    }

    @NotNull
    @Override
    public String toPrimitive(@NotNull ShopPersistentData complex, @NotNull PersistentDataAdapterContext context) {
        try {
            return GSON.toJson(complex);
        } catch (Exception th) {
            MsgUtil.debugStackTrace(th.getStackTrace());
            return "";
        }
    }

    @NotNull
    @Override
    public ShopPersistentData fromPrimitive(
            @NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        try {
            return GSON.fromJson(primitive, ShopPersistentData.class);
        } catch (Exception th) {
            MsgUtil.debugStackTrace(th.getStackTrace());
            return new ShopPersistentData(0, 0, 0, "null", false);
        }
    }
}
