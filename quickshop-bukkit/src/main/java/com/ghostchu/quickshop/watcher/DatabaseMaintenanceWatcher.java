package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.database.bean.IsolatedScanResult;
import com.ghostchu.quickshop.util.Timer;
import com.ghostchu.quickshop.util.holder.DatabaseStatusHolder;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class DatabaseMaintenanceWatcher extends BukkitRunnable {
    public DatabaseMaintenanceWatcher(QuickShop plugin) {
        this.plugin = plugin;
    }

    private final QuickShop plugin;
    @Getter
    @Nullable
    private DatabaseStatusHolder result = null;
    private final ReentrantLock LOCK = new ReentrantLock();


    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            if (!LOCK.tryLock()) {
                return;
            }
            Timer timer = new Timer();
            SimpleDatabaseHelperV2 databaseHelper = (SimpleDatabaseHelperV2) plugin.getDatabaseHelper();
            IsolatedScanResult<Long> dataIds = databaseHelper.scanIsolatedDataIds();
            IsolatedScanResult<Long> shopIds = databaseHelper.scanIsolatedShopIds();
            long reportGeneratedAt = System.currentTimeMillis();
            this.result = new DatabaseStatusHolder(DatabaseStatusHolder.Status.GOOD, dataIds, shopIds, reportGeneratedAt);
            Log.debug("Database Maintenance Watcher: Report generated in " + timer.stopAndGetTimePassed() + "ms, content=" + this.result.toString());
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Database Maintenance Watcher exited with an error", e);
        } finally {
            LOCK.unlock();
        }
    }
}
