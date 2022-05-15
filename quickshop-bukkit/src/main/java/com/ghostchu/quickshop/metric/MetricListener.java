/*
 *  This file is a part of project QuickShop, the name is MetricListener.java
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

package com.ghostchu.quickshop.metric;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopDeleteEvent;
import com.ghostchu.quickshop.api.event.ShopOngoingFeeEvent;
import com.ghostchu.quickshop.api.event.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.listener.AbstractQSListener;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MetricListener extends AbstractQSListener implements Listener {
    public MetricListener(QuickShop plugin) {
        super(plugin);
    }

    private ShopOperationEnum wrapShopOperation(Shop shop) {
        return switch (shop.getShopType()) {
            case SELLING -> ShopOperationEnum.PURCHASE_SELLING_SHOP;
            case BUYING -> ShopOperationEnum.PURCHASE_BUYING_SHOP;
        };
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPurchase(ShopSuccessPurchaseEvent event) {
        Location loc = event.getShop().getLocation();
        plugin.getDatabaseHelper().insertMetricRecord(
                ShopMetricRecord.builder()
                        .time(System.currentTimeMillis())
                        .x(loc.getBlockX())
                        .y(loc.getBlockY())
                        .z(loc.getBlockZ())
                        .world(loc.getWorld().getName())
                        .player(event.getPurchaser())
                        .tax(event.getTax())
                        .total(event.getBalanceWithoutTax())
                        .type(wrapShopOperation(event.getShop()))
                        .build()
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCreate(ShopCreateEvent event) {
        Location loc = event.getShop().getLocation();
        plugin.getDatabaseHelper().insertMetricRecord(
                ShopMetricRecord.builder()
                        .time(System.currentTimeMillis())
                        .x(loc.getBlockX())
                        .y(loc.getBlockY())
                        .z(loc.getBlockZ())
                        .world(loc.getWorld().getName())
                        .player(event.getCreator())
                        .tax(0.0d)
                        .total(plugin.getConfig().getDouble("shop.cost"))
                        .type(ShopOperationEnum.CREATE)
                        .build()
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDelete(ShopDeleteEvent event) {
        Location loc = event.getShop().getLocation();
        plugin.getDatabaseHelper().insertMetricRecord(
                ShopMetricRecord.builder()
                        .time(System.currentTimeMillis())
                        .x(loc.getBlockX())
                        .y(loc.getBlockY())
                        .z(loc.getBlockZ())
                        .world(loc.getWorld().getName())
                        .player(event.getShop().getOwner())
                        .tax(0.0d)
                        .total(plugin.getConfig().getBoolean("shop.refund") ? plugin.getConfig().getDouble("shop.cost", 0.0d) : 0.0d)
                        .type(ShopOperationEnum.CREATE)
                        .build()
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDelete(ShopOngoingFeeEvent event) {
        Location loc = event.getShop().getLocation();
        plugin.getDatabaseHelper().insertMetricRecord(
                ShopMetricRecord.builder()
                        .time(System.currentTimeMillis())
                        .x(loc.getBlockX())
                        .y(loc.getBlockY())
                        .z(loc.getBlockZ())
                        .world(loc.getWorld().getName())
                        .player(event.getShop().getOwner())
                        .tax(0.0d)
                        .total(event.getCost())
                        .type(ShopOperationEnum.ONGOING_FEE)
                        .build()
        );
    }
}
