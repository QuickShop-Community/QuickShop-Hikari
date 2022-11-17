package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.database.DatabaseIOUtil;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class ShopBackupUtil {
    private QuickShop plugin;
    private boolean backupCreated;

    public ShopBackupUtil(QuickShop plugin) {
        this.plugin = plugin;
    }

    public boolean isBackupCreated() {
        return this.backupCreated;
    }

    public boolean isBreakingAllowed() {
        return this.backupCreated || backup();
    }

    public boolean backup() {
        if (backupCreated) {
            return true;
        }
        File file = new File(QuickShop.getInstance().getDataFolder(), "auto-backup-" + System.currentTimeMillis() + ".zip");
        DatabaseIOUtil databaseIOUtil = new DatabaseIOUtil((SimpleDatabaseHelperV2) plugin.getDatabaseHelper());
        try {
            databaseIOUtil.exportTables(file);
            backupCreated = true;
            return true;
        } catch (SQLException | IOException e) {
            return false;
        }
    }
}
