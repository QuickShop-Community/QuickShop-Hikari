package com.ghostchu.quickshop.util;

import java.util.concurrent.*;

public class QuickExecutor {
    private static final ExecutorService databaseExecutor = new ThreadPoolExecutor(2, 16, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
    private static final ExecutorService shopSaveExecutor = Executors.newFixedThreadPool(3, (r) -> {
        Thread thread = new Thread(r, "QuickShop-Hikari-AutoSave-Pool");
        thread.setDaemon(true);
        return thread;
    });
    private static final ExecutorService commonExecutor = new ThreadPoolExecutor(1, 16, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());

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
