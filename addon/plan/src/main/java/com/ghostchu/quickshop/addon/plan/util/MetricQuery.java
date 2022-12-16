package com.ghostchu.quickshop.addon.plan.util;

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.ShopMetricRecord;
import com.ghostchu.quickshop.api.database.ShopOperationEnum;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.util.logger.Log;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MetricQuery {
    private final SimpleDatabaseHelperV2 databaseHelper;
    private final QuickShop plugin;

    public MetricQuery(QuickShop plugin, SimpleDatabaseHelperV2 databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.plugin = plugin;
    }


    public long queryServerPurchaseCount() {
        String sql = "SELECT COUNT(*) AS result FROM " + databaseHelper.getPrefix() + "log_purchase";
        try (SQLQuery query = databaseHelper.getManager().createQuery().withPreparedSQL(sql).setParams(Collections.emptyList()).execute()) {
            ResultSet set = query.getResultSet();
            if (set.next()) {
                return set.getInt("result");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    @NotNull
    public List<ShopTransactionRecord> queryTransactions(@NotNull Date startTime, long limit, boolean descending) {
        List<ShopTransactionRecord> list = new ArrayList<>();
        try (SQLQuery query = databaseHelper.getManager().createQuery()
                .inTable(databaseHelper.getPrefix() + "log_transaction")
                .addTimeCondition("time", startTime, null)
                .selectColumns()
                .setLimit(1000)
                .orderBy("id", !descending).build().execute()) {
            ResultSet set = query.getResultSet();
            while (set.next()) {
                //"time", "shop", "data", "buyer", "type", "amount", "money", "tax"
                ShopTransactionRecord record = new ShopTransactionRecord(
                        set.getDate("time"),
                        UUID.fromString(set.getString("from")),
                        UUID.fromString(set.getString("to")),
                        set.getString("currency"),
                        set.getDouble("amount"),
                        UUID.fromString(set.getString("tax_currency")),
                        set.getDouble("tax_amount"),
                        set.getString("error")
                );
                list.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return list;
        }
        return list;
    }

    // Use LinkedHashMap forced because we need keep the order.
    public @NotNull LinkedHashMap<ShopMetricRecord, DataRecord> mapToDataRecord(@NotNull List<ShopMetricRecord> metricRecords) throws ExecutionException, InterruptedException {
        // map ShopMetricRecord#getShopId to DataRecord with blocking future
        LinkedHashMap<ShopMetricRecord, DataRecord> dataRecords = new LinkedHashMap();
        for (ShopMetricRecord metricRecord : metricRecords) {
            long shopId = metricRecord.getShopId();
            Long dataId = databaseHelper.locateShopDataId(shopId).get();
            if (dataId == null) {
                Log.debug("dataId is null for shopId " + shopId);
                continue;
            }
            DataRecord dataRecord = databaseHelper.getDataRecord(dataId).get();
            dataRecords.put(metricRecord, dataRecord);
        }
        return dataRecords;

    }

    @NotNull
    public List<ShopMetricRecord> queryServerPurchaseRecords(@NotNull Date startTime, long limit, boolean descending) {
        List<ShopMetricRecord> list = new ArrayList<>();
        try (SQLQuery query = databaseHelper.getManager().createQuery()
                .inTable(databaseHelper.getPrefix() + "log_purchase")
                .addTimeCondition("time", startTime, null)
                .selectColumns()
                .setLimit(1000)
                .orderBy("id", !descending).build().execute()) {
            ResultSet set = query.getResultSet();
            while (set.next()) {
                //"time", "shop", "data", "buyer", "type", "amount", "money", "tax"
                ShopMetricRecord record = ShopMetricRecord.builder()
                        .time(set.getDate("time").getTime())
                        .shopId(set.getLong("shop"))
                        .type(ShopOperationEnum.valueOf(set.getString("type")))
                        .total(set.getDouble("money"))
                        .tax(set.getDouble("tax"))
                        .amount(set.getInt("amount"))
                        .player(UUID.fromString(set.getString("buyer")))
                        .build();
                list.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return list;
        }
        return list;
    }

    //
//
//
//
//
//
//
//    @NotNull
//    public List<MetricRecord> queryPlayerPurchase(@NotNull UUID player, long timeStart, long timeEnd, boolean descending, @NotNull ShopOperationEnum... filter) {
//        try (SQLQuery query = preparePlayerPurchaseQuery(player, timeStart, timeEnd, -1, descending, filter)) {
//            return wrap(query.getResultSet());
//        } catch (SQLException e) {
//            Log.debug("Failed to perform player query on metrics table: " + e.getMessage());
//            return Collections.emptyList();
//        }
//    }
//
//    public long queryPlayerPurchaseCount(@NotNull UUID player, long timeStart, long timeEnd, boolean descending, @NotNull ShopOperationEnum... filter) {
//        try (SQLQuery query = preparePlayerPurchaseQuery(player, timeStart, timeEnd, -1, descending, filter)) {
//            return JdbcStream.stream(query.getResultSet()).count();
//        } catch (SQLException e) {
//            Log.debug("Failed to perform player query on metrics table: " + e.getMessage());
//            return -1;
//        }
//    }
//
//    @NotNull
//    public List<MetricRecord> queryShopTransaction(@NotNull Shop shop, long timeStart, long timeEnd, int limit, boolean descending, @NotNull ShopOperationEnum... filter) {
//        try (SQLQuery query = prepareShopPurchaseQuery(shop, timeStart, timeEnd, limit, descending, filter)) {
//            return wrap(query.getResultSet());
//        } catch (SQLException e) {
//            Log.debug("Failed to perform shop query on metrics table: " + e.getMessage());
//            return Collections.emptyList();
//        }
//    }
//
//    public long queryShopTranslationCount(@NotNull Shop shop, long timeStart, long timeEnd, int limit, boolean descending, @NotNull ShopOperationEnum... filter) {
//        try (SQLQuery query = prepareShopPurchaseQuery(shop, timeStart, timeEnd, limit, descending, filter)) {
//            return JdbcStream.stream(query.getResultSet()).count();
//        } catch (SQLException e) {
//            Log.debug("Failed to perform player query on metrics table: " + e.getMessage());
//            return -1;
//        }
//    }
//
//
//
//
    private String createFilterArgs(@NotNull ShopOperationEnum... filter) {
        StringJoiner joiner = new StringJoiner(" OR ");
        for (int i = 0; i < filter.length; i++) {
            joiner.add("type=?");
        }
        return joiner.toString();
    }

    //
//    @NotNull
//    private SQLQuery preparePlayerPurchaseQuery(@NotNull UUID player, long timeStart, long timeEnd, int limit, boolean descending, @NotNull ShopOperationEnum... filter) throws SQLException {
//        String sql;
//        if (limit == -1 || limit == Integer.MAX_VALUE) {
//            sql = "SELECT * FROM " + databaseHelper.getPrefix() + "metrics WHERE player=? AND time>=? AND time<=? AND (" + createFilterArgs(filter) + ") ORDER BY time " + (descending ? "DESC" : "ASC");
//        } else {
//            sql = "SELECT * FROM " + databaseHelper.getPrefix() + "metrics WHERE player=? AND time>=? AND time<=? AND (" + createFilterArgs(filter) + ") ORDER BY time " + (descending ? "DESC" : "ASC") + " LIMIT " + limit;
//        }
//        List<Object> params = new ArrayList<>();
//        params.add(player);
//        params.add(timeStart);
//        params.add(timeEnd);
//        params.addAll(Arrays.stream(filter).map(ShopOperationEnum::name).toList());
//        return databaseHelper.getManager().createQuery().withPreparedSQL(sql).setParams(params).execute();
//    }
//
//    @NotNull
//    private SQLQuery prepareShopPurchaseQuery(@NotNull Shop shop, long timeStart, long timeEnd, int limit, boolean descending, @NotNull ShopOperationEnum... filter) throws SQLException {
//        String sql;
//        if (limit == -1 || limit == Integer.MAX_VALUE) {
//            sql = "SELECT * FROM " + databaseHelper.getPrefix() + "metrics WHERE x=? AND y=? AND z=? AND world= ? AND time>=? AND time<=? AND (" + createFilterArgs(filter) + ") ORDER BY time " + (descending ? "DESC" : "ASC");
//        } else {
//            sql = "SELECT * FROM " + databaseHelper.getPrefix() + "metrics WHERE x=? AND y=? AND z=? AND world= ? AND time>=? AND time<=? AND (" + createFilterArgs(filter) + ") ORDER BY time " + (descending ? "DESC" : "ASC") + " LIMIT " + limit;
//        }
//        List<Object> params = new ArrayList<>();
//        params.add(shop.getLocation().getBlockX());
//        params.add(shop.getLocation().getBlockY());
//        params.add(shop.getLocation().getBlockZ());
//        params.add(shop.getLocation().getWorld().getName());
//        params.add(timeStart);
//        params.add(timeEnd);
//        params.addAll(Arrays.stream(filter).map(ShopOperationEnum::name).toList());
//        return databaseHelper.getManager().createQuery().withPreparedSQL(sql).setParams(params).execute();
//    }
//
//
//    @NotNull
//    private List<MetricRecord> wrap(@NotNull ResultSet set) {
//        return JdbcStream.stream(set).map(rs -> {
//            try {
//                return new SimpleMetricRecord(rs.getLong("time"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getString("world"), ShopOperationEnum.valueOf(rs.getString("type")), rs.getDouble("total"), rs.getDouble("tax"), rs.getInt("amount"), UUID.fromString(rs.getString("player")));
//            } catch (SQLException e) {
//                Log.debug("Failed to perform query on metrics table: " + e.getMessage());
//                return null;
//            }
//        }).filter(Objects::nonNull).map(record -> (MetricRecord) record).toList();
//    }
    @NotNull
    private Date daysAgo(int ago) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -ago);
        return cal.getTime();
    }
}
