/*
 * This file is a part of project QuickShop, the name is SubCommand_Unlimited.java
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

package com.ghostchu.quickshop.command.subcommand;

import lombok.AllArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.SimpleShopManager;

@AllArgsConstructor
public class SubCommand_Unlimited implements CommandHandler<Player> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        BlockIterator bIt = new BlockIterator(sender, 10);

        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop == null) {
                continue;
            }
            shop.setUnlimited(!shop.isUnlimited());
            shop.update();
            if (shop.isUnlimited()) {
                plugin.text().of(sender, "command.toggle-unlimited.unlimited").send();
                if (plugin.getConfig().getBoolean("unlimited-shop-owner-change")) {
                    plugin.getShopManager().migrateOwnerToUnlimitedShopOwner(shop);
                    plugin.text().of(sender, "unlimited-shop-owner-changed", ((SimpleShopManager) plugin.getShopManager()).getCacheUnlimitedShopAccount().getName()).send();
                }
                return;
            }
            plugin.text().of(sender, "command.toggle-unlimited.limited").send();
            if (plugin.getConfig().getBoolean("unlimited-shop-owner-change")) {
                plugin.text().of(sender, "unlimited-shop-owner-keeped").send();
            }
            return;
        }
        plugin.text().of(sender, "not-looking-at-shop").send();
    }

}
