/*
 * This file is a part of project QuickShop, the name is BuiltInSolution.java
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

package org.maxgamer.quickshop;

import org.bukkit.ChatColor;

/**
 * A class to check known issues that cause plugin boot failure.
 */
public class BuiltInSolution {

    /**
     * Call îf database failed to load. This checks the failure reason.
     *
     * @return The error reason.
     */
    public static BootError databaseError() {
        return new BootError(QuickShop.getInstance().getLogger(),
                "Error connecting to the database!",
                "Please make sure your database service is running.",
                "and check the configuration in your config.yml");
    }

    /**
     * Call îf economy system failed to load. This checks the failure reason.
     *
     * @return The error reason.
     */
    public static BootError econError() {
        // Check if Vault is installed
        if (QuickShop.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
            // Vault is not installed
            return new BootError(QuickShop.getInstance().getLogger(),
                    "Vault is not installed or loaded!",
                    "Please make sure Vault is installed.");
        }
        // Vault is installed
        if (QuickShop.getInstance().getServer().getPluginManager().getPlugin("CMI") != null) {
            // Found possible incompatible plugin
            return new BootError(QuickShop.getInstance().getLogger(),
                    "No Economy plugin detected! Please make sure that you have a compatible economy",
                    "plugin installed that is hooked into Vault and loads before QuickShop.",
                    ChatColor.YELLOW + "Incompatibility detected: CMI Installed",
                    "The use of the CMI Edition of Vault might fix this.");
        }

        return new BootError(QuickShop.getInstance().getLogger(),
                "No Economy plugin detected! Please make sure that you have a",
                "compatible economy plugin installed to get Vault working.");
    }

}
