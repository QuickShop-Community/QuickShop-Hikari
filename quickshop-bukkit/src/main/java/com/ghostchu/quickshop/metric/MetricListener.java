package com.ghostchu.quickshop.metric;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.ShopMetricRecord;
import com.ghostchu.quickshop.api.database.ShopOperationEnum;
import com.ghostchu.quickshop.api.event.economy.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.event.general.ShopOngoingFeeEvent;
import com.ghostchu.quickshop.api.event.modification.ShopCreateSuccessEvent;
import com.ghostchu.quickshop.api.event.modification.ShopDeleteEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.listener.AbstractQSListener;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MetricListener extends AbstractQSListener implements Listener {

  public MetricListener(final QuickShop plugin) {

    super(plugin);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onCreate(final ShopCreateSuccessEvent event) {

    plugin.getDatabaseHelper().insertMetricRecord(
                    ShopMetricRecord.builder()
                            .time(System.currentTimeMillis())
                            .shopId(event.getShop().getShopId())
                            .player(event.getCreator())
                            .tax(0.0d)
                            .total(plugin.getConfig().getDouble("shop.cost"))
                            .type(ShopOperationEnum.CREATE)
                            .build()
                                                 )
            .exceptionally(e->{
              Log.debug("Failed to insert shop metric record: " + e.getMessage());
              return 0;
            });
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onDelete(final ShopDeleteEvent event) {

    plugin.getDatabaseHelper().insertMetricRecord(
                    ShopMetricRecord.builder()
                            .time(System.currentTimeMillis())
                            .shopId(event.getShop().getShopId())
                            .player(event.getShop().getOwner())
                            .tax(0.0d)
                            .total(plugin.getConfig().getBoolean("shop.refund")? plugin.getConfig().getDouble("shop.cost", 0.0d) : 0.0d)
                            .type(ShopOperationEnum.DELETE)
                            .build()
                                                 )
            .exceptionally(e->{
              Log.debug("Failed to insert shop metric record: " + e.getMessage());
              return 0;
            });

  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onDelete(final ShopOngoingFeeEvent event) {

    plugin.getDatabaseHelper().insertMetricRecord(
                    ShopMetricRecord.builder()
                            .time(System.currentTimeMillis())
                            .shopId(event.getShop().getShopId())
                            .player(event.getShop().getOwner())
                            .tax(0.0d)
                            .total(event.getCost())
                            .type(ShopOperationEnum.ONGOING_FEE)
                            .build()
                                                 )
            .exceptionally(e->{
              Log.debug("Failed to insert shop metric record: " + e.getMessage());
              return 0;
            });
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPurchase(final ShopSuccessPurchaseEvent event) {

    plugin.getDatabaseHelper().insertMetricRecord(
                    ShopMetricRecord.builder()
                            .time(System.currentTimeMillis())
                            .shopId(event.getShop().getShopId())
                            .player(event.getPurchaser())
                            .tax(event.getTax())
                            .total(event.getBalanceWithoutTax())
                            .type(wrapShopOperation(event.getShop()))
                            .amount(event.getAmount())
                            .build()
                                                 )
            .exceptionally(e->{
              Log.debug("Failed to insert shop metric record: " + e.getMessage());
              return 0;
            });
  }

  private ShopOperationEnum wrapShopOperation(final Shop shop) {

    return switch(shop.getShopType()) {
      case SELLING -> ShopOperationEnum.PURCHASE_SELLING_SHOP;
      case BUYING -> ShopOperationEnum.PURCHASE_BUYING_SHOP;
      case FROZEN -> ShopOperationEnum.FROZEN;
    };
  }
}
