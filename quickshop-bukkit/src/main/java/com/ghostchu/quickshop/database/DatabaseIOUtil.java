package com.ghostchu.quickshop.database;

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Data;
import me.xanium.gemseconomy.file.F;
import org.jetbrains.annotations.NotNull;
import org.relique.jdbc.csv.CsvDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.sql.*;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Data
public class DatabaseIOUtil {
    private final SimpleDatabaseHelperV2 helper;

    public DatabaseIOUtil(SimpleDatabaseHelperV2 helper) {
        this.helper = helper;
    }

    public boolean performBackup(String reason) {
        try {
            if (!QuickShop.getInstance().getConfig().getBoolean("backup-policy." + reason, true)) {
                Log.debug("The backup " + reason + " has been disabled in configuration.");
                return true;
            }

            File backupFile = new File(QuickShop.getInstance().getDataFolder(), "backup");
            if (!backupFile.exists()) {
                if (!backupFile.mkdirs()) {
                    QuickShop.getInstance().logger().warn("[DB Backup] Failed to create backup directory");
                    return false;
                }
            }
            backupFile = new File(backupFile, reason);
            if (!backupFile.exists()) {
                if (!backupFile.mkdirs()) {
                    QuickShop.getInstance().logger().warn("[DB Backup] Failed to create backup sub-reason directory");
                    return false;
                }
            }
            backupFile = new File(backupFile, System.currentTimeMillis() + ".zip");
            try {
                exportTables(backupFile);
                return true;
            } catch (SQLException | IOException e) {
                QuickShop.getInstance().logger().warn("[DB Backup] Failed to create backup", e);
                return false;
            }
        } catch (Throwable throwable) {
            QuickShop.getInstance().logger().warn("[DB Backup] Unexpected error", throwable);
            return false;
        }
    }

    public void exportTables(@NotNull File zipFile) throws SQLException, IOException {
        // zipFile.getParentFile().mkdirs();
        zipFile.createNewFile();
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (DataTables table : DataTables.values()) {
                Log.debug("Exporting table " + table.name());
                File tableCsv = new File(Util.getCacheFolder(), table.getName() + ".csv");
                tableCsv.deleteOnExit();
                try (SQLQuery query = table.createQuery().build().execute()) {
                    ResultSet result = query.getResultSet();
                    writeToCSV(result, tableCsv);
                    Log.debug("Exported table " + table.name() + " to " + tableCsv.getAbsolutePath());
                }
                Log.debug("Adding table " + table.name() + " to zip file");
                out.putNextEntry(new ZipEntry(table.getName() + ".csv"));
                Files.copy(tableCsv.toPath(), out);
                out.closeEntry();
                Log.debug("Added table " + table.name() + " to zip file");
            }
        }
    }

    public void importTables(@NotNull File zipFile) throws SQLException, ClassNotFoundException {
        // Import from CSV
        for (DataTables table : DataTables.values()) {
            Log.debug("Purging table " + table.getName());
            table.purgeTable();
            Log.debug("Importing table " + table.getName() + " from " + zipFile.getAbsolutePath());
            importFromCSV(zipFile, table);
            Log.debug("Imported table " + table.getName() + " from " + zipFile.getAbsolutePath());
        }
    }

    public void importFromCSV(@NotNull File zipFile, @NotNull DataTables table) throws SQLException, ClassNotFoundException {
        Log.debug("Loading CsvDriver...");
        Class.forName("org.relique.jdbc.csv.CsvDriver");
        try (Connection conn = DriverManager.getConnection("jdbc:relique:csv:zip:" + zipFile);
             Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                     ResultSet.CONCUR_READ_ONLY);
             ResultSet results = stmt.executeQuery("SELECT * FROM " + table.getName())) {
            ResultSetMetaData metaData = results.getMetaData();
            String[] columns = new String[metaData.getColumnCount()];
            for (int i = 0; i < columns.length; i++) {
                columns[i] = metaData.getColumnName(i + 1);
            }
            Log.debug("Parsed " + columns.length + " columns: " + CommonUtil.array2String(columns));
            while (results.next()) {
                Object[] values = new String[columns.length];
                for (int i = 0; i < values.length; i++) {
                    Log.debug("Copying column: " + columns[i]);
                    values[i] = results.getObject(columns[i]);
                }
                Log.debug("Inserting row: " + CommonUtil.array2String(Arrays.stream(values).map(Object::toString).toArray(String[]::new)));
                table.createInsert()
                        .setColumnNames(columns)
                        .setParams(values)
                        .execute();
            }
        }
    }

    public void writeToCSV(@NotNull ResultSet set, @NotNull File csvFile) throws SQLException, IOException {
        if (!csvFile.getParentFile().exists()) {
            csvFile.getParentFile().mkdirs();
        }
        if (!csvFile.exists()) {
            csvFile.createNewFile();
        }
        try (PrintStream stream = new PrintStream(csvFile)) {
            Log.debug("Writing to CSV file: " + csvFile.getAbsolutePath());
            CsvDriver.writeToCsv(set, stream, true);
        }
    }

}
