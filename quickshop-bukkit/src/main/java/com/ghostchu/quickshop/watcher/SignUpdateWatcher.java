package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

public class SignUpdateWatcher extends BukkitRunnable {
    private final Queue<Shop> signUpdateQueue = new LinkedList<>();

    @Override
    public void run() {
        Instant startTime = Instant.now();
        Instant endTime = startTime.plusMillis(50);
        Shop shop = signUpdateQueue.poll();
        while (shop != null && !shop.isDeleted() && !Instant.now().isAfter(endTime)) {
            shop.setSignText(QuickShop.getInstance().text().findRelativeLanguages(shop.getOwner()));
            shop = signUpdateQueue.poll();
        }
    }

    public void scheduleSignUpdate(@NotNull Shop shop) {
        if (signUpdateQueue.contains(shop)) {
            return; // Ignore if schedule too frequently
        }
        signUpdateQueue.add(shop);
    }

}
