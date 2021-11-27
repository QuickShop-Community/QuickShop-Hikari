/*
 * This file is a part of project QuickShop, the name is SubCommand_Refill.java
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
import org.apache.commons.lang3.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class SubCommand_Refill implements CommandHandler<Player> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "command.no-amount-given").send();
            return;
        }

        int add;

        final BlockIterator bIt = new BlockIterator(sender, 10);

        if (!bIt.hasNext()) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop == null) {
                continue;
            }
            if (StringUtils.isNumeric(cmdArg[0])) {
                add = Integer.parseInt(cmdArg[0]);
            } else {
                if (cmdArg[0].equals(plugin.getConfiguration().getString("shop.word-for-trade-all-items"))) {
                    add = shop.getRemainingSpace();
                } else {
                    plugin.text().of(sender, "thats-not-a-number").send();
                    return;
                }
            }
            shop.add(shop.getItem(), add);
            plugin.text().of(sender, "refill-success").send();
            return;
        }
        plugin.text().of(sender, "not-looking-at-shop").send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length == 1 ? Collections.singletonList(QuickShop.getInstance().text().of(sender, "tabcomplete.amount").forLocale()) : Collections.emptyList();
    }

}
