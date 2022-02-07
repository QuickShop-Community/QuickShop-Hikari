/*
 * This file is a part of project QuickShop, the name is DisplayItemPersistentDataType.java
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

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PreviewGuiPersistentDataType
        implements PersistentDataType<String, UUID> {
    static final PreviewGuiPersistentDataType INSTANCE = new PreviewGuiPersistentDataType();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<UUID> getComplexType() {
        return UUID.class;
    }

    @NotNull
    @Override
    public String toPrimitive(
            @NotNull UUID complex, @NotNull PersistentDataAdapterContext context) {
        return complex.toString();
    }

    @NotNull
    @Override
    public UUID fromPrimitive(
            @NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        try {
            return UUID.fromString(primitive);
        } catch (Exception exception) {
            return new UUID(0L, 0L);
        }
    }

}
