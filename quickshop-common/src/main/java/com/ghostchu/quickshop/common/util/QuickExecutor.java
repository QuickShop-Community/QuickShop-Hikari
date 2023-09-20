package com.ghostchu.quickshop.common.util;

import java.util.concurrent.*;

public class QuickExecutor {

    private static final ExecutorService DATABASE_EXECUTOR = new ThreadPoolExecutor(2, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    private static final ExecutorService SHOP_SAVE_EXECUTOR = new ThreadPoolExecutor(0, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private static final ExecutorService COMMON_EXECUTOR = Executors.newCachedThreadPool();
    private static final BlockingQueue<Runnable> PRIMARY_PROFILE_IO_QUEUE = new LinkedBlockingDeque<>();
    private static ExecutorService PRIMARY_PROFILE_IO_EXECUTOR = new ThreadPoolExecutor(2, 32, 60L, TimeUnit.SECONDS, PRIMARY_PROFILE_IO_QUEUE);
    private static final BlockingQueue<Runnable> SECONDARY_PROFILE_IO_QUEUE = new LinkedBlockingDeque<>();
    private static ExecutorService SECONDARY_PROFILE_IO_EXECUTOR = new ThreadPoolExecutor(2, 32, 60L, TimeUnit.SECONDS, SECONDARY_PROFILE_IO_QUEUE);
    private QuickExecutor() {
    }

    public static ExecutorService getCommonExecutor() {
        return COMMON_EXECUTOR;
    }

    public static ExecutorService getDatabaseExecutor() {
        return DATABASE_EXECUTOR;
    }

    public static ExecutorService getShopSaveExecutor() {
        return SHOP_SAVE_EXECUTOR;
    }

    public static ExecutorService getProfileIOExecutor() {
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

    public static ExecutorService getPrimaryProfileIoExecutor() {
        return PRIMARY_PROFILE_IO_EXECUTOR;
    }

    public static void setSecondaryProfileIoExecutor(ExecutorService secondaryProfileIoExecutor) {
        SECONDARY_PROFILE_IO_EXECUTOR = secondaryProfileIoExecutor;
    }
}
