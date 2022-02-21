///*
// *  This file is a part of project QuickShop, the name is SubCommand_Update.java
// *  Copyright (C) Ghost_chu and contributors
// *
// *  This program is free software: you can redistribute it and/or modify it
// *  under the terms of the GNU General Public License as published by the
// *  Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful, but WITHOUT
// *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *  for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program. If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package com.ghostchu.quickshop.command.subcommand;
//
//import lombok.AllArgsConstructor;
//import net.kyori.adventure.text.Component;
//import net.kyori.adventure.text.format.NamedTextColor;
//import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
//import org.bukkit.ChatColor;
//import org.bukkit.command.CommandSender;
//import org.jetbrains.annotations.NotNull;
//import com.ghostchu.quickshop.QuickShop;
//import com.ghostchu.quickshop.api.command.CommandHandler;
//import com.ghostchu.quickshop.util.MsgUtil;
//import com.ghostchu.quickshop.util.updater.QuickUpdater;
//import com.ghostchu.quickshop.util.updater.VersionType;
//
//import java.util.logging.Level;
//
//@AllArgsConstructor
//public class SubCommand_Update implements CommandHandler<CommandSender> {
//
//    private final QuickShop plugin;
//
//    @Override
//    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
//        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
//            MsgUtil.sendDirectMessage(sender, Component.text("Checking for updates...").color(NamedTextColor.YELLOW));
//
//            if (plugin.getUpdateWatcher() == null) {
//                MsgUtil.sendDirectMessage(sender, Component.text( "It seems like the Updater has been disabled.").color(NamedTextColor.RED));
//                return;
//            }
//            QuickUpdater updater = plugin.getUpdateWatcher().getUpdater();
//            VersionType versionType = updater.getCurrentRunning();
//            if (updater.isLatest(versionType)) {
//                MsgUtil.sendDirectMessage(sender, Component.text( "You're running the latest version!").color(NamedTextColor.GREEN));
//                return;
//            }
//
//            if (cmdArg.length == 0 || !"confirm".equalsIgnoreCase(cmdArg[0])) {
//                MsgUtil.sendDirectMessage(sender, Component.text( "You will need to restart the server to complete the update of plugin! Before restarting plugin will stop working!").color(NamedTextColor.RED));
//                MsgUtil.sendDirectMessage(sender, LegacyComponentSerializer.legacySection().deserialize( ChatColor.RED + "Type " + ChatColor.BOLD + "/qs update confirm" + ChatColor.RESET + ChatColor.RED + " to confirm update"));
//                return;
//            }
//            MsgUtil.sendDirectMessage(sender, Component.text( "Downloading update! This may take a while...").color(NamedTextColor.YELLOW));
//            try {
//                updater.install(updater.update(versionType));
//            } catch (Exception e) {
//                MsgUtil.sendDirectMessage(sender, Component.text( "Update failed! Please check your console for more information.").color(NamedTextColor.RED));
//                plugin.getSentryErrorReporter().ignoreThrow();
//                plugin.getLogger().log(Level.WARNING, "Failed to update QuickShop because of the following error:", e);
//                return;
//            }
//            MsgUtil.sendDirectMessage(sender, Component.text( "Successful! Please restart your server to apply the updated version!").color(NamedTextColor.GREEN));
//            MsgUtil.sendDirectMessage(sender, Component.text( "Please restart the server as soon as possible.").color(NamedTextColor.GREEN));
//
//        });
//    }
//
//}
