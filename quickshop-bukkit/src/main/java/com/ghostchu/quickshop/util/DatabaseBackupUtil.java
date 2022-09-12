package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.logger.Log;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DatabaseBackupUtil {
    private final File dataFolder = QuickShop.getInstance().getDataFolder();
    private final File databaseBackupFolder = new File(dataFolder, "h2-backup");
    private final List<String> databaseBackupList = List.of(
            "shops.mv.db",
            "shops.trace.db",
            "shops.mv.db-journal",
            "shops.mv.db-shm",
            "shops.mv.db-wal"
    );

    public void backup() {
        if (QuickShop.getInstance().getDatabaseDriverType() != QuickShop.DatabaseDriverType.H2) {
            return;
        }
        if (!databaseBackupFolder.exists()) {
            databaseBackupFolder.mkdirs();
        }
        File backupFolder = new File(databaseBackupFolder, String.valueOf(System.currentTimeMillis()));
        backupFolder.mkdirs();
        for (String fileName : databaseBackupList) {
            File file = new File(dataFolder, fileName);
            if (file.exists()) {
                try {
                    Log.debug("AutoBackup: Backing up " + CommonUtil.getRelativePath(file));
                    Files.copy(file.toPath(), new File(backupFolder, fileName).toPath());
                    Log.debug("AutoBackup: Backing up " + CommonUtil.getRelativePath(file) + " successfully.");
                } catch (Exception e) {
                    Log.debug(Level.WARNING, "Failed to backup " + CommonUtil.getRelativePath(file) + ": " + e.getMessage());
                }
            }
        }
        cleanup();
    }

    private void cleanup() {
        File[] fileArray = databaseBackupFolder.listFiles();
        if (fileArray == null) {
            return;
        }
        List<File> files = new ArrayList<>(List.of(fileArray));
        // sort files with lastModified date (reserve)
        files.sort((o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
        int skipped = 0;
        int purged = 0;
        for (File file : files) {
            if (skipped < 20) {
                skipped++;
                continue;
            }
            if (CommonUtil.deleteDirectory(file)) {
                purged++;
            } else {
                Log.debug("AutoBackup: Failed to purge " + CommonUtil.getRelativePath(file));
            }

        }
        Log.debug("AutoBackup: Purged " + purged + " outdated backups.");
    }
}
