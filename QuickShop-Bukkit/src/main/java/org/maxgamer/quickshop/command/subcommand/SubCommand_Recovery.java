/*
 * This file is a part of project QuickShop, the name is SubCommand_Recovery.java
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

package org.maxgamer.quickshop.command.subcommand;

import lombok.AllArgsConstructor;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

import java.io.File;
import java.util.logging.Level;

@AllArgsConstructor
public class SubCommand_Recovery implements CommandHandler<ConsoleCommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull ConsoleCommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        File file = new File(plugin.getDataFolder(), "recovery.txt");
        if (!file.exists()) {
            MsgUtil.sendDirectMessage(sender, "recovery.txt doesn't exist! Do not execute this command unless you know what are you doing.");
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                //Util.backupDatabase();
                plugin.getShopLoader().recoverFromFile(Util.readToString(file));
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to recover the data because of the following error:", e);
            }
        });

    }

}
