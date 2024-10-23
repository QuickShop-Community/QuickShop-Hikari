package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.general.ShopOngoingFeeEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.economy.SimpleEconomyTransaction;
import com.ghostchu.quickshop.shop.SimpleShopManager;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.WarningSender;
import com.ghostchu.quickshop.util.logger.Log;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Check the shops after server booted up, make sure shop can correct self-deleted when container
 * lost.
 */
public class OngoingFeeWatcher implements Runnable {

  private final QuickShop plugin;
  private final WarningSender warningSender;
  WrappedTask task = null;

  public OngoingFeeWatcher(@NotNull final QuickShop plugin) {

    this.plugin = plugin;
    this.warningSender = new WarningSender(plugin, 6000);
  }

  @Override
  public void run() {

    Log.debug("Run task for ongoing fee...");
    if(plugin.getEconomy() == null) {
      Log.debug("Economy hadn't get ready.");
      return;
    }

    final boolean allowLoan = plugin.getConfig().getBoolean("shop.allow-economy-loan");
    final boolean ignoreUnlimited = plugin.getConfig().getBoolean("shop.ongoing-fee.ignore-unlimited");
    final double gobalCost = plugin.getConfig().getDouble("shop.ongoing-fee.cost-per-shop");
    for(final Shop shop : plugin.getShopManager().getAllShops()) {
      if(!shop.isUnlimited() || !ignoreUnlimited) {
        final QUser shopOwner = shop.getOwner();
        final Location location = shop.getLocation();
        if(!location.isWorldLoaded()) {
          //ignore unloaded world
          continue;
        }
        double cost = gobalCost;
        final World world = location.getWorld();
        //We must check balance manually to avoid shop missing hell when tax account broken
        if(allowLoan || plugin.getEconomy().getBalance(shopOwner, Objects.requireNonNull(world), plugin.getCurrency()) >= cost) {
          QUser taxAccount = null;
          if(shop.getTaxAccount() != null) {
            taxAccount = shop.getTaxAccount();
          } else {
            final QUser uuid = ((SimpleShopManager)plugin.getShopManager()).getCacheTaxAccount();
            if(uuid != null) {
              taxAccount = uuid;
            }
          }

          final ShopOngoingFeeEvent event = new ShopOngoingFeeEvent(shop, shopOwner, cost);
          if(Util.fireCancellableEvent(event)) {
            continue;
          }

          cost = event.getCost();
          final double finalCost = cost;

          final QUser finalTaxAccount = taxAccount;
          Util.mainThreadRun(()->{
            final SimpleEconomyTransaction transaction = SimpleEconomyTransaction.builder()
                    .allowLoan(allowLoan)
                    .currency(plugin.getCurrency())
                    .core(plugin.getEconomy())
                    .world(world)
                    .amount(finalCost)
                    .to(finalTaxAccount)
                    .from(shopOwner).build();

            final boolean success = transaction.failSafeCommit();
            if(!success) {
              warningSender.sendWarn("Unable to deposit ongoing fee to tax account, the last error is " + transaction.getLastError());
            }
          });
        } else {
          this.removeShop(shop);
        }
      }
    }
  }

  public void start(final int i, final int i2) {

    task = QuickShop.folia().getImpl().runTimerAsync(this, i, i2);
  }

  public void stop() {

    try {
      if(task != null && !task.isCancelled()) {
        task.cancel();
      }
    } catch(IllegalStateException ex) {
      Log.debug("Task already cancelled " + ex.getMessage());
    }
  }

  /**
   * Remove shop and send alert to shop owner
   *
   * @param shop The shop was remove cause no enough ongoing fee
   */
  public void removeShop(@NotNull final Shop shop) {

    Util.mainThreadRun(()->plugin.getShopManager().deleteShop(shop));
    MsgUtil.send(shop, shop.getOwner(), plugin.text().of("shop-removed-cause-ongoing-fee", LegacyComponentSerializer.legacySection().deserialize("World:"
                                                                                                                                                 + Objects.requireNonNull(shop.getLocation().getWorld()).getName()
                                                                                                                                                 + " X:"
                                                                                                                                                 + shop.getLocation().getBlockX()
                                                                                                                                                 + " Y:"
                                                                                                                                                 + shop.getLocation().getBlockY()
                                                                                                                                                 + " Z:"
                                                                                                                                                 + shop.getLocation().getBlockZ())).forLocale());
  }
}
