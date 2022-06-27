/*
 *  This file is a part of project QuickShop, the name is SubCommand_Price.java
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
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.economy.EconomyTransaction;
import com.ghostchu.quickshop.shop.ContainerShop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class SubCommand_Price implements CommandHandler<Player> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "no-price-given").send();
            return;
        }

        final double price;

        try {
            price = Double.parseDouble(cmdArg[0]);
        } catch (NumberFormatException ex) {
            // No number input
            Log.debug(ex.getMessage());
            plugin.text().of(sender, "not-a-number", cmdArg[0]).send();
            return;
        }
        // No number input
        if (Double.isInfinite(price) || Double.isNaN(price)) {
            plugin.text().of(sender, "not-a-number", cmdArg[0]).send();
            return;
        }

        final boolean format = plugin.getConfig().getBoolean("use-decimal-format");

        double fee = 0;

        if (plugin.isPriceChangeRequiresFee()) {
            fee = plugin.getConfig().getDouble("shop.fee-for-price-change");
        }
        PriceLimiter limiter = plugin.getShopManager().getPriceLimiter();
        final Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }

        if (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_PRICE)
                && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.price")) {
            plugin.text().of(sender, "not-managed-shop").send();
            return;
        }

        if (shop.getPrice() == price) {
            // Stop here if there isn't a price change
            plugin.text().of(sender, "no-price-change").send();
            return;
        }

        PriceLimiterCheckResult checkResult = limiter.check(sender, shop.getItem(), plugin.getCurrency(), price);

        switch (checkResult.getStatus()) {
            case PRICE_RESTRICTED -> {
                plugin.text().of(sender, "restricted-prices", MsgUtil.getTranslateText(shop.getItem()),
                        Component.text(checkResult.getMin()),
                        Component.text(checkResult.getMax())).send();
                return;
            }
            case REACHED_PRICE_MIN_LIMIT -> {
                plugin.text().of(sender, "price-too-cheap", (format) ? MsgUtil.decimalFormat(checkResult.getMin()) : Double.toString(checkResult.getMin())).send();
                return;
            }
            case REACHED_PRICE_MAX_LIMIT -> {
                plugin.text().of(sender, "price-too-high", (format) ? MsgUtil.decimalFormat(checkResult.getMax()) : Double.toString(checkResult.getMax())).send();
                return;
            }
            case NOT_A_WHOLE_NUMBER -> {
                plugin.text().of(sender, "not-a-integer", price).send();
                return;
            }
        }

        if (fee > 0) {
            EconomyTransaction transaction = EconomyTransaction.builder()
                    .allowLoan(plugin.getConfig().getBoolean("shop.allow-economy-loan", false))
                    .core(plugin.getEconomy())
                    .from(sender.getUniqueId())
                    .amount(fee)
                    .world(Objects.requireNonNull(shop.getLocation().getWorld()))
                    .currency(plugin.getCurrency())
                    .build();
            if (!transaction.checkBalance()) {
                plugin.text().of(sender, "you-cant-afford-to-change-price", LegacyComponentSerializer.legacySection().deserialize(plugin.getEconomy().format(fee, shop.getLocation().getWorld(), shop.getCurrency()))).send();
                return;
            }
            if (!transaction.failSafeCommit()) {
                plugin.text().of(sender, "economy-transaction-failed", transaction.getLastError()).send();
                return;
            }
        }
        plugin.text().of(sender,
                "fee-charged-for-price-change", LegacyComponentSerializer.legacySection().deserialize(plugin.getEconomy().format(fee, shop.getLocation().getWorld(), plugin.getCurrency()))).send();
        // Update the shop
        shop.setPrice(price);
        shop.update();
        shop.setSignText(plugin.text().findRelativeLanguages(sender));
        plugin.text().of(sender,
                "price-is-now", LegacyComponentSerializer.legacySection().deserialize(plugin.getEconomy().format(shop.getPrice(), Objects.requireNonNull(shop.getLocation().getWorld()), shop.getCurrency()))).send();
        // Chest shops can be double shops.
        if (!(shop instanceof final ContainerShop cs)) {
            return;
        }

        if (!cs.isDoubleShop()) {
            return;
        }

        final Shop nextTo = cs.getAttachedShop();

        if (nextTo == null) {
            // TODO: 24/11/2019 Send message about that issue.
            return;
        }

        if (cs.isSelling()) {
            if (cs.getPrice() < nextTo.getPrice()) {
                plugin.text().of(sender, "buying-more-than-selling").send();
            }
        }
        // Buying
        else if (cs.getPrice() > nextTo.getPrice()) {
            plugin.text().of(sender, "buying-more-than-selling").send();
        }
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length == 1 ? Collections.singletonList(LegacyComponentSerializer.legacySection().serialize(plugin.text().of(sender, "tabcomplete.price").forLocale())) : Collections.emptyList();
    }

}
