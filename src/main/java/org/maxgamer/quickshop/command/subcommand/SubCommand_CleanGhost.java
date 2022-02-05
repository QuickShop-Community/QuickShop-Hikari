/*
 * This file is a part of project QuickShop, the name is SubCommand_CleanGhost.java
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
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.logging.container.ShopRemoveLog;

@AllArgsConstructor
public class SubCommand_CleanGhost implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendDirectMessage(sender,
                    ChatColor.YELLOW
                            + "This command will purge all shops that: have corrupted data / are created in disallowed or unloaded worlds / trade with blacklisted items! Please make sure you have a backup of your shops data! Use /qs cleanghost to confirm the purge.");
            return;
        }

        if (!"confirm".equalsIgnoreCase(cmdArg[0])) {
            MsgUtil.sendDirectMessage(sender,
                    ChatColor.YELLOW
                            + "This command will purge all shops that: have corrupted data / are created in disallowed or unloaded worlds / trade with blacklisted items! Please make sure you have a backup of your shops data! Use /qs cleanghost to confirm the purge.");
            return;
        }

        MsgUtil.sendDirectMessage(sender,
                ChatColor.GREEN
                        + "Starting to check for ghost shops (missing container blocks). All non-existing shops will be removed...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            MsgUtil.sendDirectMessage(sender, ChatColor.GREEN + "Starting async thread, please wait...");
         //   Util.backupDatabase(); // Already warn the user, don't care about backup result.
            for (Shop shop : plugin.getShopManager().getAllShops()) {
                MsgUtil.sendDirectMessage(sender,
                        ChatColor.GRAY
                                + "Checking the shop "
                                + shop
                                + " metadata and location block state...");
                if (shop == null) {
                    continue; // WTF
                }
                if (shop.getItem().getType() == Material.AIR) {
                    MsgUtil.sendDirectMessage(sender,
                            ChatColor.YELLOW + "Deleting shop " + shop + " because of corrupted item data.");
                    plugin.logEvent(new ShopRemoveLog(Util.getSenderUniqueId(sender), "/qs cleanghost command", shop.saveToInfoStorage()));
                    Util.mainThreadRun(shop::delete);
                    continue;
                }
                if (!shop.getLocation().isWorldLoaded()) {
                    MsgUtil.sendDirectMessage(sender,
                            ChatColor.YELLOW + "Deleting shop " + shop + " because the its world is not loaded.");
                    Util.mainThreadRun(shop::delete);
                    plugin.logEvent(new ShopRemoveLog(Util.getSenderUniqueId(sender), "/qs cleanghost command", shop.saveToInfoStorage()));
                    continue;
                }
                //noinspection ConstantConditions
                if (shop.getOwner() == null) {
                    MsgUtil.sendDirectMessage(sender,
                            ChatColor.YELLOW + "Deleting shop " + shop + " because of corrupted owner data.");
                    Util.mainThreadRun(shop::delete);
                    plugin.logEvent(new ShopRemoveLog(Util.getSenderUniqueId(sender), "/qs cleanghost command", shop.saveToInfoStorage()));
                    continue;
                }
                // Shop exist check
                Util.mainThreadRun(() -> {
                    Util.debugLog(
                            "Posted to main server thread to continue accessing Bukkit API for shop "
                                    + shop);
                    if (!Util.canBeShop(shop.getLocation().getBlock())) {
                        MsgUtil.sendDirectMessage(sender,
                                ChatColor.YELLOW
                                        + "Deleting shop "
                                        + shop
                                        + " because it is no longer on the target location or it is not allowed to create shops in this location.");
                        shop.delete();
                    }
                }); // Post to server main thread to check.
                try {
                    Thread.sleep(20); // Have a rest, don't blow up the main server thread.
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            MsgUtil.sendDirectMessage(sender, ChatColor.GREEN + "All shops have been checked!");
        });
    }


}
