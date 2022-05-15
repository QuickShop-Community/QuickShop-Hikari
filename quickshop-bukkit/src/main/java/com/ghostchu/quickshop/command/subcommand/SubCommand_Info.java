/*
 *  This file is a part of project QuickShop, the name is SubCommand_Info.java
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

package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.util.MsgUtil;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

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

        MsgUtil.sendDirectMessage(sender, Component.text("QuickShop Statistics...").color(NamedTextColor.GOLD));
        MsgUtil.sendDirectMessage(sender, Component.text("Server UniqueId: " + plugin.getServerUniqueID()).color(NamedTextColor.GREEN));
        MsgUtil.sendDirectMessage(sender, Component.text(""
                + (buying + selling)
                + " shops in "
                + chunks
                + " chunks spread over "
                + worlds
                + " worlds.").color(NamedTextColor.GREEN));
        MsgUtil.sendDirectMessage(sender, Component.text(""
                + nostock
                + " out-of-stock loaded shops (excluding doubles) which will be removed by /qs clean.").color(NamedTextColor.GREEN));
        MsgUtil.sendDirectMessage(sender, Component.text("QuickShop " + QuickShop.getVersion()).color(NamedTextColor.GREEN));
    }


}
