/*
 * This file is a part of project QuickShop, the name is CollectType.java
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

package org.maxgamer.quickshop.util.collector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum CollectType {
    QUICKSHOP("quickshop"),
    SYSTEM("system"),
    PLATFORM("platform"),
    MODULES("replaceable_module"),
    SERVICES("services"),
    SHOPS_IN_WORLD("shops_in_world"),
    PLUGINS("plugins"),
    CONFIG("config"),
    SERVER_CONFIG("server_config"),
    LANGUAGE("i18n"),
    LOGS("logs"),
    SHOPS("shops");
    private final String field;

    CollectType(String field) {
        this.field = field;
    }

    @NotNull
    public String getField() {
        return field;
    }

    @Nullable
    public CollectType fromField(@NotNull String field) {
        for (CollectType type : CollectType.values()) {
            if (type.field.equalsIgnoreCase(field)) {
                return type;
            }
        }
        return null;
    }
}
