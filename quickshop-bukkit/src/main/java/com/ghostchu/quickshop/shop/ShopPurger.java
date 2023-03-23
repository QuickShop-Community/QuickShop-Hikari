package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.database.DatabaseIOUtil;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.economy.SimpleEconomyTransaction;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.BatchBukkitExecutor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ShopPurger {
    private final QuickShop plugin;
    private volatile boolean executing;

    public ShopPurger(QuickShop plugin) {
        this.plugin = plugin;
    }

    public void purge() {
        if (!plugin.getConfig().getBoolean("purge.enabled")) {
            plugin.logger().info("[Shop Purger] Purge not enabled!");
            return;
        }
        if (executing) {
            plugin.logger().info("[Shop Purger] Another purge task still running!");
        } else {
            Util.asyncThreadRun(this::run);
        }
    }

    private void run() {
        Util.ensureThread(true);
        executing = true;
        if (plugin.getConfig().getBoolean("purge.backup")) {
            DatabaseIOUtil ioUtil = new DatabaseIOUtil((SimpleDatabaseHelperV2) plugin.getDatabaseHelper());
            try {
                File file = new File("purge-backup-" + UUID.randomUUID() + ".zip");
                ioUtil.exportTables(file);
                plugin.logger().info("[Shop Purger] We have backup shop data as {}, if you ran into any trouble, please rename it to recovery.txt then use /qs recovery in console to rollback!", file.getName());
            } catch (SQLException | IOException e) {
                plugin.logger().warn("Failed to backup database, purge cancelled.", e);
                return;
            }

        }
        plugin.logger().info("[Shop Purger] Scanning and removing shops....");
        List<Shop> pendingRemovalShops = new ArrayList<>();
        int days = plugin.getConfig().getInt("purge.days", 360);
        boolean deleteBanned = plugin.getConfig().getBoolean("purge.banned");
        boolean skipOp = plugin.getConfig().getBoolean("purge.skip-op");
        boolean returnCreationFee = plugin.getConfig().getBoolean("purge.return-create-fee");
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(shop.getOwner());
            if (!player.hasPlayedBefore()) {
                Log.debug("Shop " + shop + " detection skipped: Owner never played before.");
                continue;
            }
            long lastPlayed = player.getLastPlayed();
            if (lastPlayed == 0) {
                continue;
            }
            if (player.isOnline()) {
                continue;
            }
            if (player.isOp() && skipOp) {
                continue;
            }
            boolean markDeletion = player.isBanned() && deleteBanned;
            long noOfDaysBetween = ChronoUnit.DAYS.between(CommonUtil.getDateTimeFromTimestamp(lastPlayed), CommonUtil.getDateTimeFromTimestamp(System.currentTimeMillis()));
            if (noOfDaysBetween > days) {
                markDeletion = true;
            }
            if (!markDeletion) {
                continue;
            }
            pendingRemovalShops.add(shop);
        }

        BatchBukkitExecutor<Shop> purgeExecutor = new BatchBukkitExecutor<>();
        purgeExecutor.addTasks(pendingRemovalShops);
        purgeExecutor.startHandle(plugin.getJavaPlugin(), (shop) -> {
            shop.delete(false);
            if (returnCreationFee) {
                SimpleEconomyTransaction transaction =
                        SimpleEconomyTransaction.builder()
                                .amount(plugin.getConfig().getDouble("shop.cost"))
                                .core(plugin.getEconomy())
                                .currency(shop.getCurrency())
                                .world(shop.getLocation().getWorld())
                                .to(shop.getOwner())
                                .build();
                transaction.failSafeCommit();
            }
        }).whenComplete((a, b) -> {
            long usedTime = purgeExecutor.getStartTime().until(Instant.now(), java.time.temporal.ChronoUnit.MILLIS);
            plugin.logger().info("[Shop Purger] Total shop {} has been purged, used {}ms",
                    pendingRemovalShops.size(),
                    usedTime);
        });
    }
}
