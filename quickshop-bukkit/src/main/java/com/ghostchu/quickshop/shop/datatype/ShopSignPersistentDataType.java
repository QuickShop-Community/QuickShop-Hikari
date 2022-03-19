/*
 *  This file is a part of project QuickShop, the name is ShopSignPersistentDataType.java
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

package com.ghostchu.quickshop.shop.datatype;

import com.ghostchu.quickshop.shop.ShopSignStorage;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import com.ghostchu.quickshop.util.JsonUtil;

public class ShopSignPersistentDataType
        implements PersistentDataType<String, ShopSignStorage> {
    public static final ShopSignPersistentDataType INSTANCE = new ShopSignPersistentDataType();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<ShopSignStorage> getComplexType() {
        return ShopSignStorage.class;
    }

    @NotNull
    @Override
    public String toPrimitive(
            @NotNull ShopSignStorage complex, @NotNull PersistentDataAdapterContext context) {
        return JsonUtil.getGson().toJson(complex);
    }

    @Override
    public @NotNull ShopSignStorage fromPrimitive(
            @NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return JsonUtil.getGson().fromJson(primitive, ShopSignStorage.class);
    }

}
