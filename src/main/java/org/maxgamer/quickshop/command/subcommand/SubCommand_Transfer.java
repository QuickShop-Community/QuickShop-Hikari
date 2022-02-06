/*
 * This file is a part of project QuickShop, the name is SubCommand_Transfer.java
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

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.PlayerFinder;
import org.maxgamer.quickshop.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SubCommand_Transfer implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_Transfer(QuickShop plugin) {
        this.plugin = plugin;
    }


    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length == 1) {
            final OfflinePlayer targetPlayer = PlayerFinder.findOfflinePlayerByName(cmdArg[0]);
            String targetPlayerName = targetPlayer.getName();
            if (targetPlayerName == null) {
                targetPlayerName = "null";
            }
            final UUID targetPlayerUUID = targetPlayer.getUniqueId();
            List<Shop> shopList = plugin.getShopManager().getPlayerAllShops(sender.getUniqueId());
            for (Shop shop : shopList) {
                shop.setOwner(targetPlayerUUID);
            }
            plugin.text().of(sender, "command.transfer-success", Integer.toString(shopList.size()), targetPlayerName).send();
        } else if (cmdArg.length == 2) {
            if (!QuickShop.getPermissionManager().hasPermission(sender, "quickshop.transfer.other")) {
                plugin.text().of(sender, "no-permission").send();
                return;
            }

            final OfflinePlayer fromPlayer = PlayerFinder.findOfflinePlayerByName(cmdArg[0]);
            String fromPlayerName = fromPlayer.getName();
            if (fromPlayerName == null) {
                fromPlayerName = "null";
            }
            //FIXME: Update this when drop 1.15 supports
            final OfflinePlayer targetPlayer = PlayerFinder.findOfflinePlayerByName(cmdArg[1]);
            String targetPlayerName = targetPlayer.getName();
            if (targetPlayerName == null) {
                targetPlayerName = "null";
            }
            final UUID targetPlayerUUID = targetPlayer.getUniqueId();
            List<Shop> shopList = plugin.getShopManager().getPlayerAllShops(fromPlayer.getUniqueId());
            for (Shop shop : shopList) {
                shop.setOwner(targetPlayerUUID);
            }
            plugin.text().of(sender, "command.transfer-success-other", Integer.toString(shopList.size()), fromPlayerName, targetPlayerName).send();

        } else {
            plugin.text().of(sender, "command.wrong-args").send();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length <= 2 ? Util.getPlayerList() : Collections.emptyList();
    }
}
