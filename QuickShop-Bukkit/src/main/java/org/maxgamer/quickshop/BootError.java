/*
 * This file is a part of project QuickShop, the name is BootError.java
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

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * BootError class contains print errors on /qs command when plugin failed launched.
 *
 * @author Ghost_chu
 */
@EqualsAndHashCode
@ToString
public class BootError {

    private final String[] errors;

    public BootError(@NotNull Logger logger, @NotNull String... errors) {
        this.errors = errors;
        for (String err : errors) {
            logger.severe(err);
        }
    }

    /**
     * Print the errors. ##################################################### QuickShop is disabled,
     * Please fix errors and restart ..........................
     * #################################################### This one.
     *
     * @param sender The sender you want output the errors.
     */
    public void printErrors(CommandSender sender) { //Do not use any method that not in CraftBukkit
        sender.sendMessage(ChatColor.RED + "#####################################################");
        sender.sendMessage(ChatColor.RED + " QuickShop is disabled, Please fix any errors and restart");
        for (String issue : errors) {
            sender.sendMessage(ChatColor.WHITE + "- " + ChatColor.YELLOW + issue);
        }
        sender.sendMessage(ChatColor.RED + "#####################################################");
    }

    public String[] getErrors() {
        return Arrays.copyOf(errors, errors.length);
    }
}
