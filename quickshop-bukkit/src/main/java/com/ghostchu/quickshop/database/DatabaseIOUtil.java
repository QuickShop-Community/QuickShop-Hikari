package com.ghostchu.quickshop.database;

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Data
public class DatabaseIOUtil {
    private final SimpleDatabaseHelperV2 helper;

    public DatabaseIOUtil(SimpleDatabaseHelperV2 helper) {
        this.helper = helper;
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
                    helper.writeToCSV(result, tableCsv);
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
            helper.importFromCSV(zipFile, table);
            Log.debug("Imported table " + table.getName() + " from " + zipFile.getAbsolutePath());
        }
    }
}
