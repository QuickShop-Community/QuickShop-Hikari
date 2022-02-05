/*
 * This file is a part of project QuickShop, the name is SubCommand_Create.java
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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.shop.ShopAction;
import org.maxgamer.quickshop.shop.SimpleInfo;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.holder.Result;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SubCommand_Create implements CommandHandler<Player> {

    private final QuickShop plugin;


    public SubCommand_Create(@NotNull QuickShop plugin) {
        this.plugin = plugin;
    }

    @Nullable
    private Material matchMaterial(String itemName) {
        Material material = Material.matchMaterial(itemName);
        if (isValidMaterial(material)) {
            return material;
        }
        ConfigurationSection section = MsgUtil.getItemi18n().getConfigurationSection("itemi18n");
        for (String itemKey : Objects.requireNonNull(section).getKeys(false)) {
            if (itemName.equalsIgnoreCase(section.getString(itemKey))) {
                material = Material.matchMaterial(itemKey);
                break;
            }
        }
        if (!isValidMaterial(material)) {
            return null;
        }
        return material;
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
                plugin.text().of(sender, "item-not-exist", cmdArg[1]).send();
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
        Util.debugLog("Pending task for material: " + item);

        String price = cmdArg[0];

        while (bIt.hasNext()) {
            final Block b = bIt.next();

            if (!Util.canBeShop(b)) {
                continue;
            }

            Result result = plugin.getPermissionChecker().canBuild(sender, b);
            if (!result.isSuccess()) {
                plugin.text().of(sender, "3rd-plugin-build-check-failed", result.getMessage()).send();
                Util.debugLog("Failed to create shop because the protection check has failed! Reason:" + result.getMessage());
                return;
            }

            BlockFace blockFace = sender.getFacing();

            if (!plugin.getShopManager().canBuildShop(sender, b, blockFace)) {
                // As of the new checking system, most plugins will tell the
                // player why they can't create a shop there.
                // So telling them a message would cause spam etc.
                Util.debugLog("Util report you can't build shop there.");
                return;
            }

            if (Util.isDoubleChest(b.getBlockData())
                    && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.create.double")) {
                plugin.text().of(sender, "no-double-chests").send();
                return;
            }

            if (Util.isBlacklisted(item)
                    && !QuickShop.getPermissionManager()
                    .hasPermission(sender, "quickshop.bypass." + item.getType().name())) {
                plugin.text().of(sender, "blacklisted-item").send();
                return;
            }

            // Send creation menu.
            plugin.getShopManager().getActions().put(sender.getUniqueId(),
                    new SimpleInfo(b.getLocation(), ShopAction.CREATE, item, b.getRelative(sender.getFacing().getOppositeFace()), false));
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
            return Collections.singletonList(plugin.text().of(sender, "tabcomplete.price").forLocale());
        }
        if (sender.getInventory().getItemInMainHand().getType().isAir()) {
            if (cmdArg.length == 2) {
                return Collections.singletonList(plugin.text().of(sender, "tabcomplete.item").forLocale());
            }
            if (cmdArg.length == 3) {
                return Collections.singletonList(plugin.text().of(sender, "tabcomplete.amount").forLocale());
            }
        }
        return Collections.emptyList();
    }

}
