/*
 * This file is a part of project QuickShop, the name is SubCommand_TaxAccount.java
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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.PlayerFinder;
import org.maxgamer.quickshop.util.Util;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class SubCommand_TaxAccount implements CommandHandler<Player> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        BlockIterator bIt = new BlockIterator(sender, 10);

        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                if (cmdArg.length < 1) {
                    shop.setTaxAccount(null);
                    plugin.text().of(sender, "taxaccount-unset").send();
                    return;
                }
                if (Util.isUUID(cmdArg[0])) {
                    shop.setTaxAccount(UUID.fromString(cmdArg[0]));
                } else {
                    shop.setTaxAccount(PlayerFinder.findUUIDByName(cmdArg[0]));
                }
                plugin.text().of(sender, "taxaccount-set", cmdArg[0]).send();
                return;
            }
        }
        plugin.text().of(sender, "not-looking-at-shop").send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return null;
    }

}
