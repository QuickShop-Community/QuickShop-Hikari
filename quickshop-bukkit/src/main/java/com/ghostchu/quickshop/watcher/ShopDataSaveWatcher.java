package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

@AllArgsConstructor
public class ShopDataSaveWatcher extends BukkitRunnable {
    private final QuickShop plugin;
    private final AtomicBoolean saving = new AtomicBoolean(false);

    @Override
    public void run() {
        if (saving.get()) {
            Log.debug("Another save task still running!");
            return;
        }
        Log.debug("Starting save shops...");
        saving.set(true);
        try {
            for (Shop shop : plugin.getShopManager().getAllShops()) {
                try {
                    shop.update().get();
                } catch (InterruptedException | ExecutionException e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to save the shop.", e);
                }
            }
        } finally {
            saving.set(false);
        }
        Log.debug("Shops saved!");
    }
}
