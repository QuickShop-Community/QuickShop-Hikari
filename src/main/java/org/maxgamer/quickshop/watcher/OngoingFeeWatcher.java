/*
 * This file is a part of project QuickShop, the name is OngoingFeeWatcher.java
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

package org.maxgamer.quickshop.watcher;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.economy.EconomyTransaction;
import org.maxgamer.quickshop.api.event.ShopOngoingFeeEvent;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.economy.Trader;
import org.maxgamer.quickshop.localization.LocalizedMessagePair;
import org.maxgamer.quickshop.shop.ShopTransactionMessageContainer;
import org.maxgamer.quickshop.shop.SimpleShopManager;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.WarningSender;

import java.util.Objects;
import java.util.UUID;

/**
 * Check the shops after server booted up, make sure shop can correct self-deleted when container
 * lost.
 */
public class OngoingFeeWatcher extends BukkitRunnable {
    private final QuickShop plugin;
    private final WarningSender warningSender;

    public OngoingFeeWatcher(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        this.warningSender = new WarningSender(plugin, 6000);
    }

    @Override
    public void run() {
        Util.debugLog("Run task for ongoing fee...");
        if (plugin.getEconomy() == null) {
            Util.debugLog("Economy hadn't get ready.");
            return;
        }

        boolean allowLoan = plugin.getConfig().getBoolean("shop.allow-economy-loan");
        boolean ignoreUnlimited = plugin.getConfig().getBoolean("shop.ongoing-fee.ignore-unlimited");
        double gobalCost = plugin.getConfig().getDouble("shop.ongoing-fee.cost-per-shop");
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if ((!shop.isUnlimited() || !ignoreUnlimited) && !shop.isDeleted()) {
                UUID shopOwner = shop.getOwner();
                Location location = shop.getLocation();
                if (!location.isWorldLoaded()) {
                    //ignore unloaded world
                    continue;
                }
                double cost = gobalCost;
                World world = location.getWorld();
                //We must check balance manually to avoid shop missing hell when tax account broken
                if (allowLoan || plugin.getEconomy().getBalance(shopOwner, Objects.requireNonNull(world), shop.getCurrency()) >= cost) {
                    Trader taxAccount;
                    if (shop.getTaxAccount() != null) {
                        taxAccount = new Trader(shop.getTaxAccount().toString(), Bukkit.getOfflinePlayer(shop.getTaxAccount()));
                    } else {
                        taxAccount = ((SimpleShopManager) plugin.getShopManager()).getCacheTaxAccount();
                    }

                    ShopOngoingFeeEvent event = new ShopOngoingFeeEvent(shop, shopOwner, cost);
                    if (Util.fireCancellableEvent(event)) {
                        continue;
                    }

                    cost = event.getCost();
                    double finalCost = cost;

                    Util.mainThreadRun(() -> {
                        EconomyTransaction transaction = EconomyTransaction.builder()
                                .allowLoan(allowLoan)
                                .currency(shop.getCurrency())
                                .core(plugin.getEconomy())
                                .world(world)
                                .amount(finalCost)
                                .to(taxAccount == null ? null : taxAccount.getUniqueId())
                                .from(shopOwner).build();

                        boolean success = transaction.failSafeCommit();
                        if (!success) {
                            warningSender.sendWarn("Unable to deposit ongoing fee to tax account, the last error is " + transaction.getLastError());
                        }
                    });
                } else {
                    this.removeShop(shop);
                }
            }
        }
    }

    /**
     * Remove shop and send alert to shop owner
     *
     * @param shop The shop was remove cause no enough ongoing fee
     */
    public void removeShop(@NotNull Shop shop) {
        Util.mainThreadRun(shop::delete);

        MsgUtil.send(shop, shop.getOwner(), ShopTransactionMessageContainer.ofLocalizedMessageWithItem(
                LocalizedMessagePair.of("shop-removed-cause-ongoing-fee", "World:"
                        + Objects.requireNonNull(shop.getLocation().getWorld()).getName()
                        + " X:"
                        + shop.getLocation().getBlockX()
                        + " Y:"
                        + shop.getLocation().getBlockY()
                        + " Z:"
                        + shop.getLocation().getBlockZ()
                )
                , null, null));
    }

}
