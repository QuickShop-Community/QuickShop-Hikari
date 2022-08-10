package com.ghostchu.quickshop.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuickExecutor {
    private static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(
            Math.max(8, Runtime.getRuntime().availableProcessors() * 4), (r) -> {
                Thread thread = new Thread(r, "QuickShop-Hikari-Database-Pool");
                thread.setDaemon(true);
                return thread;
            });
    private static final ExecutorService shopSaveExecutor = Executors.newFixedThreadPool(
            3, (r) -> {
                Thread thread = new Thread(r, "QuickShop-Hikari-AutoSave-Pool");
                thread.setDaemon(true);
                return thread;
            });
    private static final ExecutorService commonExecutor = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() * 2), (r) -> {
        Thread thread = new Thread(r, "QuickShop-Hikari-Common-Pool");
        thread.setDaemon(true);
        return thread;
    });

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
