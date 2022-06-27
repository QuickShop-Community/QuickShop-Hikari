/*
 *  This file is a part of project QuickShop, the name is SubCommand_Item.java
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
import com.ghostchu.quickshop.api.shop.PriceLimiter;
import com.ghostchu.quickshop.api.shop.PriceLimiterCheckResult;
import com.ghostchu.quickshop.api.shop.PriceLimiterStatus;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class SubCommand_Item implements CommandHandler<Player> {
    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        // Loop through every block they're looking at upto 10 blocks away
        final Shop shop = getLookingShop(sender);
        if (shop != null) {
            if (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_ITEM)
                    && !plugin.perm().hasPermission(sender, "quickshop.other.item")) {
                plugin.text().of(sender, "not-managed-shop").send();
                return;
            }
            ItemStack itemStack = sender.getInventory().getItemInMainHand().clone();
            if (itemStack.getType() == Material.AIR) {
                plugin.text().of(sender, "command.no-trade-item").send();
                return;
            }
            if (Util.isBlacklisted(itemStack) && !plugin.perm().hasPermission(sender, "quickshop.bypass." + itemStack.getType().name())) {
                plugin.text().of(sender, "blacklisted-item").send();
                return;
            }
            if (!plugin.isAllowStack() && !plugin.perm().hasPermission(sender, "quickshop.create.stacks")) {
                itemStack.setAmount(1);
            }
            PriceLimiter limiter = plugin.getShopManager().getPriceLimiter();
            PriceLimiterCheckResult checkResult = limiter.check(sender, itemStack, shop.getCurrency(), shop.getPrice());
            if (checkResult.getStatus() != PriceLimiterStatus.PASS) {
                plugin.text().of(sender, "restricted-prices", MsgUtil.getTranslateText(shop.getItem()),
                        Component.text(checkResult.getMin()),
                        Component.text(checkResult.getMax())).send();
                return;
            }
            shop.setItem(itemStack);
            plugin.text().of(sender, "command.trade-item-now", shop.getItem().getAmount(), Util.getItemStackName(shop.getItem())).send();
            return;
        }
        plugin.text().of(sender, "not-looking-at-shop").send();
    }

}
