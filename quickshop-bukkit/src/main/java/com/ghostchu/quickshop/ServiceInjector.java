/*
 *  This file is a part of project QuickShop, the name is ServiceInjector.java
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

package com.ghostchu.quickshop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ServiceInjector used for "Replaceable Modules" features that allow 3rd party QuickShop addon
 * replace some modules used in QuickShop internal by register as service.
 *
 * @author Ghost_chu
 */
public class ServiceInjector {
    public static @Nullable <T> T getInjectedService(@NotNull Class<T> clazz, T def) {
        @Nullable RegisteredServiceProvider<? extends T> registeredServiceProvider =
                Bukkit.getServicesManager().getRegistration(clazz);
        if (registeredServiceProvider == null) {
            return def;
        } else {
            return registeredServiceProvider.getProvider();
        }
    }
}
