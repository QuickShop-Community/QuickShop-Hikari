/*
 *  This file is a part of project QuickShop, the name is SubCommand_Size.java
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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class SubCommand_Size implements CommandHandler<Player> {
    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "command.bulk-size-not-set").send();
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(cmdArg[0]);
        } catch (NumberFormatException e) {
            plugin.text().of(sender, "not-a-integer", cmdArg[0]).send();
            return;
        }
        final Shop shop = getLookingShop(sender);
        if (shop != null) {
            if (shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_STACK_AMOUNT)
                    || plugin.perm().hasPermission(sender, "quickshop.other.amount")) {
                if (amount <= 0 || amount > Util.getItemMaxStackSize(shop.getItem().getType())) {
                    plugin.text().of(sender, "command.invalid-bulk-amount", amount).send();
                    return;
                }
                ItemStack pendingItemStack = shop.getItem().clone();
                pendingItemStack.setAmount(amount);
                PriceLimiter limiter = plugin.getShopManager().getPriceLimiter();
                PriceLimiterCheckResult checkResult = limiter.check(sender, pendingItemStack, shop.getCurrency(), shop.getPrice());
                if (checkResult.getStatus() != PriceLimiterStatus.PASS) {
                    plugin.text().of(sender, "restricted-prices", MsgUtil.getTranslateText(shop.getItem()),
                            Component.text(checkResult.getMin()),
                            Component.text(checkResult.getMax())).send();
                    return;
                }
                shop.setItem(pendingItemStack);
                plugin.text().of(sender, "command.bulk-size-now", shop.getItem().getAmount(), MsgUtil.getTranslateText(shop.getItem())).send();
            } else {
                plugin.text().of(sender, "not-managed-shop").send();
            }
        } else {
            plugin.text().of(sender, "not-looking-at-shop").send();
        }


    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length == 1 ? Collections.singletonList(LegacyComponentSerializer.legacySection().serialize(plugin.text().of(sender, "tabcomplete.amount").forLocale())) : Collections.emptyList();
    }
}
