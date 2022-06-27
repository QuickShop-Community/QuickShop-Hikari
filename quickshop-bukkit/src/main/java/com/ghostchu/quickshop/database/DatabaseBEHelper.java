//package com.ghostchu.quickshop.database;
//
//import cc.carm.lib.easysql.api.SQLQuery;
//import com.ghostchu.quickshop.QuickShop;
//import com.ghostchu.quickshop.api.shop.Shop;
//import com.ghostchu.quickshop.external.com.ti.ems.jacky.ResultSetToJson;
//import com.ghostchu.quickshop.shop.SimpleShopManager;
//import com.ghostchu.quickshop.util.DatabaseBackupUtil;
//import com.ghostchu.quickshop.util.MsgUtil;
//import com.ghostchu.quickshop.util.Util;
//import com.ghostchu.quickshop.util.logger.Log;
//import com.google.gson.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import org.apache.commons.compress.harmony.unpack200.bytecode.CPMember;
//import org.jetbrains.annotations.NotNull;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
//import java.sql.SQLException;
//import java.util.*;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.logging.Level;
//import java.util.stream.Collectors;
//
//@AllArgsConstructor
//@Data
//public class DatabaseBEHelper {
//    private final SimpleDatabaseHelperV2 helper;
//    private final SimpleShopManager manager;
//    private final QuickShop plugin;
//
//    /**
//     * Export database tables to JSON file
//     *
//     * @param file File to export to
//     * @return The Map contains failed tables and their errors
//     */
//    @NotNull
//    public Map<DataTables, SQLException> exports(@NotNull File file) throws IOException {
//        Map<DataTables, SQLException> fails = new LinkedHashMap<>();
//        JsonObject tablesDump = new JsonObject();
//        for (DataTables table : DataTables.values()) {
//            try (SQLQuery query = table.createQuery().build().execute()) {
//                ResultSet result = query.getResultSet();
//                JsonArray tableData = ResultSetToJson.resultSetToJsonArray(result);
//                tablesDump.add(table.name(), tableData);
//            } catch (SQLException e) {
//                fails.put(table, e);
//                plugin.getLogger().log(Level.WARNING, "Failed to process export task for table " + table.name(), e);
//            }
//        }
//        Files.writeString(file.toPath(), tablesDump.toString());
//        return fails;
//    }
//
//    @NotNull
//    public Map<DataTables, SQLException> imports(@NotNull File file) throws IllegalArgumentException, IOException, InterruptedException {
//        String json = Files.readString(file.toPath());
//        JsonElement root = JsonParser.parseString(json);
//        if (!root.isJsonObject())
//            throw new IllegalArgumentException("Malformed exported data JSON file.");
//        JsonObject tablesDump = root.getAsJsonObject();
//        Map<DataTables, SQLException> fails = new LinkedHashMap<>();
//        // Validate tables
//        if (!validateTables(tablesDump)) {
//            Util.SysPropertiesParseResult parseResult = Util.parsePackageProperly("forceImport");
//            if (!parseResult.asBoolean())
//                throw new IllegalArgumentException("Database schema has been changed since last export. To force import data, set system properties " + parseResult.getParseKey() + " value to true");
//        }
//        plugin.getLogger().info("Unloading shops from server...");
//        // Unloading all shops...
//        Util.mainThreadRun(() -> {
//            manager.getLoadedShops().forEach(Shop::onUnload);
//            plugin.getLogger().info("Removing shops objects from memory...");
//            manager.clear();
//        });
//        plugin.getLogger().info("Clean shop caches...");
//        plugin.getShopCache().invalidateAll();
//        plugin.getLogger().info("Shutting down SQL Manager, Please allow up to 10 secs....");
//        plugin.getSqlManager().getActiveQuery().values().forEach(SQLQuery::close);
//        plugin.getSqlManager().getExecutorPool().shutdown();
//        plugin.getSqlManager().getExecutorPool().awaitTermination(10, TimeUnit.SECONDS);
//        plugin.getSqlManager().setExecutorPool(Executors.newFixedThreadPool(3, (r) -> {
//            Thread thread = new Thread(r, "SQL-Manager-Executor");
//            thread.setDaemon(true);
//            return thread;
//        }));
//        plugin.getLogger().info("Checking and preparing database...");
//        new DatabaseBackupUtil().backup();
//        plugin.getLogger().info("Dropping exists data...");
//        purgeTables();
//        plugin.getLogger().info("Importing data from file, this may need a while. Do not shut down the server.");
//        importTables(tablesDump);
//    }
//
//    private void importTables(@NotNull JsonObject tablesDump) throws IllegalArgumentException {
//        for (DataTables value : DataTables.values()) {
//            JsonElement element = tablesDump.get(value.name());
//            if (!element.isJsonArray())
//                throw new IllegalArgumentException("Malformed exported data JSON file.");
//            JsonArray tableData = element.getAsJsonArray();
//            tableData.
//        }
//    }
//
//    public void jsonArrayImport(@NotNull JsonArray array, @NotNull DataTables table) throws SQLException {
//        try (SQLQuery query = table.createQuery().build().execute()) {
//            ResultSet set = query.getResultSet();
//            ResultSetMetaData rsmd = set.getMetaData();
//            Iterator<JsonElement> elementIterator = array.iterator();
//            while (elementIterator.hasNext()) {
//                JsonElement entryElement = elementIterator.next();
//                if (!entryElement.isJsonObject())
//                    throw new IllegalArgumentException("Malformed exported data JSON file.");
//                JsonObject obj = entryElement.getAsJsonObject();
//                Set<String> jsonColumns = obj.keySet().stream().map(s->s.toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
//                Set<String> sqlColumns = getTableColumns(table);
//                if (!Util.parsePackageProperly("forceImport").asBoolean()) {
//                    if (!jsonColumns.equals(sqlColumns))
//                        throw new IllegalArgumentException("Columns data mismatch.");
//                }
//                Map<String, Object> dataMap = new LinkedHashMap<>();
//                for (String column : sqlColumns) {
//                    JsonElement element = obj.get(column);
//                    if(element.isJsonNull()){
//                        dataMap.put(column, null);continue;
//                    }
//                    if(element.isJsonPrimitive()){
//                        JsonPrimitive primitive = element.getAsJsonPrimitive();
//                        if(primitive.isBoolean()) {
//                            dataMap.put(column, primitive.getAsBoolean()); continue;
//                        }
//                        if(primitive.isNumber()) {
//                            dataMap.put(column, primitive.getAsNumber()); continue;
//                        }
//                        if(primitive.isString()) {
//                            dataMap.put(column, primitive.getAsString());
//                        }
//                    }
//
//                }
//                table.createInsert()
//                        .setColumnNames(sortedColumns)
//                        .setParams()
//            }
//        }
//        return ja;
//    }
//
//    private Set<String> getTableColumns(@NotNull DataTables table) throws SQLException {
//        Set<String> columns = new HashSet<>();
//        try (SQLQuery query = table.createQuery().build().execute()) {
//            ResultSet set = query.getResultSet();
//            ResultSetMetaData rsmd = set.getMetaData();
//            for (int i = 0; i < rsmd.getColumnCount(); i++) {
//                columns.add(rsmd.getColumnName(i + 1).toUpperCase(Locale.ROOT));
//            }
//        }
//    }
//
//    private Map<DataTables, SQLException> purgeTables() {
//        Map<DataTables, SQLException> fails = new LinkedHashMap<>();
//        for (DataTables table : DataTables.values()) {
//            try {
//                table.createDelete().addCondition("1=1").build().execute();
//            } catch (SQLException e) {
//                fails.put(table, e);
//                plugin.getLogger().log(Level.WARNING, "Failed to process purge task for table " + table.name(), e);
//            }
//        }
//        return fails;
//    }
//
//    private boolean validateTables(@NotNull JsonObject object) {
//        for (DataTables value : DataTables.values()) {
//            JsonElement obj = object.get(value.name());
//            if (obj == null || !obj.isJsonArray()) {
//                Log.debug("Mapping for table " + value.name() + " not a valid JsonArray.");
//                return false;
//            }
//        }
//        return true;
//    }
//}
