package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.ContainerShop;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Check the shops after server booted up, make sure shop can correct self-deleted when container
 * lost.
 */
public class ShopContainerWatcher extends BukkitRunnable {
    private final Queue<Shop> checkQueue = new LinkedList<>();

    public void scheduleCheck(@NotNull Shop shop) {
        checkQueue.add(shop);
    }

    @Override
    public void run() {
        long beginTime = System.currentTimeMillis();
        Shop shop = checkQueue.poll();
        while (shop != null && !shop.isDeleted()) {
            if (shop instanceof ContainerShop) {
                ((ContainerShop) shop).checkContainer();
            }
            if (System.currentTimeMillis() - beginTime
                    > 45) { // Don't let quickshop eat more than 45 ms per tick.
                break;
            }
            shop = checkQueue.poll();
        }
    }

}
