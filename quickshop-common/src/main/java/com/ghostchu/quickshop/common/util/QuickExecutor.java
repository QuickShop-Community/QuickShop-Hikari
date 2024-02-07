package com.ghostchu.quickshop.common.util;

import java.util.concurrent.*;

public class QuickExecutor {
    private static ExecutorService HIKARICP_EXECUTOR;
    private static ExecutorService SHOP_HISTORY_QUERY_EXECUTOR;
    private static ExecutorService SHOP_SAVE_EXECUTOR = new ThreadPoolExecutor(1, 2, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private static ExecutorService COMMON_EXECUTOR = Executors.newCachedThreadPool();
    private static BlockingQueue<Runnable> PRIMARY_PROFILE_IO_QUEUE = new LinkedBlockingDeque<>();
    private static ExecutorService PRIMARY_PROFILE_IO_EXECUTOR = new ThreadPoolExecutor(2, 32, 60L, TimeUnit.SECONDS, PRIMARY_PROFILE_IO_QUEUE);
    private static BlockingQueue<Runnable> SECONDARY_PROFILE_IO_QUEUE = new LinkedBlockingDeque<>();
    private static ExecutorService SECONDARY_PROFILE_IO_EXECUTOR = new ThreadPoolExecutor(2, 32, 60L, TimeUnit.SECONDS, SECONDARY_PROFILE_IO_QUEUE);
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
        return new ThreadPoolExecutor(2, 8, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    public static ExecutorService getCommonExecutor() {
        return COMMON_EXECUTOR;
    }

    public static ExecutorService getHikaricpExecutor() {
        return HIKARICP_EXECUTOR;
    }

    public static ExecutorService getShopSaveExecutor() {
        return SHOP_SAVE_EXECUTOR;
    }

    public static ExecutorService getPrimaryProfileIoExecutor() {
        return PRIMARY_PROFILE_IO_EXECUTOR;
    }

    public static BlockingQueue<Runnable> getPrimaryProfileIoQueue() {
        return PRIMARY_PROFILE_IO_QUEUE;
    }

    public static void setPrimaryProfileIoExecutor(ExecutorService primaryProfileIoExecutor) {
        PRIMARY_PROFILE_IO_EXECUTOR = primaryProfileIoExecutor;
    }

    public static ExecutorService getSecondaryProfileIoExecutor() {
        return SECONDARY_PROFILE_IO_EXECUTOR;
    }

    public static BlockingQueue<Runnable> getSecondaryProfileIoQueue() {
        return SECONDARY_PROFILE_IO_QUEUE;
    }


    public static void setSecondaryProfileIoExecutor(ExecutorService secondaryProfileIoExecutor) {
        SECONDARY_PROFILE_IO_EXECUTOR = secondaryProfileIoExecutor;
    }

    public static ExecutorService getShopHistoryQueryExecutor() {
        return SHOP_HISTORY_QUERY_EXECUTOR;
    }

    public static void setHikaricpExecutor(ExecutorService hikaricpExecutor) {
        HIKARICP_EXECUTOR = hikaricpExecutor;
    }

    public static void setShopSaveExecutor(ExecutorService shopSaveExecutor) {
        SHOP_SAVE_EXECUTOR = shopSaveExecutor;
    }

    public static void setPrimaryProfileIoQueue(BlockingQueue<Runnable> primaryProfileIoQueue) {
        PRIMARY_PROFILE_IO_QUEUE = primaryProfileIoQueue;
    }

    public static void setSecondaryProfileIoQueue(BlockingQueue<Runnable> secondaryProfileIoQueue) {
        SECONDARY_PROFILE_IO_QUEUE = secondaryProfileIoQueue;
    }

    public static void setShopHistoryQueryExecutor(ExecutorService shopHistoryQueryExecutor) {
        SHOP_HISTORY_QUERY_EXECUTOR = shopHistoryQueryExecutor;
    }

    public static void setCommonExecutor(ExecutorService commonExecutor) {
        COMMON_EXECUTOR = commonExecutor;
    }
}
