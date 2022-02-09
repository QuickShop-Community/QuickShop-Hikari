package org.maxgamer.quickshop.metric;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.event.ShopCreateEvent;
import org.maxgamer.quickshop.api.event.ShopDeleteEvent;
import org.maxgamer.quickshop.api.event.ShopOngoingFeeEvent;
import org.maxgamer.quickshop.api.event.ShopSuccessPurchaseEvent;
import org.maxgamer.quickshop.listener.AbstractQSListener;

public class MetricListener extends AbstractQSListener implements Listener {
    public MetricListener(QuickShop plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPurchase(ShopSuccessPurchaseEvent event){
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
                        .type(ShopOperationEnum.PURCHASE)
                        .build()
        );
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCreate(ShopCreateEvent event){
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
    public void onDelete(ShopDeleteEvent event){
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
                        .total(plugin.getConfig().getBoolean("shop.refund") ? plugin.getConfig().getDouble("shop.cost",0.0d):0.0d)
                        .type(ShopOperationEnum.CREATE)
                        .build()
        );
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDelete(ShopOngoingFeeEvent event){
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
