package com.ghostchu.quickshop.addon.discount.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.discount.DiscountCode;
import com.ghostchu.quickshop.addon.discount.Main;
import com.ghostchu.quickshop.api.event.ShopInfoPanelEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
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

    public MainListener(@NotNull Main main) {
        this.main = main;
        this.quickshop = QuickShop.getInstance();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPrePurchase(ShopInfoPanelEvent event) {
        UUID purchaser = event.getPurchaser();
        Shop shop = event.getShop();
        DiscountCode codeInstalled = main.getStatusManager().get(purchaser, main.getCodeManager());
        if (codeInstalled == null) {
            Log.debug("Player doesn't have installed the discount code");
            return;
        }
        if (!shop.isSelling()) {
            return;
        }
        String code = codeInstalled.getCode();
        Util.mainThreadRun(()->{
            switch (codeInstalled.applicableShop(purchaser, shop)) {
                case APPLICABLE -> quickshop.text().of(purchaser, "addon.discount.discount-code-applicable", code).send();
                case APPLICABLE_WITH_THRESHOLD ->
                        quickshop.text().of(purchaser, "addon.discount.discount-code-applicable", code, quickshop.getEconomy().format(codeInstalled.getThreshold(), shop.getLocation().getWorld(), shop.getCurrency())).send();
                case NOT_APPLICABLE ->
                        quickshop.text().of(purchaser, "addon.discount.discount-code-not-applicable", code).send();
                case REACHED_THE_LIMIT ->
                        quickshop.text().of(purchaser, "addon.discount.discount-code-reach-the-limit", code).send();
                case NO_PERMISSION ->
                        quickshop.text().of(purchaser, "addon.discount.discount-code-no-permission", code).send();
                case EXPIRED -> quickshop.text().of(purchaser, "addon.discount.discount-code-expired", code).send();
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPurchase(ShopPurchaseEvent event) {
        UUID purchaser = event.getPurchaser();
        Shop shop = event.getShop();
        DiscountCode codeInstalled = main.getStatusManager().get(purchaser, main.getCodeManager());
        if (codeInstalled == null) {
            Log.debug("Player doesn't have installed the discount code");
            return;
        }
        if (!shop.isSelling()) {
            return;
        }
        String code = codeInstalled.getCode();
        switch (codeInstalled.applicableShop(purchaser, shop)) {
            case APPLICABLE -> {
                double beforeDiscount = event.getTotal();
                event.setTotal(codeInstalled.apply(purchaser, event.getTotal()));
                double discounted = beforeDiscount - event.getTotal();
                quickshop.text().of(purchaser, "addon.discount.discount-code-applied-in-purchase", code, quickshop.getEconomy().format(discounted, shop.getLocation().getWorld(), shop.getCurrency())).send();
            }
            case APPLICABLE_WITH_THRESHOLD -> {
                if (event.getTotal() < codeInstalled.getThreshold()) {
                    quickshop.text().of(purchaser, "addon.discount.discount-code-under-threshold", quickshop.getEconomy().format(codeInstalled.getThreshold(), shop.getLocation().getWorld(), shop.getCurrency())).send();
                } else {
                    double beforeDiscount = event.getTotal();
                    event.setTotal(codeInstalled.apply(purchaser, event.getTotal()));
                    double discounted = beforeDiscount - event.getTotal();
                    quickshop.text().of(purchaser, "addon.discount.discount-code-applied-in-purchase", code, quickshop.getEconomy().format(discounted, shop.getLocation().getWorld(), shop.getCurrency())).send();
                }
            }
            case NOT_APPLICABLE ->
                    quickshop.text().of(purchaser, "addon.discount.discount-code-not-applicable", code).send();
            case REACHED_THE_LIMIT ->
                    quickshop.text().of(purchaser, "addon.discount.discount-code-reach-the-limit", code).send();
            case NO_PERMISSION ->
                    quickshop.text().of(purchaser, "addon.discount.discount-code-no-permission", code).send();
            case EXPIRED -> quickshop.text().of(purchaser, "addon.discount.discount-code-expired", code).send();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        main.getStatusManager().unset(event.getPlayer().getUniqueId());
    }

}
