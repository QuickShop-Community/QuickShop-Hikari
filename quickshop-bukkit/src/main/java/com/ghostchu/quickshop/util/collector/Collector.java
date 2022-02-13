/*
 * This file is a part of project QuickShop, the name is Collector.java
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

package com.ghostchu.quickshop.util.collector;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.collector.adapter.CollectorAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

@Data
public class Collector {
    private CollectorAdapter adapter = new CollectorAdapter();
    private Map<CollectType, Map<?, ?>> collectInformation = new LinkedHashMap<>();

    public Collector(@NotNull QuickShop plugin) {
        for (CollectType value : CollectType.values()) {
            collectInformation.put(value, bake(value, plugin));
        }
    }

    @NotNull
    private Map<?, ?> bake(@NotNull CollectType field, @NotNull QuickShop plugin) {
        for (Method declaredMethod : adapter.getClass().getDeclaredMethods()) {
            CollectResolver resolver = declaredMethod.getAnnotation(CollectResolver.class);
            if (resolver == null) {
                continue;
            }
            if (!resolver.field().equals(field)) {
                continue;
            }
            try {
                Map<?, ?> map = (Map<?, ?>) declaredMethod.invoke(adapter, plugin);
                if (map == null) {
                    map = new HashMap<>();
                }
                return map;
            } catch (IllegalAccessException | ClassCastException | InvocationTargetException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to resolve the field " + field + " when collecting data. Please report to author.", e);
            }
        }
        return new HashMap<>();
    }

}