package com.ghostchu.quickshop.common.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QuickExecutor {

  private static ExecutorService HIKARICP_EXECUTOR;
  private static ExecutorService SHOP_HISTORY_QUERY_EXECUTOR;
  private static ExecutorService SHOP_SAVE_EXECUTOR = Executors.newWorkStealingPool(2);
  private static ExecutorService COMMON_EXECUTOR = Executors.newCachedThreadPool();
  private static ExecutorService PRIMARY_PROFILE_IO_EXECUTOR = Executors.newWorkStealingPool(16);
  private static ExecutorService SECONDARY_PROFILE_IO_EXECUTOR = Executors.newWorkStealingPool(2);
  private static final ExecutorService ERROR_REPORT_EXECUTOR = new ThreadPoolExecutor(
          1,
          1,
          60L,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(100),r -> {
            Thread t = new Thread(r, "QuickShop-ErrorReporter");
            t.setDaemon(true);
            return t;
    }, new ThreadPoolExecutor.DiscardPolicy());

  static {
    HIKARICP_EXECUTOR = provideHikariCPExecutor();
    SHOP_HISTORY_QUERY_EXECUTOR = provideShopHistoryQueryExecutor();
  }

  private QuickExecutor() {


  }

  public static ExecutorService provideShopHistoryQueryExecutor() {

    return new ThreadPoolExecutor(1, 2, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
  }

  public static ExecutorService provideHikariCPExecutor() {

    return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("QuickShop-Database-Worker-", 0).factory());
  }

  public static ExecutorService getErrorReportExecutor() {
    return ERROR_REPORT_EXECUTOR;
  }

  public static ExecutorService getCommonExecutor() {

    return COMMON_EXECUTOR;
  }

  public static void setCommonExecutor(final ExecutorService commonExecutor) {

    COMMON_EXECUTOR = commonExecutor;
  }

  public static ExecutorService getHikaricpExecutor() {

    return HIKARICP_EXECUTOR;
  }

  public static void setHikaricpExecutor(final ExecutorService hikaricpExecutor) {

    HIKARICP_EXECUTOR = hikaricpExecutor;
  }

  public static ExecutorService getShopSaveExecutor() {

    return SHOP_SAVE_EXECUTOR;
  }

  public static void setShopSaveExecutor(final ExecutorService shopSaveExecutor) {

    SHOP_SAVE_EXECUTOR = shopSaveExecutor;
  }

  public static ExecutorService getPrimaryProfileIoExecutor() {

    return PRIMARY_PROFILE_IO_EXECUTOR;
  }

  public static void setPrimaryProfileIoExecutor(final ExecutorService primaryProfileIoExecutor) {

    PRIMARY_PROFILE_IO_EXECUTOR = primaryProfileIoExecutor;
  }

  public static ExecutorService getSecondaryProfileIoExecutor() {

    return SECONDARY_PROFILE_IO_EXECUTOR;
  }

  public static void setSecondaryProfileIoExecutor(final ExecutorService secondaryProfileIoExecutor) {

    SECONDARY_PROFILE_IO_EXECUTOR = secondaryProfileIoExecutor;
  }

  public static ExecutorService getShopHistoryQueryExecutor() {

    return SHOP_HISTORY_QUERY_EXECUTOR;
  }

  public static void setShopHistoryQueryExecutor(final ExecutorService shopHistoryQueryExecutor) {

    SHOP_HISTORY_QUERY_EXECUTOR = shopHistoryQueryExecutor;
  }
}
