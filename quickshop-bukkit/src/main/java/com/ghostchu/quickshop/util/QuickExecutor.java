package com.ghostchu.quickshop.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QuickExecutor {
    private static final ExecutorService databaseExecutor = new ThreadPoolExecutor(2, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private static final ExecutorService shopSaveExecutor = new ThreadPoolExecutor(0, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private static final ExecutorService commonExecutor = new ThreadPoolExecutor(1, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    public static ExecutorService getCommonExecutor() {
        return commonExecutor;
    }

    public static ExecutorService getDatabaseExecutor() {
        return databaseExecutor;
    }

    public static ExecutorService getShopSaveExecutor() {
        return shopSaveExecutor;
    }
}
