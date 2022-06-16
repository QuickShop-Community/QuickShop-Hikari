/*
 *  This file is a part of project QuickShop, the name is SubCommand_Refill.java
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
import com.ghostchu.quickshop.api.event.ShopNamingEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.economy.EconomyTransaction;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class SubCommand_Name implements CommandHandler<Player> {

    private final QuickShop plugin;

    @Override
    @SneakyThrows
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        final Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        if (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_NAME)
                && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.shopnaming")) {
            plugin.text().of(sender, "not-managed-shop").send();
        }

        if (cmdArg.length < 1) {
            shop.setShopName(null);
            plugin.text().of(sender, "shop-name-unset").send();
            return;
        }

        String shopName = cmdArg[0];
        // Translate the all chat colors
        shopName = ChatColor.translateAlternateColorCodes('&', shopName);
        // Then strip all of them, Shop name reference is disallow any color
        shopName = ChatColor.stripColor(shopName);

        int maxLength = plugin.getConfig().getInt("shop.name-max-length", 32);
        if (shopName.length() > maxLength) {
            plugin.text().of(sender, "shop-name-too-long", maxLength).send();
            return;
        }

        double fee = plugin.getConfig().getDouble("shop.name-fee", 0);
        EconomyTransaction transaction = null;
        if (fee > 0) {
            if (!QuickShop.getPermissionManager().hasPermission(sender, "quickshop.bypass.namefee")) {
                transaction = EconomyTransaction.builder()
                        .world(shop.getLocation().getWorld())
                        .from(sender.getUniqueId())
                        .to(shop.getTaxAccount())
                        .currency(plugin.getCurrency())
                        .taxAccount(shop.getTaxAccount())
                        .taxModifier(0.0d)
                        .allowLoan(plugin.getConfig().getBoolean("shop.allow-economy-loan", false))
                        .core(plugin.getEconomy())
                        .amount(fee)
                        .build();
                if (!transaction.checkBalance()) {
                    plugin.text().of(sender, "you-cant-afford-shop-naming", plugin.getShopManager().format(fee, shop.getLocation().getWorld(), plugin.getCurrency())).send();
                    return;
                }
            }
        }
        ShopNamingEvent namingEvent = new ShopNamingEvent(shop, shopName);
        if (Util.fireCancellableEvent(namingEvent)) {
            Log.debug("Other plugin cancelled shop naming.");
            return;
        }
        shopName = namingEvent.getName();

        if (transaction != null && !transaction.failSafeCommit()) {
            plugin.text().of(sender, "economy-transaction-failed", transaction.getLastError()).send();
            plugin.getLogger().severe("EconomyTransaction Failed, last error:" + transaction.getLastError());
            return;
        }

        shop.setShopName(shopName);
        plugin.text().of(sender, "shop-name-success", shopName).send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length == 1 ? Collections.singletonList(LegacyComponentSerializer.legacySection().serialize(QuickShop.getInstance().text().of(sender, "tabcomplete.name").forLocale())) : Collections.emptyList();
    }

}
