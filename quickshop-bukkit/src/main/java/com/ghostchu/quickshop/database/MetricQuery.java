package com.ghostchu.quickshop.database;

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.ShopMetricRecord;
import com.ghostchu.quickshop.api.database.ShopOperationEnum;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MetricQuery {

  private final SimpleDatabaseHelperV2 databaseHelper;
  private final QuickShop plugin;

  public MetricQuery(final QuickShop plugin, final SimpleDatabaseHelperV2 databaseHelper) {

    this.databaseHelper = databaseHelper;
    this.plugin = plugin;
  }


  public long queryServerPurchaseCount() {

    final String sql = "SELECT COUNT(*) AS result FROM " + databaseHelper.getPrefix() + "log_purchase";
    try(SQLQuery query = databaseHelper.getManager().createQuery().withPreparedSQL(sql).setParams(Collections.emptyList()).execute()) {
      final ResultSet set = query.getResultSet();
      if(set.next()) {
        return set.getInt("result");
      } else {
        return -1;
      }
    } catch(SQLException e) {
      return -1;
    }
  }

  @NotNull
  public List<ShopTransactionRecord> queryTransactions(@NotNull final Date startTime, final long limit, final boolean descending) {

    final List<ShopTransactionRecord> list = new ArrayList<>();
    try(SQLQuery query = databaseHelper.getManager().createQuery()
            .inTable(databaseHelper.getPrefix() + "log_transaction")
            .addTimeCondition("time", startTime, null)
            .selectColumns()
            .setLimit(1000)
            .orderBy("id", !descending).build().execute()) {
      final ResultSet set = query.getResultSet();
      while(set.next()) {
        //"time", "shop", "data", "buyer", "type", "amount", "money", "tax"
        final ShopTransactionRecord record = new ShopTransactionRecord(
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
    } catch(SQLException e) {
      plugin.logger().warn("Querying transactions failed.", e);
      return list;
    }
    return list;
  }

  // Use LinkedHashMap forced because we need keep the order.
  public @NotNull LinkedHashMap<ShopMetricRecord, DataRecord> mapToDataRecord(@NotNull final List<ShopMetricRecord> metricRecords) throws ExecutionException, InterruptedException {
    // map ShopMetricRecord#getShopId to DataRecord with blocking future
    final LinkedHashMap<ShopMetricRecord, DataRecord> dataRecords = new LinkedHashMap<>();
    for(final ShopMetricRecord metricRecord : metricRecords) {
      final long shopId = metricRecord.getShopId();
      final Long dataId = databaseHelper.locateShopDataId(shopId).get();
      if(dataId == null) {
        Log.debug("dataId is null for shopId " + shopId);
        continue;
      }
      final DataRecord dataRecord = databaseHelper.getDataRecord(dataId).get();
      dataRecords.put(metricRecord, dataRecord);
    }
    return dataRecords;

  }

  @NotNull
  public List<ShopMetricRecord> queryServerPurchaseRecords(@NotNull final Date startTime, final int limit, final boolean descending) {

    final List<ShopMetricRecord> list = new ArrayList<>();
    try(SQLQuery query = databaseHelper.getManager().createQuery()
            .inTable(databaseHelper.getPrefix() + "log_purchase")
            .addTimeCondition("time", startTime, null)
            .selectColumns()
            .setLimit(limit)
            .orderBy("id", !descending).build().execute()) {
      final ResultSet set = query.getResultSet();
      while(set.next()) {
        //"time", "shop", "data", "buyer", "type", "amount", "money", "tax"
        final ShopMetricRecord record = ShopMetricRecord.builder()
                .time(set.getDate("time").getTime())
                .shopId(set.getLong("shop"))
                .type(ShopOperationEnum.valueOf(set.getString("type")))
                .total(set.getDouble("money"))
                .tax(set.getDouble("tax"))
                .amount(set.getInt("amount"))
                .player(QUserImpl.createSync(plugin.getPlayerFinder(), set.getString("buyer")))
                .build();
        list.add(record);
      }
    } catch(SQLException e) {
      plugin.logger().warn("Querying transactions failed.", e);
      return list;
    }
    return list;
  }

  @Data
  @AllArgsConstructor
  @Builder
  public static class ShopTransactionRecord {

    private Date time;
    private UUID from;
    private UUID to;
    private String currency;
    private double amount;
    private UUID taxAccount;
    private double taxAmount;
    private String error;
  }
}
