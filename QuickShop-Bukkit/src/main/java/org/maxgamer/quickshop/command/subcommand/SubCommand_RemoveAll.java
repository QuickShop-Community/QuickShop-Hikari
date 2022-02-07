/*
 * This file is a part of project QuickShop, the name is SubCommand_RemoveAll.java
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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.logging.container.ShopRemoveLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.maxgamer.quickshop.util.Util.getPlayerList;

@AllArgsConstructor
public class SubCommand_RemoveAll implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length == 1) {
            //copy it first
            List<Shop> tempList = new ArrayList<>(plugin.getShopManager().getAllShops());
            OfflinePlayer shopOwner = null;
            for (OfflinePlayer player : plugin.getServer().getOfflinePlayers()) {
                if (player.getName() != null && player.getName().equalsIgnoreCase(cmdArg[0])) {
                    shopOwner = player;
                    break;
                }
            }
            if (shopOwner == null) {
                plugin.text().of(sender, "unknown-player").send();
                return;
            }

            int i = 0;
            if (!shopOwner.equals(sender)) { //Non-self shop
                if (!sender.hasPermission("quickshop.removeall.other")) {
                    plugin.text().of(sender, "no-permission").send();
                    return;
                }
                for (Shop shop : tempList) {
                    if (shop.getOwner().equals(shopOwner.getUniqueId())) {
                        plugin.logEvent(new ShopRemoveLog(Util.getSenderUniqueId(sender), "Deleting shop " + shop + " as requested by the /qs removeall command.", shop.saveToInfoStorage()));
                        shop.delete();
                        i++;
                    }
                }
            } else { //Self shop
                if (!sender.hasPermission("quickshop.removeall.self")) {
                    plugin.text().of(sender, "no-permission").send();
                    return;
                }
                if (!(sender instanceof OfflinePlayer)) {
                    sender.sendMessage(ChatColor.RED + "This command can't be run by the console!");
                    return;
                }
                for (Shop shop : tempList) {
                    if (shop.getOwner().equals(((OfflinePlayer) sender).getUniqueId())) {
                        plugin.logEvent(new ShopRemoveLog(Util.getSenderUniqueId(sender), "Deleting shop " + shop + " as requested by the /qs removeall command.", shop.saveToInfoStorage()));
                        shop.delete();
                        i++;
                    }
                }
            }
            plugin.text().of(sender, "command.some-shops-removed", Integer.toString(i)).send();
        } else {
            plugin.text().of(sender, "command.no-owner-given").send();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length <= 1 ? getPlayerList() : Collections.emptyList();
    }
}
