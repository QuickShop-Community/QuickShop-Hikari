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
import com.ghostchu.quickshop.api.event.ShopOwnershipTransferEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SubCommand_Transfer implements CommandHandler<Player> {

    private final QuickShop plugin;
    private final Cache<UUID, PendingTransferTask> taskCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build();

    public SubCommand_Transfer(QuickShop plugin) {
        this.plugin = plugin;
    }


    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "command.wrong-args").send();
            return;
        }
        if (cmdArg.length == 1) {
            switch (cmdArg[0]) {
                case "accept" -> {
                    PendingTransferTask task = taskCache.getIfPresent(sender.getUniqueId());
                    taskCache.invalidate(sender.getUniqueId());
                    if (task == null) {
                        plugin.text().of(sender, "transfer-no-pending-operation").send();
                        return;
                    }
                    task.commit(true);
                }
                case "reject" -> {
                    PendingTransferTask task = taskCache.getIfPresent(sender.getUniqueId());
                    taskCache.invalidate(sender.getUniqueId());
                    if (task == null) {
                        plugin.text().of(sender, "transfer-no-pending-operation").send();
                        return;
                    }
                    task.cancel(true);
                }
                default -> {
                    Profile profile = plugin.getPlayerFinder().find(cmdArg[0]);
                    if (profile == null) {
                        plugin.text().of(sender, "unknown-player").send();
                        return;
                    }
                    Player receiver = Bukkit.getPlayer(profile.getUniqueId());
                    if (receiver == null) {
                        plugin.text().of(sender, "player-offline", profile.getName()).send();
                        return;
                    }
                    UUID targetPlayerUUID = profile.getUniqueId();
                    List<Shop> shopList = plugin.getShopManager().getPlayerAllShops(sender.getUniqueId());
                    PendingTransferTask task = new PendingTransferTask(sender.getUniqueId(), targetPlayerUUID, shopList);
                    taskCache.put(targetPlayerUUID, task);
                    plugin.text().of(sender, "transfer-sent", profile.getName()).send();
                    plugin.text().of(receiver, "transfer-request", sender.getName()).send();
                    plugin.text().of(receiver, "transfer-ask", 60).send();
                    return;
                }
            }
        }
        if (cmdArg.length == 2) {
            if (!plugin.perm().hasPermission(sender, "quickshop.transfer.other")) {
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
            PendingTransferTask task = new PendingTransferTask(fromPlayer.getUniqueId(), targetPlayer.getUniqueId(), shopList);
            task.commit(false);
            plugin.text().of(sender, "command.transfer-success-other", shopList.size(), fromPlayer.getName(), targetPlayer.getName()).send();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length <= 2 ? Util.getPlayerList() : Collections.emptyList();
    }

    @AllArgsConstructor
    @Data
    static class PendingTransferTask {
        private final UUID from;
        private final UUID to;
        private final List<Shop> shops;

        public void commit(boolean sendMessage) {
            for (Shop shop : shops) {
                ShopOwnershipTransferEvent event = new ShopOwnershipTransferEvent(shop, shop.getOwner(), to);
                if (event.callCancellableEvent()) {
                    continue;
                }
                shop.setOwner(to);
            }
            if (sendMessage) {
                Profile fromPlayerProfile = QuickShop.getInstance().getPlayerFinder().find(from);
                Profile toPlayerProfile = QuickShop.getInstance().getPlayerFinder().find(to);
                if (toPlayerProfile != null) {
                    QuickShop.getInstance().text().of(from, "transfer-accepted-fromside", toPlayerProfile.getName()).send();
                }
                if (fromPlayerProfile != null) {
                    QuickShop.getInstance().text().of(to, "transfer-accepted-toside", fromPlayerProfile.getName()).send();
                }
            }
        }

        public void cancel(boolean sendMessage) {
            if (sendMessage) {
                Profile fromPlayerProfile = QuickShop.getInstance().getPlayerFinder().find(from);
                Profile toPlayerProfile = QuickShop.getInstance().getPlayerFinder().find(to);
                Player fromPlayer = Bukkit.getPlayer(from);
                Player toPlayer = Bukkit.getPlayer(to);
                if (fromPlayer != null && toPlayerProfile != null) {
                    QuickShop.getInstance().text().of(fromPlayer, "transfer-rejected-fromside", toPlayerProfile.getName()).send();
                }
                if (toPlayer != null && fromPlayerProfile != null) {
                    QuickShop.getInstance().text().of(toPlayer, "transfer-rejected-toside", fromPlayerProfile.getName()).send();
                }
            }
        }
    }
}
