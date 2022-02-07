/*
 * This file is a part of project QuickShop, the name is SubCommand_Update.java
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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.BootError;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.updater.QuickUpdater;
import org.maxgamer.quickshop.util.updater.VersionType;

import java.util.logging.Level;

@AllArgsConstructor
public class SubCommand_Update implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            MsgUtil.sendDirectMessage(sender, ChatColor.YELLOW + "Checking for updates...");

            if (plugin.getUpdateWatcher() == null) {
                MsgUtil.sendDirectMessage(sender, ChatColor.RED + "It seems like the Updater has been disabled.");
                return;
            }
            QuickUpdater updater = plugin.getUpdateWatcher().getUpdater();
            VersionType versionType = updater.getCurrentRunning();
            if (updater.isLatest(versionType)) {
                MsgUtil.sendDirectMessage(sender, ChatColor.GREEN + "You're running the latest version!");
                return;
            }

            if (cmdArg.length == 0 || !"confirm".equalsIgnoreCase(cmdArg[0])) {
                MsgUtil.sendDirectMessage(sender, ChatColor.RED + "You will need to restart the server to complete the update of plugin! Before restarting plugin will stop working!");
                MsgUtil.sendDirectMessage(sender, ChatColor.RED + "Type " + ChatColor.BOLD + "/qs update confirm" + ChatColor.RESET + ChatColor.RED + " to confirm update");
                return;
            }
            MsgUtil.sendDirectMessage(sender, ChatColor.YELLOW + "Downloading update! This may take a while...");
            try {
                updater.install(updater.update(versionType));
            } catch (Exception e) {
                MsgUtil.sendDirectMessage(sender, ChatColor.RED + "Update failed! Please check your console for more information.");
                plugin.getSentryErrorReporter().ignoreThrow();
                plugin.getLogger().log(Level.WARNING, "Failed to update QuickShop because of the following error:", e);
                return;
            }

            MsgUtil.sendDirectMessage(sender,
                    ChatColor.GREEN + "Successful! Please restart your server to apply the updated version!");
            MsgUtil.sendDirectMessage(sender,
                    ChatColor.RED + "Before you restarting the server, QuickShop won't working again.");
            plugin.setupBootError(new BootError(plugin.getLogger(), "Reboot required after update the plugin."), true);

        });
    }

}
