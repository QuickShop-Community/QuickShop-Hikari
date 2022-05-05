/*
 *  This file is a part of project QuickShop, the name is SubCommand_Create.java
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
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SubCommand_Create implements CommandHandler<Player> {

    private final QuickShop plugin;


    public SubCommand_Create(@NotNull QuickShop plugin) {
        this.plugin = plugin;
    }

    @Nullable
    private Material matchMaterial(String itemName) {
        itemName = itemName.toUpperCase();
        itemName = itemName.replace(" ", "_");
        Material material = Material.matchMaterial(itemName);
        if (isValidMaterial(material)) {
            return material;
        }
        return null;
    }

    private boolean isValidMaterial(@Nullable Material material) {
        return material != null && !material.isAir();
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        BlockIterator bIt = new BlockIterator(sender, 10);
        ItemStack item;
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "command.wrong-args").send();
            return;
        } else if (cmdArg.length == 1) {
            item = sender.getInventory().getItemInMainHand();
            if (item.getType().isAir()) {
                plugin.text().of(sender, "no-anythings-in-your-hand").send();
                return;
            }
        } else {
            Material material = matchMaterial(cmdArg[1]);
            if (material == null) {
                plugin.text().of(sender, "item-not-exist",cmdArg[1]).send();
                return;
            }
            if (cmdArg.length > 2 && QuickShop.getPermissionManager().hasPermission(sender, "quickshop.create.stack") && plugin.isAllowStack()) {
                try {
                    int amount = Integer.parseInt(cmdArg[2]);
                    if (amount < 1) {
                        amount = 1;
                    }
                    item = new ItemStack(material, amount);
                } catch (NumberFormatException e) {
                    item = new ItemStack(material, 1);
                }
            } else {
                item = new ItemStack(material, 1);
            }
        }
        Log.debug("Pending task for material: " + item);

        String price = cmdArg[0];

        while (bIt.hasNext()) {
            final Block b = bIt.next();
            if (!Util.canBeShop(b)) {
                continue;
            }
            // Send creation menu.
            plugin.getShopManager().getActions().put(sender.getUniqueId(),
                    new SimpleInfo(b.getLocation(), ShopAction.CREATE_SELL, item, b.getRelative(sender.getFacing().getOppositeFace()), false));
            plugin.getShopManager().handleChat(sender, price);
            return;
        }
        plugin.text().of(sender, "not-looking-at-valid-shop-block").send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length == 1) {
            return Collections.singletonList(LegacyComponentSerializer.legacySection().serialize(plugin.text().of(sender, "tabcomplete.price").forLocale()));
        }
        if (sender.getInventory().getItemInMainHand().getType().isAir()) {
            if (cmdArg.length == 2) {
                return Collections.singletonList(LegacyComponentSerializer.legacySection().serialize(plugin.text().of(sender, "tabcomplete.item").forLocale()));
            }
            if (cmdArg.length == 3) {
                return Collections.singletonList(LegacyComponentSerializer.legacySection().serialize(plugin.text().of(sender, "tabcomplete.amount").forLocale()));
            }
        }
        return Collections.emptyList();
    }

}
