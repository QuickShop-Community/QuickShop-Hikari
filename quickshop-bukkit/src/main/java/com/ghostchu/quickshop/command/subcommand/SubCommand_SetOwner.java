/*
 *  This file is a part of project QuickShop, the name is SubCommand_SetOwner.java
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
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.ghostchu.quickshop.util.Util.getPlayerList;

@AllArgsConstructor
public class SubCommand_SetOwner implements CommandHandler<Player> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "command.no-owner-given").send();
            return;
        }
        final Shop shop = getLookingShop(sender);

        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }

        if (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.OWNERSHIP_TRANSFER)
                && !plugin.perm().hasPermission(sender, "quickshop.other.setowner")) {
            plugin.text().of(sender, "no-permission").send();
            return;
        }

        Profile newShopOwner = plugin.getPlayerFinder().find(cmdArg[0]);
        if (newShopOwner == null) {
            plugin.text().of(sender, "unknown-player").send();
            return;
        }
        ShopOwnershipTransferEvent event = new ShopOwnershipTransferEvent(shop, shop.getOwner(), newShopOwner.getUniqueId());
        if (event.callCancellableEvent()) {
            return;
        }
        shop.setOwner(newShopOwner.getUniqueId());
        plugin.text().of(sender, "command.new-owner", newShopOwner.getName()).send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length <= 1 ? getPlayerList() : Collections.emptyList();
    }

}
