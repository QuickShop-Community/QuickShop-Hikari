package com.ghostchu.quickshop.database;

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.ShopMetricRecord;
import com.ghostchu.quickshop.api.database.ShopOperationEnum;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.logger.Log;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
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
  @NotNull
  public LinkedHashMap<ShopMetricRecord, DataRecord> mapToDataRecord(@NotNull final List<ShopMetricRecord> metricRecords) throws ExecutionException, InterruptedException {
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

  public static class ShopTransactionRecord {

    private Date time;
    private UUID from;
    private UUID to;
    private String currency;
    private double amount;
    private UUID taxAccount;
    private double taxAmount;
    private String error;

    public static class ShopTransactionRecordBuilder {

      private Date time;
      private UUID from;
      private UUID to;
      private String currency;
      private double amount;
      private UUID taxAccount;
      private double taxAmount;
      private String error;

      ShopTransactionRecordBuilder() {

      }

      public MetricQuery.ShopTransactionRecord.ShopTransactionRecordBuilder time(final Date time) {

        this.time = time;
        return this;
      }

      public MetricQuery.ShopTransactionRecord.ShopTransactionRecordBuilder from(final UUID from) {

        this.from = from;
        return this;
      }

      public MetricQuery.ShopTransactionRecord.ShopTransactionRecordBuilder to(final UUID to) {

        this.to = to;
        return this;
      }

      public MetricQuery.ShopTransactionRecord.ShopTransactionRecordBuilder currency(final String currency) {

        this.currency = currency;
        return this;
      }

      public MetricQuery.ShopTransactionRecord.ShopTransactionRecordBuilder amount(final double amount) {

        this.amount = amount;
        return this;
      }

      public MetricQuery.ShopTransactionRecord.ShopTransactionRecordBuilder taxAccount(final UUID taxAccount) {

        this.taxAccount = taxAccount;
        return this;
      }

      public MetricQuery.ShopTransactionRecord.ShopTransactionRecordBuilder taxAmount(final double taxAmount) {

        this.taxAmount = taxAmount;
        return this;
      }

      public MetricQuery.ShopTransactionRecord.ShopTransactionRecordBuilder error(final String error) {

        this.error = error;
        return this;
      }

      public MetricQuery.ShopTransactionRecord build() {

        return new MetricQuery.ShopTransactionRecord(this.time, this.from, this.to, this.currency, this.amount, this.taxAccount, this.taxAmount, this.error);
      }

      @Override
      public String toString() {

        return "MetricQuery.ShopTransactionRecord.ShopTransactionRecordBuilder(time=" + this.time + ", from=" + this.from + ", to=" + this.to + ", currency=" + this.currency + ", amount=" + this.amount + ", taxAccount=" + this.taxAccount + ", taxAmount=" + this.taxAmount + ", error=" + this.error + ")";
  }
  }

    public static MetricQuery.ShopTransactionRecord.ShopTransactionRecordBuilder builder() {

      return new MetricQuery.ShopTransactionRecord.ShopTransactionRecordBuilder();
    }

    public Date getTime() {

      return this.time;
    }

    public UUID getFrom() {

      return this.from;
    }

    public UUID getTo() {

      return this.to;
    }

    public String getCurrency() {

      return this.currency;
    }

    public double getAmount() {

      return this.amount;
    }

    public UUID getTaxAccount() {

      return this.taxAccount;
    }

    public double getTaxAmount() {

      return this.taxAmount;
    }

    public String getError() {

      return this.error;
    }

    public void setTime(final Date time) {

      this.time = time;
    }

    public void setFrom(final UUID from) {

      this.from = from;
    }

    public void setTo(final UUID to) {

      this.to = to;
    }

    public void setCurrency(final String currency) {

      this.currency = currency;
    }

    public void setAmount(final double amount) {

      this.amount = amount;
    }

    public void setTaxAccount(final UUID taxAccount) {

      this.taxAccount = taxAccount;
    }

    public void setTaxAmount(final double taxAmount) {

      this.taxAmount = taxAmount;
    }

    public void setError(final String error) {

      this.error = error;
    }

    @Override
    public boolean equals(final Object o) {

      if(o == this) return true;
      if(!(o instanceof MetricQuery.ShopTransactionRecord)) return false;
      final MetricQuery.ShopTransactionRecord other = (MetricQuery.ShopTransactionRecord)o;
      return Double.compare(this.getAmount(), other.getAmount()) == 0
             && Double.compare(this.getTaxAmount(), other.getTaxAmount()) == 0
             && Objects.equals(this.getTime(), other.getTime())
             && Objects.equals(this.getFrom(), other.getFrom())
             && Objects.equals(this.getTo(), other.getTo())
             && Objects.equals(this.getCurrency(), other.getCurrency())
             && Objects.equals(this.getTaxAccount(), other.getTaxAccount())
             && Objects.equals(this.getError(), other.getError());
    }

    @Override
    public int hashCode() {

      return Objects.hash(this.getAmount(), this.getTaxAmount(), this.getTime(), this.getFrom(), this.getTo(), this.getCurrency(), this.getTaxAccount(), this.getError());
    }

    @Override
    public String toString() {

      return "MetricQuery.ShopTransactionRecord(time=" + this.getTime() + ", from=" + this.getFrom() + ", to=" + this.getTo() + ", currency=" + this.getCurrency() + ", amount=" + this.getAmount() + ", taxAccount=" + this.getTaxAccount() + ", taxAmount=" + this.getTaxAmount() + ", error=" + this.getError() + ")";
    }

    public ShopTransactionRecord(final Date time, final UUID from, final UUID to, final String currency, final double amount, final UUID taxAccount, final double taxAmount, final String error) {

      this.time = time;
      this.from = from;
      this.to = to;
      this.currency = currency;
      this.amount = amount;
      this.taxAccount = taxAccount;
      this.taxAmount = taxAmount;
      this.error = error;
    }
  }
}
