package com.ghostchu.quickshop.common.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QuickExecutor {

    private QuickExecutor() {
    }

    private static final ExecutorService DATABASE_EXECUTOR = new ThreadPoolExecutor(2, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private static final ExecutorService SHOP_SAVE_EXECUTOR = new ThreadPoolExecutor(0, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private static final ExecutorService COMMON_EXECUTOR = new ThreadPoolExecutor(1, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    public static ExecutorService getCommonExecutor() {
        return COMMON_EXECUTOR;
    }

    public static ExecutorService getDatabaseExecutor() {
        return DATABASE_EXECUTOR;
    }

    public static ExecutorService getShopSaveExecutor() {
        return SHOP_SAVE_EXECUTOR;
    }
}
