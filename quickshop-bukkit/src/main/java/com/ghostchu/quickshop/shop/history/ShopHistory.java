package com.ghostchu.quickshop.shop.history;

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.ShopOperationEnum;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.database.DataTables;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.performance.PerfMonitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ShopHistory {
    private final long shopId;
    protected final Shop shop;
    private final QuickShop plugin;

    public ShopHistory(QuickShop plugin, Shop shop) {
        this.plugin = plugin;
        if (shop.getShopId() < 0) {
            throw new IllegalStateException("The shop " + shop + " had no shopId persist in database");
        }
//        Long dataId = plugin.getDatabaseHelper().locateShopDataId( shop.getShopId()).join();
//        if(dataId == null){
//            throw new IllegalStateException("The shop "+shop +" had no dataId persist in database" );
//        }
        this.shopId = shop.getShopId();
        this.shop = shop;
    }

    private boolean isValidSummaryRecordType(String type) {
        return ShopOperationEnum.PURCHASE_SELLING_SHOP.name().equalsIgnoreCase(type) || ShopOperationEnum.PURCHASE_BUYING_SHOP.name().equalsIgnoreCase(type);
    }

    private CompletableFuture<LinkedHashMap<UUID, Long>> summaryTopNValuableCustomers(int n, Instant from, Instant to) {
        return CompletableFuture.supplyAsync(() -> {
            LinkedHashMap<UUID, Long> orderedMap = new LinkedHashMap<>();
            String SQL = "SELECT `buyer`, COUNT(`buyer`) AS `count` FROM %s " +
                    "WHERE `shop`= ? AND `time` >= ? AND `time` <= ? GROUP BY `buyer` ORDER BY `count` DESC  LIMIT " + n;
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryTopNValuableCustomers");
                 SQLQuery query = plugin.getSqlManager().createQuery().withPreparedSQL(SQL).setParams(shopId, from, to).execute()) {
                perfMonitor.setContext("shopId="+shopId+", n="+n+", from="+from+", to="+to);
                ResultSet set = query.getResultSet();
                while (set.next()) {
                    orderedMap.put(UUID.fromString(set.getString("buyer")), set.getLong("count"));
                }
                return orderedMap;
            } catch (SQLException exception) {
                plugin.logger().warn("Failed to summary valuable customers", exception);
                return orderedMap;
            }
        }, QuickExecutor.getShopHistoryQueryExecutor());
    }

    private CompletableFuture<Long> summaryUniquePurchasers(Instant from, Instant to) {
        return CompletableFuture.supplyAsync(() -> {
            String SQL = "SELECT COUNT(DISTINCT `buyer`) FROM %s " +
                    "WHERE `shop`= ? AND `time` >= ? AND `time` <= ?";
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryUniquePurchasers");
                 SQLQuery query = plugin.getSqlManager().createQuery().withPreparedSQL(SQL).setParams(shopId, from, to).execute()) {
                perfMonitor.setContext("shopId="+shopId+", from="+from+", to="+to);
                ResultSet set = query.getResultSet();
                if (set.next()) {
                    return set.getLong(1);
                }
                return 0L;
            } catch (SQLException exception) {
                plugin.logger().warn("Failed to summary unique purchasers", exception);
                return 0L;
            }
        }, QuickExecutor.getShopHistoryQueryExecutor());
    }

    private CompletableFuture<LinkedHashMap<UUID, Long>> summaryTopNValuableCustomers(int n) {
        return CompletableFuture.supplyAsync(() -> {
            LinkedHashMap<UUID, Long> orderedMap = new LinkedHashMap<>();
            String SQL = "SELECT `buyer`, COUNT(`buyer`) AS `count` FROM %s " +
                    "WHERE `shop`= ? GROUP BY `buyer` ORDER BY `count` DESC  LIMIT " + n;
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryTopNValuableCustomers");
                 SQLQuery query = plugin.getSqlManager().createQuery().withPreparedSQL(SQL).setParams(shopId).execute()) {
                perfMonitor.setContext("shopId="+shopId+", n="+n);
                ResultSet set = query.getResultSet();
                while (set.next()) {
                    orderedMap.put(UUID.fromString(set.getString("buyer")), set.getLong("count"));
                }
                return orderedMap;
            } catch (SQLException exception) {
                plugin.logger().warn("Failed to summary valuable customers", exception);
                return orderedMap;
            }
        }, QuickExecutor.getShopHistoryQueryExecutor());
    }

    private CompletableFuture<Long> summaryUniquePurchasers() {
        return CompletableFuture.supplyAsync(() -> {
            String SQL = "SELECT COUNT(DISTINCT `buyer`) FROM %s " +
                    "WHERE `shop`= ?";
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryUniquePurchasers");
                 SQLQuery query = plugin.getSqlManager().createQuery().withPreparedSQL(SQL).setParams(shopId).execute()) {
                perfMonitor.setContext("shopId="+shopId);
                ResultSet set = query.getResultSet();
                if (set.next()) {
                    return set.getLong(1);
                }
                return 0L;
            } catch (SQLException exception) {
                plugin.logger().warn("Failed to summary unique purchasers", exception);
                return 0L;
            }
        }, QuickExecutor.getShopHistoryQueryExecutor());
    }

    private CompletableFuture<Double> summaryPurchasesBalance(Instant from, Instant to) {
        return CompletableFuture.supplyAsync(() -> {
            String SQL = "SELECT SUM(`money`) FROM %s " +
                    "WHERE `shop`= ? AND `time` >= ? AND `time` <= ?";
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryPurchasesBalance");
                 SQLQuery query = plugin.getSqlManager().createQuery().withPreparedSQL(SQL).setParams(shopId, from, to).execute()) {
                perfMonitor.setContext("shopId="+shopId+", from="+from+", to="+to);
                ResultSet set = query.getResultSet();
                if (set.next()) {
                    return set.getDouble(1);
                }
                return 0.0d;
            } catch (SQLException exception) {
                plugin.logger().warn("Failed to summary unique purchasers", exception);
                return 0d;
            }
        }, QuickExecutor.getShopHistoryQueryExecutor());
    }

    private CompletableFuture<Double> summaryPurchasesBalance() {
        return CompletableFuture.supplyAsync(() -> {
            String SQL = "SELECT SUM(`money`) FROM %s " +
                    "WHERE `shop`= ?";
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryPurchasesBalance");
                 SQLQuery query = plugin.getSqlManager().createQuery().withPreparedSQL(SQL).setParams(shopId).execute()) {
                perfMonitor.setContext("shopId="+shopId);
                ResultSet set = query.getResultSet();
                if (set.next()) {
                    return set.getDouble(1);
                }
                return 0.0d;
            } catch (SQLException exception) {
                plugin.logger().warn("Failed to summary unique purchasers", exception);
                return 0d;
            }
        }, QuickExecutor.getShopHistoryQueryExecutor());
    }

    private CompletableFuture<Long> summaryPurchasesCount(Instant from, Instant to) {
        if((from==null) != (to==null))
            throw new IllegalStateException("from to must null or not null in same time");
        return CompletableFuture.supplyAsync(() -> {
            String SQL = "SELECT COUNT(*) FROM %s " +
                    "WHERE `shop`= ? AND `time` >= ? AND `time` <= ?";
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryPurchasesCount");
                 SQLQuery query = plugin.getSqlManager().createQuery().withPreparedSQL(SQL).setParams(shopId, from, to).execute()) {
                perfMonitor.setContext("shopId="+shopId+", from="+from+", to="+to);
                ResultSet set = query.getResultSet();
                if (set.next()) {
                    return set.getLong(1);
                }
                return 0L;
            } catch (SQLException exception) {
                plugin.logger().warn("Failed to summary unique purchasers", exception);
                return 0L;
            }
        }, QuickExecutor.getShopHistoryQueryExecutor());
    }

    private CompletableFuture<Long> summaryPurchasesCount() {
        return CompletableFuture.supplyAsync(() -> {
            String SQL = "SELECT COUNT(*) FROM %s " +
                    "WHERE `shop`= ?";
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryPurchasesCount");
                 SQLQuery query = plugin.getSqlManager().createQuery().withPreparedSQL(SQL).setParams(shopId).execute()) {
                perfMonitor.setContext("shopId="+shopId);
                ResultSet set = query.getResultSet();
                if (set.next()) {
                    return set.getLong(1);
                }
                return 0L;
            } catch (SQLException exception) {
                plugin.logger().warn("Failed to summary unique purchasers", exception);
                return 0L;
            }
        }, QuickExecutor.getShopHistoryQueryExecutor());
    }

    public CompletableFuture<ShopSummary> generateSummary() {
        long recentPurchases24h = summaryPurchasesCount(Instant.now().minus(24, ChronoUnit.HOURS), Instant.now()).join();
        long recentPurchases3d = summaryPurchasesCount(Instant.now().minus(3, ChronoUnit.DAYS), Instant.now()).join();
        long recentPurchases7d = summaryPurchasesCount(Instant.now().minus(7, ChronoUnit.DAYS), Instant.now()).join();
        long recentPurchases30d = summaryPurchasesCount(Instant.now().minus(30, ChronoUnit.DAYS), Instant.now()).join();
        long totalPurchases = summaryPurchasesCount().join();
        double recentPurchasesBalance24h = summaryPurchasesBalance(Instant.now().minus(24, ChronoUnit.HOURS), Instant.now()).join();
        double recentPurchasesBalance3d = summaryPurchasesBalance(Instant.now().minus(3, ChronoUnit.DAYS), Instant.now()).join();
        double recentPurchasesBalance7d = summaryPurchasesBalance(Instant.now().minus(7, ChronoUnit.DAYS), Instant.now()).join();
        double recentPurchasesBalance30d = summaryPurchasesBalance(Instant.now().minus(30, ChronoUnit.DAYS), Instant.now()).join();
        double totalPurchasesBalance = summaryPurchasesBalance().join();
        long totalUniquePurchases = summaryUniquePurchasers().join();
        LinkedHashMap<UUID, Long> valuableCustomers = summaryTopNValuableCustomers(5).join();
        
        return CompletableFuture.supplyAsync(() -> new ShopSummary(
                recentPurchases24h,
                recentPurchases3d,
                recentPurchases7d,
                recentPurchases30d,
                totalPurchases,
                recentPurchasesBalance24h,
                recentPurchasesBalance3d,
                recentPurchasesBalance7d,
                recentPurchasesBalance30d,
                totalPurchasesBalance,
                totalUniquePurchases,
                valuableCustomers
        ));

    }


    public List<ShopHistoryRecord> query(int page, int pageSize) throws SQLException {
        Util.ensureThread(true);
        List<ShopHistoryRecord> historyRecords = new ArrayList<>(pageSize);
        try (PerfMonitor perfMonitor = new PerfMonitor("historyPageableQuery")) {
            SQLQuery query = DataTables.LOG_PURCHASE.createQuery()
                    .addCondition("shop", shopId)
                    .orderBy("time", false)
                    .setPageLimit((page - 1) * pageSize, pageSize)
                    .build().execute();
            perfMonitor.setContext("shopId="+shopId+", page="+page+", pageSize="+pageSize);
            try (query) {
                ResultSet set = query.getResultSet();
                while (set.next()) {
                    if (!isValidSummaryRecordType(set.getString("type"))) {
                        continue;
                    }
                    Timestamp date = set.getTimestamp("time");
                    long shopId = set.getLong("shop");
                    long dataId = set.getLong("data");
                    UUID buyer = UUID.fromString(set.getString("buyer"));
                    ShopOperationEnum shopType = ShopOperationEnum.valueOf(set.getString("type"));
                    int amount = set.getInt("amount");
                    double money = set.getDouble("money");
                    double tax = set.getDouble("tax");
                    historyRecords.add(new ShopHistoryRecord(date, shopId, dataId, buyer, shopType, amount, money, tax));
                }
            }
        }
        return historyRecords;
    }

    public record ShopSummary(long recentPurchases24h, long recentPurchases3d, long recentPurchases7d,
                              long recentPurchases30d, long totalPurchases,
                              double recentPurchasesBalance24h, double recentPurchasesBalance3d,
                              double recentPurchasesBalance7d,
                              double recentPurchasesBalance30d, double totalBalance,
                              long uniquePurchasers, LinkedHashMap<UUID, Long> valuableCustomers) {
        @Override
        public String toString() {
            return "ShopSummary{" +
                    "recentPurchases24h=" + recentPurchases24h +
                    ", recentPurchases3d=" + recentPurchases3d +
                    ", recentPurchases7d=" + recentPurchases7d +
                    ", recentPurchases30d=" + recentPurchases30d +
                    ", totalPurchases=" + totalPurchases +
                    ", recentPurchasesBalance24h=" + recentPurchasesBalance24h +
                    ", recentPurchasesBalance3d=" + recentPurchasesBalance3d +
                    ", recentPurchasesBalance7d=" + recentPurchasesBalance7d +
                    ", recentPurchasesBalance30d=" + recentPurchasesBalance30d +
                    ", totalBalance=" + totalBalance +
                    ", uniquePurchasers=" + uniquePurchasers +
                    '}';
        }
    }

    public record ShopHistoryRecord(Timestamp date, long shopId, long dataId, UUID buyer,
                                    ShopOperationEnum shopType, int amount, double money, double tax
    ) {
    }
}
