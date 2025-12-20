package com.ghostchu.quickshop.database;

/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.relique.jdbc.csv.CsvDriver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for transferring DataTables to and from CSV files in a zip format. Provides methods
 * to export all tables to a zip file and to import them back. This class is not meant to be
 * instantiated.
 */
public final class QuickShopCsvTransfer {

  private QuickShopCsvTransfer() { }

  /**
   * Export all DataTables to a zip file.
   *
   * Each entry: <physicalTableName>.csv (physicalTableName = table.getName()).
   */
  public static void exportTablesToZip(@NotNull final File zipFile) throws SQLException, IOException {

    final File parent = zipFile.getParentFile();
    if(parent != null) parent.mkdirs();

    if(!zipFile.exists()) {
      //noinspection ResultOfMethodCallIgnored
      zipFile.createNewFile();
    }

    try(final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
      for(final DataTables table : DataTables.values()) {
        final String physicalName = table.getName();
        final long startNs = System.nanoTime();

        Log.debug("[Backup] Exporting table " + table.name() + " (" + physicalName + ")");

        // temp csv file
        final File tableCsv = new File(Util.getCacheFolder(), physicalName + ".csv");
        //noinspection ResultOfMethodCallIgnored
        tableCsv.getParentFile().mkdirs();
        if(tableCsv.exists()) {
          //noinspection ResultOfMethodCallIgnored
          tableCsv.delete();
        }
        //noinspection ResultOfMethodCallIgnored
        tableCsv.deleteOnExit();

        final long rows;
        try(final SQLQuery query = table.createQuery().build().execute()) {
          final ResultSet result = query.getResultSet();
          rows = writeToCSV(result, tableCsv, physicalName);
        }

        Log.debug("[Backup] Adding " + physicalName + ".csv to zip");
        out.putNextEntry(new ZipEntry(physicalName + ".csv"));
        try(final InputStream in = new BufferedInputStream(new FileInputStream(tableCsv))) {
          in.transferTo(out);
        }
        out.closeEntry();

        final long ms = (System.nanoTime() - startNs) / 1_000_000L;
        Log.debug("[Backup] Exported " + physicalName + " rows=" + rows + " timeMs=" + ms);
      }
    }
  }

  /**
   * Import all tables from a zip created by exportTablesToZip().
   *
   * @param purgeBeforeImport if true, purges each table before importing its rows.
   */
  public static void importTablesFromZip(@NotNull final File zipFile,
                                         final boolean purgeBeforeImport) throws SQLException, IOException, ClassNotFoundException {

    if(!zipFile.exists()) {
      throw new FileNotFoundException("Zip file not found: " + zipFile.getAbsolutePath());
    }

    Log.debug("[Restore] Loading CsvDriver...");
    Class.forName("org.relique.jdbc.csv.CsvDriver");

    Log.debug("[Restore] Import source: " + zipFile.getAbsolutePath());

    for(final DataTables table : DataTables.values()) {
      final String physicalName = table.getName();
      final long startNs = System.nanoTime();

      if(purgeBeforeImport) {
        Log.debug("[Restore] Purging table " + table.name() + " (" + physicalName + ")");
        table.purgeTable();
      }

      Log.debug("[Restore] Importing table " + table.name() + " (" + physicalName + ")");

      final long rows = importSingleTableFromZip(zipFile, table);

      final long ms = (System.nanoTime() - startNs) / 1_000_000L;
      Log.debug("[Restore] Imported " + physicalName + " rows=" + rows + " timeMs=" + ms);
    }
  }

  /**
   * Import a single table from the zip file using CsvDriver.
   *
   * Returns number of imported rows.
   */
  private static long importSingleTableFromZip(@NotNull final File zipFile,
                                               @NotNull final DataTables table) throws SQLException {

    final String physicalName = table.getName();

    final String csvJdbcUrl = "jdbc:relique:csv:zip:" + zipFile;
    final String querySql = "SELECT * FROM \"" + physicalName + "\""; // quote to handle underscores/prefix safely

    try(final Connection conn = DriverManager.getConnection(csvJdbcUrl);
        final Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        final ResultSet results = stmt.executeQuery(querySql)) {

      final ResultSetMetaData metaData = results.getMetaData();
      final int colCount = metaData.getColumnCount();
      final String[] columns = new String[colCount];

      for(int i = 0; i < colCount; i++) {
        columns[i] = metaData.getColumnName(i + 1);
      }

      Log.debug("[Restore] Parsed " + columns.length + " columns for " + physicalName + ": " + Arrays.toString(columns));

      long rows = 0;

      while(results.next()) {
        rows++;

        final Object[] values = new Object[colCount];
        for(int i = 0; i < colCount; i++) {
          final Object v = results.getObject(columns[i]);

          values[i] = normalizeCsvValue(v);
        }

        table.createInsert()
                .setColumnNames(columns)
                .setParams(values)
                .execute();
      }

      return rows;
    } catch(final SQLException ex) {
      // If the zip is missing a CSV entry, CsvDriver will throw "table not found".
      Log.debug("[Restore] Failed importing " + physicalName + ": " + ex.getMessage());
      throw ex;
    }
  }

  /**
   * Writes ResultSet to CSV using CsvDriver, with a small normalization pass to keep BIT(1)
   * consistent across drivers (0/1).
   *
   * Returns number of data rows written.
   */
  private static long writeToCSV(@NotNull final ResultSet set,
                                 @NotNull final File csvFile,
                                 @Nullable final String tableNameForLog) throws SQLException, IOException {

    if(!csvFile.getParentFile().exists()) {
      //noinspection ResultOfMethodCallIgnored
      csvFile.getParentFile().mkdirs();
    }
    if(!csvFile.exists()) {
      //noinspection ResultOfMethodCallIgnored
      csvFile.createNewFile();
    }

    try(final PrintStream stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(csvFile)), false, StandardCharsets.UTF_8)) {
      Log.debug("[Backup] Writing CSV: " + (tableNameForLog == null? csvFile.getName() : tableNameForLog) + " -> " + csvFile.getAbsolutePath());
      CsvDriver.writeToCsv(set, stream, true);
    }

    // Count rows in the file (header excluded) for progress logging
    long rows = 0;
    try(final BufferedReader r = Files.newBufferedReader(csvFile.toPath(), StandardCharsets.UTF_8)) {
      String line;
      boolean first = true;
      while((line = r.readLine()) != null) {
        if(first) {
          first = false;
          continue;
        }
        if(!line.isEmpty()) rows++;
      }
    }
    return rows;
  }

  /**
   * Normalize odd CSV representations that can appear depending on drivers. Keeps behavior
   * backwards compatible with your working importer.
   */
  private static Object normalizeCsvValue(final Object v) {

    if(v == null) return null;

    if(v instanceof final byte[] bytes) {
      // BIT(1) from some sources
      boolean on = false;
      for(final byte b : bytes) {
        if(b != 0) {
          on = true;
          break;
        }
      }
      return on? "1" : "0";
    }

    if(v instanceof final String s) {
      final String t = s.trim();
      // MySQL BIT literals sometimes look like: b'0' / b'1'
      if(t.startsWith("b'") && t.endsWith("'") && t.length() == 4) {
        final char c = t.charAt(2);
        if(c == '0' || c == '1') return String.valueOf(c);
      }
      // common boolean strings
      if(t.equalsIgnoreCase("true")) return "1";
      if(t.equalsIgnoreCase("false")) return "0";
      // NULL token some tools use
      if(t.equals("\\N")) return null;

      return s;
    }

    return v;
  }
}