package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopOngoingFeeEvent;
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

    public OngoingFeeWatcher(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        this.warningSender = new WarningSender(plugin, 6000);
    }

    @Override
    public void run() {
        Log.debug("Run task for ongoing fee...");
        if (plugin.getEconomy() == null) {
            Log.debug("Economy hadn't get ready.");
            return;
        }

        boolean allowLoan = plugin.getConfig().getBoolean("shop.allow-economy-loan");
        boolean ignoreUnlimited = plugin.getConfig().getBoolean("shop.ongoing-fee.ignore-unlimited");
        double gobalCost = plugin.getConfig().getDouble("shop.ongoing-fee.cost-per-shop");
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (!shop.isUnlimited() || !ignoreUnlimited) {
                QUser shopOwner = shop.getOwner();
                Location location = shop.getLocation();
                if (!location.isWorldLoaded()) {
                    //ignore unloaded world
                    continue;
                }
                double cost = gobalCost;
                World world = location.getWorld();
                //We must check balance manually to avoid shop missing hell when tax account broken
                if (allowLoan || plugin.getEconomy().getBalance(shopOwner, Objects.requireNonNull(world), plugin.getCurrency()) >= cost) {
                    QUser taxAccount = null;
                    if (shop.getTaxAccount() != null) {
                        taxAccount = shop.getTaxAccount();
                    } else {
                        QUser uuid = ((SimpleShopManager) plugin.getShopManager()).getCacheTaxAccount();
                        if (uuid != null) {
                            taxAccount = uuid;
                        }
                    }

                    ShopOngoingFeeEvent event = new ShopOngoingFeeEvent(shop, shopOwner, cost);
                    if (Util.fireCancellableEvent(event)) {
                        continue;
                    }

                    cost = event.getCost();
                    double finalCost = cost;

                    QUser finalTaxAccount = taxAccount;
                    Util.mainThreadRun(() -> {
                        SimpleEconomyTransaction transaction = SimpleEconomyTransaction.builder()
                                .allowLoan(allowLoan)
                                .currency(plugin.getCurrency())
                                .core(plugin.getEconomy())
                                .world(world)
                                .amount(finalCost)
                                .to(finalTaxAccount)
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

    public void start(int i, int i2) {
        task = QuickShop.folia().getImpl().runTimerAsync(this, i, i2);
    }

    public void stop() {
        try {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        } catch (IllegalStateException ex) {
            Log.debug("Task already cancelled " + ex.getMessage());
        }
    }

    /**
     * Remove shop and send alert to shop owner
     *
     * @param shop The shop was remove cause no enough ongoing fee
     */
    public void removeShop(@NotNull Shop shop) {
        Util.mainThreadRun(() -> plugin.getShopManager().deleteShop(shop));
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
