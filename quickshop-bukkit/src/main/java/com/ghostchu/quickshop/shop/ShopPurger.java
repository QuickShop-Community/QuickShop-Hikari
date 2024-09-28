package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.database.DatabaseIOUtil;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.BatchBukkitExecutor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


public class ShopPurger {

  private final QuickShop plugin;
  private volatile boolean executing;

  public ShopPurger(QuickShop plugin) {

    this.plugin = plugin;
  }

  public void purge() {

    if(!plugin.getConfig().getBoolean("purge.enabled")) {
      plugin.logger().info("[Shop Purger] Purge not enabled!");
      return;
    }
    if(executing) {
      plugin.logger().info("[Shop Purger] Another purge task still running!");
    } else {
      Util.asyncThreadRun(this::run);
    }
  }

  private void run() {

    Util.ensureThread(true);
    executing = true;
    DatabaseIOUtil ioUtil = new DatabaseIOUtil((SimpleDatabaseHelperV2)plugin.getDatabaseHelper());
    if(!ioUtil.performBackup("shops-auto-purge")) {
      plugin.logger().warn("[Shop Purger] Purge progress declined due backup failure");
      return;
    }
    plugin.logger().info("[Shop Purger] Scanning and removing shops....");
    List<Shop> pendingRemovalShops = new ArrayList<>();
    int days = plugin.getConfig().getInt("purge.days", 360);
    boolean deleteBanned = plugin.getConfig().getBoolean("purge.banned");
    boolean skipOp = plugin.getConfig().getBoolean("purge.skip-op");
    for(Shop shop : plugin.getShopManager().getAllShops()) {
      try {
        OfflinePlayer player = shop.getOwner().getUniqueIdIfRealPlayer().map(Bukkit::getOfflinePlayer).orElse(null);
        if(player == null) {
          return;
        }
        if(!player.hasPlayedBefore()) {
          Log.debug("Shop " + shop + " detection skipped: Owner never played before.");
          continue;
        }
        long lastPlayed = player.getLastPlayed();
        if(lastPlayed == 0) {
          continue;
        }
        if(player.isOnline()) {
          continue;
        }
        if(player.isOp() && skipOp) {
          continue;
        }
        boolean markDeletion = player.isBanned() && deleteBanned;
        long noOfDaysBetween = ChronoUnit.DAYS.between(CommonUtil.getDateTimeFromTimestamp(lastPlayed), CommonUtil.getDateTimeFromTimestamp(System.currentTimeMillis()));
        if(noOfDaysBetween > days) {
          markDeletion = true;
        }
        if(!markDeletion) {
          continue;
        }
        pendingRemovalShops.add(shop);
      } catch(Exception e) {
        plugin.logger().warn("Failed to purge shop " + shop.getShopId(), e);
      }
    }

    BatchBukkitExecutor<Shop> purgeExecutor = new BatchBukkitExecutor<>();
    purgeExecutor.addTasks(pendingRemovalShops);
    purgeExecutor.startHandle(plugin.getJavaPlugin(), (shop)->plugin.getShopManager().deleteShop(shop))
            .whenComplete((a, b)->{
              long usedTime = purgeExecutor.getStartTime().until(Instant.now(), java.time.temporal.ChronoUnit.MILLIS);
              plugin.logger().info("[Shop Purger] Total shop {} has been purged, used {}ms",
                                   pendingRemovalShops.size(),
                                   usedTime);
            });
  }
}
