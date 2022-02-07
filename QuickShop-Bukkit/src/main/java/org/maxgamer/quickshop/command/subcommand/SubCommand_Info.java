/*
 * This file is a part of project QuickShop, the name is SubCommand_Info.java
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
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.api.shop.ShopChunk;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.Map;

@AllArgsConstructor
public class SubCommand_Info implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        int buying, selling, chunks, worlds;
        buying = 0;
        selling = 0;
        chunks = 0;
        worlds = 0;
        int nostock = 0;

        for (Map<ShopChunk, Map<Location, Shop>> inWorld :
                plugin.getShopManager().getShops().values()) {
            worlds++;

            for (Map<Location, Shop> inChunk : inWorld.values()) {
                chunks++;
                for (Shop shop : inChunk.values()) {
                    if (shop.isBuying()) {
                        buying++;
                    } else if (shop.isSelling()) {
                        selling++;
                    }
                    if (shop.isSelling() && shop.isLoaded() && shop.getRemainingStock() == 0) {
                        nostock++;
                    }
                }
            }
        }

        MsgUtil.sendDirectMessage(sender, ChatColor.RED + "QuickShop Statistics...");
        MsgUtil.sendDirectMessage(sender, ChatColor.GREEN + "Server UniqueID: " + plugin.getServerUniqueID());
        MsgUtil.sendDirectMessage(sender,
                ChatColor.GREEN
                        + ""
                        + (buying + selling)
                        + " shops in "
                        + chunks
                        + " chunks spread over "
                        + worlds
                        + " worlds.");
        MsgUtil.sendDirectMessage(sender,
                ChatColor.GREEN
                        + ""
                        + nostock
                        + " out-of-stock loaded shops (excluding doubles) which will be removed by /qs clean.");
        MsgUtil.sendDirectMessage(sender, ChatColor.GREEN + "QuickShop " + QuickShop.getVersion());
    }


}
