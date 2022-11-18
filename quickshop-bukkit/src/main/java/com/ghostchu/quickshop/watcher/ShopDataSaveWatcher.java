package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;

public class ShopDataSaveWatcher extends BukkitRunnable {
    private final QuickShop plugin;
    private CompletableFuture<Void> saveTask;

    public ShopDataSaveWatcher(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (saveTask != null && !saveTask.isDone()) {
            Log.debug("Another save task still running!");
            return;
        }
        Log.debug("Starting save shops...");
        saveTask = CompletableFuture.allOf(plugin.getShopManager().getAllShops().stream().filter(Shop::isDirty)
                        .map(Shop::update)
                        .toArray(CompletableFuture[]::new))
                .whenCompleteAsync((v, e) -> {
                    if (e != null) {
                        Log.debug("Error saving shops: " + e.getMessage());
                    } else {
                        Log.debug("Finished saving shops!");
                    }
                }, QuickExecutor.getShopSaveExecutor());
    }
}
