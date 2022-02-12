/*
 * This file is a part of project QuickShop, the name is VaultPermissionProvider.java
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

import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.permission.PermissionProvider;
import org.maxgamer.quickshop.api.permission.ProviderIsEmptyException;

@Deprecated
public class VaultPermissionProvider implements PermissionProvider {
    private final Permission api;

    @Deprecated
    public VaultPermissionProvider() {
        RegisteredServiceProvider<Permission> rsp =
                QuickShop.getInstance().getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            throw new ProviderIsEmptyException(getName());
        }
        api = rsp.getProvider();
    }

    /**
     * Test the sender has special permission
     *
     * @param sender     CommandSender
     * @param permission The permission want to check
     * @return hasPermission
     */
    @Override
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        return api.has(sender, permission);
    }

    /**
     * Get permission provider name
     *
     * @return The name of permission provider
     */
    @Override
    public @NotNull String getName() {
        return "Vault";
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
        if (sender instanceof Server) {
            return new PermissionInformationContainer(sender, permission, null, "User is Console");
        }
        OfflinePlayer offlinePlayer = (OfflinePlayer) sender;
        return new PermissionInformationContainer(
                sender, permission, api.getPrimaryGroup(null, offlinePlayer), null);
    }

}
