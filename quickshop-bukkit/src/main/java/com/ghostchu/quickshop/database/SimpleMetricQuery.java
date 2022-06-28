///*
// *  This file is a part of project QuickShop, the name is SimpleMetricQuery.java
// *  Copyright (C) Ghost_chu and contributors
// *
// *  This program is free software: you can redistribute it and/or modify it
// *  under the terms of the GNU General Public License as published by the
// *  Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful, but WITHOUT
// *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *  for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program. If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package com.ghostchu.quickshop.database;
//
//import cc.carm.lib.easysql.api.SQLQuery;
//import com.ghostchu.quickshop.QuickShop;
//import com.ghostchu.quickshop.api.database.MetricQuery;
//import com.ghostchu.quickshop.api.database.MetricRecord;
//import com.ghostchu.quickshop.api.database.ShopOperationEnum;
//import com.ghostchu.quickshop.api.shop.Shop;
//import com.ghostchu.quickshop.util.logger.Log;
//import jdbc.stream.JdbcStream;
//import org.jetbrains.annotations.NotNull;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.*;
//
//public class SimpleMetricQuery implements MetricQuery {
//    private final SimpleDatabaseHelperV2 databaseHelper;
//    private final QuickShop plugin;
//
//    public SimpleMetricQuery(QuickShop plugin, SimpleDatabaseHelperV2 databaseHelper) {
//        this.databaseHelper = databaseHelper;
//        this.plugin = plugin;
//    }
//
//    @NotNull
//    @Override
//    public List<MetricRecord> queryPlayerPurchase(@NotNull UUID player, long timeStart, long timeEnd, boolean descending, @NotNull ShopOperationEnum... filter) {
//        try (SQLQuery query = preparePlayerPurchaseQuery(player, timeStart, timeEnd, -1, descending, filter)) {
//            return wrap(query.getResultSet());
//        } catch (SQLException e) {
//            Log.debug("Failed to perform player query on metrics table: " + e.getMessage());
//            return Collections.emptyList();
//        }
//    }
//
//    @Override
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
//    @Override
//    public List<MetricRecord> queryShopTransaction(@NotNull Shop shop, long timeStart, long timeEnd, int limit, boolean descending, @NotNull ShopOperationEnum... filter) {
//        try (SQLQuery query = prepareShopPurchaseQuery(shop, timeStart, timeEnd, limit, descending, filter)) {
//            return wrap(query.getResultSet());
//        } catch (SQLException e) {
//            Log.debug("Failed to perform shop query on metrics table: " + e.getMessage());
//            return Collections.emptyList();
//        }
//    }
//
//    @Override
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
//    @NotNull
//    private String createFilterArgs(@NotNull ShopOperationEnum... filter) {
//        StringJoiner joiner = new StringJoiner(" OR ");
//        for (int i = 0; i < filter.length; i++) {
//            joiner.add("type=?");
//        }
//        return joiner.toString();
//    }
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
//
//}
