/*
 *  This file is a part of project QuickShop, the name is OpenInvCommand.java
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

package com.ghostchu.quickshop.compatibility.openinv;

import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OpenInvCommand implements CommandHandler<Player> {
    private final Main plugin;

    public OpenInvCommand(Main openinv) {
        this.plugin = openinv;
    }

    /**
     * Calling while command executed by specified sender
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param cmdArg       The arguments (/qs create stone will receive stone)
     */
    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        final Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.getApi().getTextManager().of(sender, "not-looking-at-shop").send();
            return;
        }
        if (!shop.getOwner().equals(sender.getUniqueId()) && !sender.hasPermission("quickshop.admin")) {
            plugin.getApi().getTextManager().of(sender, "no-permission").send();
            return;
        }
        if (shop.getInventory() instanceof EnderChestWrapper) {
            shop.setInventory(new BukkitInventoryWrapper((((InventoryHolder) shop.getLocation().getBlock().getState()).getInventory())), plugin.getApi().getInventoryWrapperRegistry().get("QuickShop-Hikari"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.to-chest")));
        } else {
            shop.setInventory(new EnderChestWrapper(shop.getOwner(), plugin.getOpenInv(), plugin), plugin.getManager());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.to-echest")));
        }
    }

    /**
     * Calling while sender trying to tab-complete
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param cmdArg       The arguments (/qs create stone [TAB] will receive stone)
     * @return Candidate list
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return CommandHandler.super.onTabComplete(sender, commandLabel, cmdArg);
    }
}
