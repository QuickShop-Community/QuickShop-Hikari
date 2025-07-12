package com.ghostchu.quickshop.addon.discount.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.discount.DiscountCode;
import com.ghostchu.quickshop.addon.discount.Main;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.general.ShopInfoPanelEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MainListener implements Listener {

  private final Main main;
  private final QuickShop quickshop;

  public MainListener(@NotNull final Main main) {

    this.main = main;
    this.quickshop = QuickShop.getInstance();
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPrePurchase(final ShopInfoPanelEvent event) {

    final UUID purchaser = event.getPurchaser();
    final Shop shop = event.getShop();
    final DiscountCode codeInstalled = main.getStatusManager().get(purchaser, main.getCodeManager());
    if(codeInstalled == null) {
      Log.debug("Player doesn't have installed the discount code");
      return;
    }
    if(!shop.isSelling()) {
      return;
    }
    final String code = codeInstalled.getCode();
    Util.mainThreadRun(()->{
      switch(codeInstalled.applicableShop(purchaser, shop)) {
        case APPLICABLE ->
                quickshop.text().of(purchaser, "addon.discount.discount-code-applicable", code).send();
        case APPLICABLE_WITH_THRESHOLD ->
                quickshop.text().of(purchaser, "addon.discount.discount-code-applicable", code, quickshop.getEconomy().format(codeInstalled.getThreshold(), shop.getLocation().getWorld(), shop.getCurrency())).send();
        case NOT_APPLICABLE ->
                quickshop.text().of(purchaser, "addon.discount.discount-code-not-applicable", code).send();
        case REACHED_THE_LIMIT ->
                quickshop.text().of(purchaser, "addon.discount.discount-code-reach-the-limit", code).send();
        case NO_PERMISSION ->
                quickshop.text().of(purchaser, "addon.discount.discount-code-no-permission", code).send();
        case EXPIRED ->
                quickshop.text().of(purchaser, "addon.discount.discount-code-expired", code).send();
      }
    });
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onPurchase(final ShopPurchaseEvent event) {

    final QUser purchaserQUser = event.getPurchaser();
    final UUID purchaser = purchaserQUser.getUniqueIdIfRealPlayer().orElse(null);
    if(purchaser == null) {
      return;
    }
    final Shop shop = event.getShop();
    final DiscountCode codeInstalled = main.getStatusManager().get(purchaser, main.getCodeManager());
    if(codeInstalled == null) {
      Log.debug("Player doesn't have installed the discount code");
      return;
    }
    if(!shop.isSelling()) {
      return;
    }
    final String code = codeInstalled.getCode();
    switch(codeInstalled.applicableShop(purchaser, shop)) {
      case APPLICABLE -> {
        final double beforeDiscount = event.getTotal();
        event.setTotal(codeInstalled.apply(purchaser, event.getTotal()));
        final double discounted = beforeDiscount - event.getTotal();
        quickshop.text().of(purchaser, "addon.discount.discount-code-applied-in-purchase", code, quickshop.getEconomy().format(discounted, shop.getLocation().getWorld(), shop.getCurrency())).send();
      }
      case APPLICABLE_WITH_THRESHOLD -> {
        if(event.getTotal() < codeInstalled.getThreshold()) {
          quickshop.text().of(purchaser, "addon.discount.discount-code-under-threshold", quickshop.getEconomy().format(codeInstalled.getThreshold(), shop.getLocation().getWorld(), shop.getCurrency())).send();
        } else {
          final double beforeDiscount = event.getTotal();
          event.setTotal(codeInstalled.apply(purchaser, event.getTotal()));
          final double discounted = beforeDiscount - event.getTotal();
          quickshop.text().of(purchaser, "addon.discount.discount-code-applied-in-purchase", code, quickshop.getEconomy().format(discounted, shop.getLocation().getWorld(), shop.getCurrency())).send();
        }
      }
      case NOT_APPLICABLE ->
              quickshop.text().of(purchaser, "addon.discount.discount-code-not-applicable", code).send();
      case REACHED_THE_LIMIT ->
              quickshop.text().of(purchaser, "addon.discount.discount-code-reach-the-limit", code).send();
      case NO_PERMISSION ->
              quickshop.text().of(purchaser, "addon.discount.discount-code-no-permission", code).send();
      case EXPIRED ->
              quickshop.text().of(purchaser, "addon.discount.discount-code-expired", code).send();
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onQuit(final PlayerQuitEvent event) {

    main.getStatusManager().unset(event.getPlayer().getUniqueId());
  }

}
