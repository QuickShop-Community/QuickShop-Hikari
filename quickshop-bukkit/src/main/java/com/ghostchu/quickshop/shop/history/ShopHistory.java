package com.ghostchu.quickshop.shop.history;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.ShopOperationEnum;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.database.DataTables;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import lombok.Cleanup;

import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ShopHistory {
    protected final List<Shop> shops;
    protected final Map<Long, Shop> shopsMapping = new HashMap<>();
    private final String shopIdsPlaceHolders;
    private final QuickShop plugin;

    public ShopHistory(QuickShop plugin, List<Shop> shops) {
        this.plugin = plugin;
        this.shops = shops;
        for (Shop shop : shops) {
            long shopId = shop.getShopId();
            if (shopId <= 0) {
                continue;
            }
            shopsMapping.put(shopId, shop);
        }
        this.shopIdsPlaceHolders = generatePlaceHolders(shopsMapping.size());
    }

    private boolean isValidSummaryRecordType(String type) {
        return ShopOperationEnum.PURCHASE_SELLING_SHOP.name().equalsIgnoreCase(type) || ShopOperationEnum.PURCHASE_BUYING_SHOP.name().equalsIgnoreCase(type);
    }

    private String generatePlaceHolders(int size) {
        StringJoiner joiner = new StringJoiner(",");
        for (int i = 0; i < size; i++) {
            joiner.add("?");
        }
        return joiner.toString();
    }

    private void mappingPreparedStatement(PreparedStatement statement, int startAt) throws SQLException {
        List<Long> ids = new ArrayList<>(shopsMapping.keySet());
        for (int i = startAt; i < startAt + ids.size(); i++) {
            statement.setLong(i, ids.get(i - startAt));
        }
    }

    private CompletableFuture<LinkedHashMap<UUID, Long>> summaryTopNValuableCustomers(int n, Instant from, Instant to) {
        return CompletableFuture.supplyAsync(() -> {
            LinkedHashMap<UUID, Long> orderedMap = new LinkedHashMap<>();
            String SQL = "SELECT `buyer`, COUNT(`buyer`) AS `count` FROM %s " +
                    "WHERE `time` >= ? AND `time` <= ? AND `shop` IN (" + this.shopIdsPlaceHolders + ")  GROUP BY `buyer` ORDER BY `count` DESC  LIMIT " + n;
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryTopNValuableCustomers");
                 Connection connection = plugin.getSqlManager().getConnection();
                 PreparedStatement ps = connection.prepareStatement(SQL)) {
                ps.setTimestamp(1, new Timestamp(from.toEpochMilli()));
                ps.setTimestamp(2, new Timestamp(to.toEpochMilli()));
                mappingPreparedStatement(ps, 3);
                perfMonitor.setContext("shopIds=" + shopsMapping.keySet() + ", n=" + n + ", from=" + from + ", to=" + to);
                @Cleanup
                ResultSet set = ps.executeQuery();
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
                    "WHERE `time` >= ? AND `time` <= ? AND `shop` IN (" + this.shopIdsPlaceHolders + ")";
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryUniquePurchasers");
                 Connection connection = plugin.getSqlManager().getConnection();
                 PreparedStatement ps = connection.prepareStatement(SQL)) {
                ps.setTimestamp(1, new Timestamp(from.toEpochMilli()));
                ps.setTimestamp(2, new Timestamp(to.toEpochMilli()));
                mappingPreparedStatement(ps, 3);
                perfMonitor.setContext("shopId=" + shopsMapping.keySet() + ", from=" + from + ", to=" + to);
                @Cleanup
                ResultSet set = ps.executeQuery();
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
                    "WHERE `shop` IN (" + this.shopIdsPlaceHolders + ") GROUP BY `buyer` ORDER BY `count` DESC LIMIT " + n;
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryTopNValuableCustomers");
                 Connection connection = plugin.getSqlManager().getConnection();
                 PreparedStatement ps = connection.prepareStatement(SQL)) {
                mappingPreparedStatement(ps, 1);
                @Cleanup
                ResultSet set = ps.executeQuery();
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
                    "WHERE `shop` IN (" + this.shopIdsPlaceHolders + ")";
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryUniquePurchasers");
                 Connection connection = plugin.getSqlManager().getConnection();
                 PreparedStatement ps = connection.prepareStatement(SQL)) {
                perfMonitor.setContext("shopIds=" + shopsMapping.keySet());
                mappingPreparedStatement(ps, 1);
                @Cleanup
                ResultSet set = ps.executeQuery();
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
                    "WHERE `time` >= ? AND `time` <= ? AND `shop` IN (" + this.shopIdsPlaceHolders + ")";
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryPurchasesBalance");
                 Connection connection = plugin.getSqlManager().getConnection();
                 PreparedStatement ps = connection.prepareStatement(SQL)) {
                ps.setTimestamp(1, new Timestamp(from.toEpochMilli()));
                ps.setTimestamp(2, new Timestamp(to.toEpochMilli()));
                mappingPreparedStatement(ps, 3);
                perfMonitor.setContext("shopIds=" + shopsMapping.keySet() + ", from=" + from + ", to=" + to);
                @Cleanup
                ResultSet set = ps.executeQuery();
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
                    "WHERE `shop` IN (" + this.shopIdsPlaceHolders + ")";
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryPurchasesBalance");
                 Connection connection = plugin.getSqlManager().getConnection();
                 PreparedStatement ps = connection.prepareStatement(SQL)) {
                perfMonitor.setContext("shopIds=" + shopsMapping.keySet());
                mappingPreparedStatement(ps, 1);
                @Cleanup
                ResultSet set = ps.executeQuery();
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
        if ((from == null) != (to == null))
            throw new IllegalStateException("from to must null or not null in same time");
        return CompletableFuture.supplyAsync(() -> {
            String SQL = "SELECT COUNT(*) FROM %s " +
                    "WHERE `time` >= ? AND `time` <= ?  AND `shop` IN (" + this.shopIdsPlaceHolders + ")";
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryPurchasesCount");
                 Connection connection = plugin.getSqlManager().getConnection();
                 PreparedStatement ps = connection.prepareStatement(SQL)) {
                ps.setTimestamp(1, new Timestamp(from.toEpochMilli()));
                ps.setTimestamp(2, new Timestamp(to.toEpochMilli()));
                mappingPreparedStatement(ps, 3);
                perfMonitor.setContext("shopId=" + shopsMapping.keySet() + ", from=" + from + ", to=" + to);
                @Cleanup
                ResultSet set = ps.executeQuery();
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
                    "WHERE `shop` IN (" + this.shopIdsPlaceHolders + ")";
            SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
            try (PerfMonitor perfMonitor = new PerfMonitor("summaryPurchasesCount");
                 Connection connection = plugin.getSqlManager().getConnection();
                 PreparedStatement ps = connection.prepareStatement(SQL)) {
                mappingPreparedStatement(ps, 1);
                perfMonitor.setContext("shopIds=" + shopsMapping.keySet());
                @Cleanup
                ResultSet set = ps.executeQuery();
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
        String SQL = "SELECT * FROM %s WHERE `shop` IN (" + shopIdsPlaceHolders + ") ORDER BY `time` DESC LIMIT " + (page - 1) * pageSize + "," + pageSize;
        SQL = String.format(SQL, DataTables.LOG_PURCHASE.getName());
        try (PerfMonitor perfMonitor = new PerfMonitor("historyPageableQuery");
             Connection connection = plugin.getSqlManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(SQL)) {
            mappingPreparedStatement(ps, 1);
            perfMonitor.setContext("shopIds=" + shopsMapping.keySet() + ", page=" + page + ", pageSize=" + pageSize);
            try (ResultSet set = ps.executeQuery()) {
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
