/*
 * This file is a part of project QuickShop, the name is BukkitPermsProvider.java
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

package org.maxgamer.quickshop.permission;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.api.permission.PermissionProvider;

/**
 * A simple impl for PermissionProvider
 *
 * @author Ghost_chu
 */
public class BukkitPermsProvider implements PermissionProvider {

    @Override
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public @NotNull String getName() {
        return "Bukkit";
    }

    /**
     * Get the debug infos in provider
     *
     * @param sender     CommandSender
     * @param permission The permission want to check
     * @return Debug Infos
     */
    public @NotNull PermissionInformationContainer getDebugInfo(
            @NotNull CommandSender sender, @NotNull String permission) {
        return new PermissionInformationContainer(sender, permission, null, null);
    }

}
