/*
 *  This file is a part of project QuickShop, the name is SubCommand_SuperCreate.java
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
import com.ghostchu.quickshop.api.shop.ShopAction;
import com.ghostchu.quickshop.shop.SimpleInfo;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class SubCommand_SuperCreate implements CommandHandler<Player> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        ItemStack item = sender.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            plugin.text().of(sender, "no-anythings-in-your-hand").send();
            return;
        }

        final BlockIterator bIt = new BlockIterator(sender, 10);

        while (bIt.hasNext()) {
            final Block b = bIt.next();

            if (!Util.canBeShop(b)) {
                continue;
            }

            // Send creation menu.
            final SimpleInfo info = new SimpleInfo(b.getLocation(), ShopAction.CREATE_SELL, sender.getInventory().getItemInMainHand(), b.getRelative(sender.getFacing().getOppositeFace()), true);

            plugin.getShopManager().getActions().put(sender.getUniqueId(), info);
            plugin.text().of(sender, "how-much-to-trade-for", MsgUtil.getTranslateText(info.getItem()), plugin.isAllowStack() && QuickShop.getPermissionManager().hasPermission(sender, "quickshop.create.stacks") ? item.getAmount() : 1).send();
            return;
        }
        plugin.text().of(sender, "not-looking-at-shop").send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length == 1 ? Collections.singletonList(LegacyComponentSerializer.legacySection().serialize(QuickShop.getInstance().text().of(sender, "tabcomplete.amount").forLocale())) : Collections.emptyList();
    }

}
