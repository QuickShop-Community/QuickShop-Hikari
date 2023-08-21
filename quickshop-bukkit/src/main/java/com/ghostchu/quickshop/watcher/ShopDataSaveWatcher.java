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
                .thenAcceptAsync((v) -> Log.debug("Shop save completed."), QuickExecutor.getShopSaveExecutor())
                .exceptionally(e -> {
                    Log.debug("Error while saving shops: " + e.getMessage());
                    return null;
                });
    }
}
