/*
 *  This file is a part of project QuickShop, the name is SubCommand_Transfer.java
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
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            Profile profile = plugin.getPlayerFinder().find(cmdArg[0]);
            if (profile == null) {
                plugin.text().of(sender, "unknown-player").send();
                return;
            }
            UUID targetPlayerUUID = profile.getUniqueId();
            List<Shop> shopList = plugin.getShopManager().getPlayerAllShops(sender.getUniqueId());
            for (Shop shop : shopList) {
                shop.setOwner(targetPlayerUUID);
            }
            plugin.text().of(sender, "command.transfer-success", shopList.size(), profile.getName()).send();
        } else if (cmdArg.length == 2) {
            if (!QuickShop.getPermissionManager().hasPermission(sender, "quickshop.transfer.other")) {
                plugin.text().of(sender, "no-permission").send();
                return;
            }
            Profile fromPlayer = plugin.getPlayerFinder().find(cmdArg[0]);
            Profile targetPlayer = plugin.getPlayerFinder().find(cmdArg[1]);
            if (fromPlayer == null) {
                plugin.text().of(sender, "unknown-player", "fromPlayer").send();
                return;
            }
            if (targetPlayer == null) {
                plugin.text().of(sender, "unknown-player", "targetPlayer").send();
                return;
            }
            List<Shop> shopList = plugin.getShopManager().getPlayerAllShops(fromPlayer.getUniqueId());
            for (Shop shop : shopList) {
                shop.setOwner(targetPlayer.getUniqueId());
            }
            plugin.text().of(sender, "command.transfer-success-other", shopList.size(), fromPlayer.getName(), targetPlayer.getName()).send();
        } else {
            plugin.text().of(sender, "command.wrong-args").send();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length <= 2 ? Util.getPlayerList() : Collections.emptyList();
    }
}
