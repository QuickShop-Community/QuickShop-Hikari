package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.QuickExecutor;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;

public class ShopDataSaveWatcher extends BukkitRunnable {
    //private final AtomicBoolean saving = new AtomicBoolean(false);
    private CompletableFuture<Void> saveTask;

    private final QuickShop plugin;

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
//    @Override
//    public void run() {
//        if (saving.get()) {
//            Log.debug("Another save task still running!");
//            return;
//        }
//        Log.debug("Starting save shops...");
//        saving.set(true);
//        try {
//            for (Shop shop : plugin.getShopManager().getAllShops()) {
//                try {
//                    shop.update().get();
//                } catch (InterruptedException | ExecutionException e) {
//                    plugin.getLogger().log(Level.WARNING, "Failed to save the shop.", e);
//                }
//            }
//        } finally {
//            saving.set(false);
//        }
//        Log.debug("Shops saved!");
//    }
}
