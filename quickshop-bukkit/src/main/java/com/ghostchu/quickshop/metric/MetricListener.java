package com.ghostchu.quickshop.metric;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.ShopMetricRecord;
import com.ghostchu.quickshop.api.database.ShopOperationEnum;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopDeleteEvent;
import com.ghostchu.quickshop.api.event.ShopOngoingFeeEvent;
import com.ghostchu.quickshop.api.event.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.listener.AbstractQSListener;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MetricListener extends AbstractQSListener implements Listener {
    public MetricListener(QuickShop plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCreate(ShopCreateEvent event) {
        plugin.getDatabaseHelper().insertMetricRecord(
                ShopMetricRecord.builder()
                        .time(System.currentTimeMillis())
                        .shopId(event.getShop().getShopId())
                        .player(event.getCreator())
                        .tax(0.0d)
                        .total(plugin.getConfig().getDouble("shop.cost"))
                        .type(ShopOperationEnum.CREATE)
                        .build()
        ).whenComplete((id, e) -> {
            if (e != null) {
                Log.debug("Failed to insert shop metric record: " + e.getMessage());
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDelete(ShopDeleteEvent event) {
        plugin.getDatabaseHelper().insertMetricRecord(
                ShopMetricRecord.builder()
                        .time(System.currentTimeMillis())
                        .shopId(event.getShop().getShopId())
                        .player(event.getShop().getOwner())
                        .tax(0.0d)
                        .total(plugin.getConfig().getBoolean("shop.refund") ? plugin.getConfig().getDouble("shop.cost", 0.0d) : 0.0d)
                        .type(ShopOperationEnum.CREATE)
                        .build()
        ).whenComplete((id, e) -> {
            if (e != null) {
                Log.debug("Failed to insert shop metric record: " + e.getMessage());
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDelete(ShopOngoingFeeEvent event) {
        plugin.getDatabaseHelper().insertMetricRecord(
                ShopMetricRecord.builder()
                        .time(System.currentTimeMillis())
                        .shopId(event.getShop().getShopId())
                        .player(event.getShop().getOwner())
                        .tax(0.0d)
                        .total(event.getCost())
                        .type(ShopOperationEnum.ONGOING_FEE)
                        .build()
        ).whenComplete((id, e) -> {
            if (e != null) {
                Log.debug("Failed to insert shop metric record: " + e.getMessage());
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPurchase(ShopSuccessPurchaseEvent event) {
        plugin.getDatabaseHelper().insertMetricRecord(
                        ShopMetricRecord.builder()
                                .time(System.currentTimeMillis())
                                .shopId(event.getShop().getShopId())
                                .player(event.getPurchaser())
                                .tax(event.getTax())
                                .total(event.getBalanceWithoutTax())
                                .type(wrapShopOperation(event.getShop()))
                                .build()
                )
                .whenComplete((id, e) -> {
                    if (e != null) {
                        Log.debug("Failed to insert shop metric record: " + e.getMessage());
                    }
                });
    }

    private ShopOperationEnum wrapShopOperation(Shop shop) {
        return switch (shop.getShopType()) {
            case SELLING -> ShopOperationEnum.PURCHASE_SELLING_SHOP;
            case BUYING -> ShopOperationEnum.PURCHASE_BUYING_SHOP;
        };
    }
}
