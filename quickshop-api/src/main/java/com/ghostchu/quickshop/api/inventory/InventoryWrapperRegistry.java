/*
 *  This file is a part of project QuickShop, the name is InventoryWrapperRegistry.java
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

package com.ghostchu.quickshop.api.inventory;

import com.google.common.collect.MapMaker;
import lombok.AllArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@AllArgsConstructor
public class InventoryWrapperRegistry {
    private final Map<String, InventoryWrapperManager> registry = new MapMaker().makeMap();

    public void register(@NotNull Plugin plugin, @NotNull InventoryWrapperManager manager) {
        if (registry.containsKey(plugin.getName())) {
            plugin.getLogger().warning("Nag Author: Plugin " + plugin.getName() + " already have a registered InventoryWrapperManager: "
                    + registry.get(plugin.getName()).getClass().getName() +
                    " but trying register another new manager: " + manager.getClass().getName() + "" +
                    ". This may cause unexpected behavior! Replacing with new instance...");
        }
        registry.put(plugin.getName(), manager);
    }

    public void unregister(@NotNull Plugin plugin) {
        registry.remove(plugin.getName());
    }

    @Nullable
    public InventoryWrapperManager get(String pluginName) {
        return registry.get(pluginName);
    }

    @Nullable
    public String find(InventoryWrapperManager manager) {
        for (Map.Entry<String, InventoryWrapperManager> entry : registry.entrySet()) {
            if (entry.getValue() == manager || entry.getValue().equals(manager)) {
                return entry.getKey();
            }
        }
        return null;
    }


}
