/*
 *  This file is a part of project QuickShop, the name is PermissionManager.java
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

package com.ghostchu.quickshop.permission;

import com.ghostchu.quickshop.util.logger.Log;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.permission.PermissionProvider;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;

import java.util.logging.Level;

@Getter
public class PermissionManager {
    private final QuickShop plugin;

    private final PermissionProvider provider;

    /**
     * The manager to call permission providers
     *
     * @param plugin Instance
     */
    public PermissionManager(QuickShop plugin) {
        this.plugin = plugin;
        provider = new BukkitPermsProvider();
        plugin.getLogger().info("Selected permission provider: " + provider.getName());
    }

    /**
     * Check the permission for sender
     *
     * @param sender     The CommandSender you want check
     * @param permission The permission node wait to check
     * @return The result of check
     */
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        try {
            boolean result = provider.hasPermission(sender, permission);
            if (Util.isDevMode()) {
                try {
                    Log.debug("Node: [" + permission + "]; Result: [" + result + "]; Sender: [" + sender.getName() + "]");
                } catch (Exception th) {
                    Log.debug("Exception threw when getting debug messages.");
                    MsgUtil.debugStackTrace(th.getStackTrace());
                }
            }
            return result;
        } catch (Exception th) {
            plugin.getLogger().log(Level.WARNING, "Failed to processing permission response, This might or not a bug, we not sure, but you can report to both permission provider plugin author or QuickShop devs about this error", th);
            return false;
        }
    }

}
