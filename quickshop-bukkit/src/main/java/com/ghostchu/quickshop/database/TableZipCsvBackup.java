package com.ghostchu.quickshop.database;

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.relique.jdbc.csv.CsvDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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

/**
 * TableZipCsvBackup
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public final class TableZipCsvBackup {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private static TableSchema readSchemaFromZip(@NotNull final File zipFile, @NotNull final String tableName)
          throws IOException {

    final String entryName = tableName + ".schema.json";
    try(final ZipFile zf = new ZipFile(zipFile)) {
      final ZipEntry entry = zf.getEntry(entryName);
      if(entry == null) return null;

      try(final InputStream in = zf.getInputStream(entry);
          final InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        return GSON.fromJson(reader, TableSchema.class);
      }
    }
  }

  private static Object convertCsvString(final String raw, final ColumnSchema col) throws SQLException {

    if(raw == null) return null;
    final String s = raw.trim();
    if(s.isEmpty()) return null;

    try {
      switch(col.sqlType) {
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
          return Integer.valueOf(s);

        case Types.BIGINT:
          return Long.valueOf(s);

        case Types.FLOAT:
        case Types.REAL:
        case Types.DOUBLE:
          return Double.valueOf(s);

        case Types.DECIMAL:
        case Types.NUMERIC:
          return new BigDecimal(s);

        case Types.BIT:
        case Types.BOOLEAN:
          if(s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes")) return true;
          if(s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no")) return false;
          if(s.equals("1")) return true;
          if(s.equals("0")) return false;
          return Boolean.valueOf(s);

        case Types.DATE:
          return Date.valueOf(s);

        case Types.TIME:
          return Time.valueOf(s);

        case Types.TIMESTAMP:
        case Types.TIMESTAMP_WITH_TIMEZONE:
          return parseTimestampFlexible(s);

        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.NCHAR:
        case Types.NVARCHAR:
        case Types.LONGNVARCHAR:
        default:
          return s;
      }
    } catch(final Exception ex) {

      throw new SQLException("Failed to convert CSV value '" + s + "' for column '" + col.name
                             + "' (sqlType=" + col.sqlType + ", typeName=" + col.typeName + ")", ex);
    }
  }

  private static Timestamp parseTimestampFlexible(final String s) {
    try {
      final Instant inst = Instant.parse(s);
      return Timestamp.from(inst);
    } catch(final Exception ignored) { }

    try {
      final OffsetDateTime odt = OffsetDateTime.parse(s);
      return Timestamp.from(odt.toInstant());
    } catch(final Exception ignored) { }

    try {
      final LocalDateTime ldt = LocalDateTime.parse(s);
      return Timestamp.valueOf(ldt);
    } catch(final Exception ignored) { }

    return Timestamp.valueOf(s);
  }

  public static void exportTables(@NotNull final File zipFile) throws SQLException, IOException {

    zipFile.getParentFile().mkdirs();
    if(!zipFile.exists()) zipFile.createNewFile();

    try(final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {
      for(final DataTables table : DataTables.values()) {
        Log.debug("Exporting table " + table.name());

        final File tableCsv = new File(Util.getCacheFolder(), table.getName() + ".csv");
        tableCsv.getParentFile().mkdirs();
        if(tableCsv.exists()) tableCsv.delete();
        tableCsv.deleteOnExit();

        final TableSchema schema;
        try(final SQLQuery query = table.createQuery().build().execute()) {
          final ResultSet rs = query.getResultSet();

          writeToCSV(rs, tableCsv);

          schema = TableSchema.from(table.getName(), rs.getMetaData());

          Log.debug("Exported table " + table.name() + " to " + tableCsv.getAbsolutePath());
        }

        Log.debug("Adding CSV for " + table.name() + " to zip file");
        out.putNextEntry(new ZipEntry(table.getName() + ".csv"));
        Files.copy(tableCsv.toPath(), out);
        out.closeEntry();

        Log.debug("Adding schema for " + table.name() + " to zip file");
        out.putNextEntry(new ZipEntry(table.getName() + ".schema.json"));
        final byte[] schemaBytes = GSON.toJson(schema).getBytes(StandardCharsets.UTF_8);
        out.write(schemaBytes);
        out.closeEntry();

        Log.debug("Added table " + table.name() + " to zip file");
      }
    }
  }

  public static void importTables(@NotNull final File zipFile) throws SQLException, ClassNotFoundException, IOException {

    for(final DataTables table : DataTables.values()) {
      Log.debug("Purging table " + table.getName());
      table.purgeTable();

      Log.debug("Importing table " + table.getName() + " from " + zipFile.getAbsolutePath());
      importFromCSV(zipFile, table);
      Log.debug("Imported table " + table.getName() + " from " + zipFile.getAbsolutePath());
    }
  }

  public static void importFromCSV(@NotNull final File zipFile, @NotNull final DataTables table)
          throws SQLException, ClassNotFoundException, IOException {

    final TableSchema schema = readSchemaFromZip(zipFile, table.getName());
    if(schema == null) {
      throw new IllegalStateException("Missing schema sidecar for table " + table.getName()
                                      + " (expected " + table.getName() + ".schema.json in zip)");
    }

    Log.debug("Loading CsvDriver...");
    Class.forName("org.relique.jdbc.csv.CsvDriver");

    final String csvTableName = table.logicalName();

    try(final Connection conn = DriverManager.getConnection("jdbc:relique:csv:zip:" + zipFile);
        final Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        final ResultSet results = stmt.executeQuery("SELECT * FROM " + csvTableName)) {

      final String[] columnNames = schema.columnNames();
      Log.debug("Schema columns (" + columnNames.length + "): " + CommonUtil.array2String(columnNames));

      while(results.next()) {
        final Object[] values = new Object[columnNames.length];

        for(int i = 0; i < columnNames.length; i++) {
          final ColumnSchema col = schema.columns.get(i);

          final String raw = results.getString(col.name);
          values[i] = convertCsvString(raw, col);
        }

        Log.debug("Inserting row: " + CommonUtil.array2String(
                Arrays.stream(values).map(v->v == null? "null" : String.valueOf(v)).toArray(String[]::new)
                                                             ));

        table.createInsert()
                .setColumnNames(columnNames)
                .setParams(values)
                .execute();
      }
    }
  }

  public static void writeToCSV(@NotNull final ResultSet set, @NotNull final File csvFile) throws SQLException, IOException {

    if(!csvFile.getParentFile().exists()) csvFile.getParentFile().mkdirs();
    if(!csvFile.exists()) csvFile.createNewFile();

    try(final PrintStream stream = new PrintStream(csvFile)) {
      Log.debug("Writing to CSV file: " + csvFile.getAbsolutePath());
      CsvDriver.writeToCsv(set, stream, true);
    }
  }

  public static final class TableSchema {

    public String table;
    public List<ColumnSchema> columns = new ArrayList<>();

    public static TableSchema from(final String tableName, final ResultSetMetaData md) throws SQLException {

      final TableSchema schema = new TableSchema();
      schema.table = tableName;

      for(int i = 1; i <= md.getColumnCount(); i++) {
        final ColumnSchema col = new ColumnSchema();
        col.name = md.getColumnLabel(i); // label respects aliases; use getColumnName if you prefer
        col.sqlType = md.getColumnType(i);
        col.typeName = md.getColumnTypeName(i);
        col.nullable = (md.isNullable(i) != ResultSetMetaData.columnNoNulls);
        col.precision = md.getPrecision(i);
        col.scale = md.getScale(i);
        schema.columns.add(col);
      }
      return schema;
    }

    public String[] columnNames() {

      final String[] arr = new String[columns.size()];
      for(int i = 0; i < columns.size(); i++) arr[i] = columns.get(i).name;
      return arr;
    }
  }

  public static final class ColumnSchema {

    public String name;
    public int sqlType;
    public String typeName;
    public boolean nullable;
    public int precision;
    public int scale;
  }
}