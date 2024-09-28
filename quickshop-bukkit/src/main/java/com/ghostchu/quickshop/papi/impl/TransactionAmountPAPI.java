package com.ghostchu.quickshop.papi.impl;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.ShopMetricRecord;
import com.ghostchu.quickshop.api.database.ShopOperationEnum;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.database.MetricQuery;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.papi.PAPISubHandler;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

public class TransactionAmountPAPI implements PAPISubHandler {

  private final QuickShop plugin;
  private final MetricQuery query;

  public TransactionAmountPAPI(@NotNull final QuickShop plugin) {

    this.plugin = plugin;
    this.query = new MetricQuery(plugin, (SimpleDatabaseHelperV2)plugin.getDatabaseHelper());
  }

  @Override
  public @NotNull String getPrefix() {

    return "metrics_recent_transactionamount";
  }

  @Override
  @Nullable
  public String handle0(@NotNull final OfflinePlayer player, @NotNull final String paramsTrimmed) {

    final String[] args = paramsTrimmed.split("_");
    if(args.length < 1) {
      return null;
    }
    final String[] passThroughArgs = new String[args.length - 1];
    System.arraycopy(args, 1, passThroughArgs, 0, passThroughArgs.length);

    return switch(args[0]) {
      case "global" -> handleGlobal(player, passThroughArgs);
      case "player" -> handlePlayer(player, passThroughArgs);
      default -> null;
    };
  }

  @Nullable
  private String handleGlobal(@NotNull final OfflinePlayer player, @NotNull final String[] passThroughArgs) {

    if(passThroughArgs.length < 2) {
      return null;
    }
    final String type = passThroughArgs[0];
    final String days = passThroughArgs[1];
    if(!StringUtils.isNumeric(days)) {
      return null;
    }
    final ShopType shopType = ShopType.fromString(type.toUpperCase(Locale.ROOT));
    final int recentDays = Integer.parseInt(days);
    final Date startTime = new Date(Instant.now().minus(Duration.ofDays(recentDays)).toEpochMilli());
    final long count = this.query.queryServerPurchaseRecords(startTime, -1, false).stream()
            .filter(record->{
              if(shopType == null) {
                return true;
              }
              if(shopType == ShopType.SELLING) {
                return record.getType() == ShopOperationEnum.PURCHASE_SELLING_SHOP;
              }
              if(shopType == ShopType.BUYING) {
                return record.getType() == ShopOperationEnum.PURCHASE_BUYING_SHOP;
              }
              return false;
            })
            .mapToLong(ShopMetricRecord::getAmount).sum();
    return String.valueOf(count);
  }

  @Nullable
  private String handlePlayer(@NotNull final OfflinePlayer player, final String[] passThroughArgs) {

    if(passThroughArgs.length < 2) {
      return null;
    }
    final String type = passThroughArgs[0];
    final String days = passThroughArgs[1];
    if(!StringUtils.isNumeric(days)) {
      return null;
    }
    final ShopType shopType = ShopType.fromString(type.toUpperCase(Locale.ROOT));
    final int recentDays = Integer.parseInt(days);
    final Date startTime = new Date(Instant.now().minus(Duration.ofDays(recentDays)).toEpochMilli());
    final long count = this.query.queryServerPurchaseRecords(startTime, -1, false).stream()
            .filter(record->{
              if(shopType == null) {
                return true;
              }
              if(shopType == ShopType.SELLING) {
                return record.getType() == ShopOperationEnum.PURCHASE_SELLING_SHOP;
              }
              if(shopType == ShopType.BUYING) {
                return record.getType() == ShopOperationEnum.PURCHASE_BUYING_SHOP;
              }
              return false;
            })
            .filter(record->{
              final QUser qUser = QUserImpl.createSync(plugin.getPlayerFinder(), record.getPlayer());
              return player.getUniqueId().equals(qUser.getUniqueId());
            })
            .mapToLong(ShopMetricRecord::getAmount).sum();
    return String.valueOf(count);
  }
}
