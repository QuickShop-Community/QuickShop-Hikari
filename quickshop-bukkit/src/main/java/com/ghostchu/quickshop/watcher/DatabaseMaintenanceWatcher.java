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
    private final QuickShop plugin;
    private final ReentrantLock LOCK = new ReentrantLock();
    @Getter
    @Nullable
    private DatabaseStatusHolder result = null;

    public DatabaseMaintenanceWatcher(QuickShop plugin) {
        this.plugin = plugin;
    }

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
            IsolatedScanResult<Long> shopIds = databaseHelper.scanIsolatedShopIds();
            IsolatedScanResult<Long> dataIds = databaseHelper.scanIsolatedDataIds();
            long reportGeneratedAt = System.currentTimeMillis();
            long total = shopIds.getTotal().size() + dataIds.getTotal().size();
            long totalIsolated = shopIds.getIsolated().size() + dataIds.getIsolated().size();
            if (total > 100 && totalIsolated > 500) {
                this.result = new DatabaseStatusHolder(DatabaseStatusHolder.Status.MAINTENANCE_REQUIRED, dataIds, shopIds, reportGeneratedAt);
            } else {
                this.result = new DatabaseStatusHolder(DatabaseStatusHolder.Status.GOOD, dataIds, shopIds, reportGeneratedAt);
            }
            Log.debug("Database Maintenance Watcher: Report generated in " + timer.stopAndGetTimePassed() + "ms, content=" + this.result.toString());
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Database Maintenance Watcher exited with an error", e);
        } finally {
            LOCK.unlock();
        }
    }
}
