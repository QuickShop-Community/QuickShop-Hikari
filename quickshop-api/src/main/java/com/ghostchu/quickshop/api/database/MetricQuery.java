///*
// *  This file is a part of project QuickShop, the name is MetricQuery.java
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
//package com.ghostchu.quickshop.api.database;
//
//import com.ghostchu.quickshop.api.shop.Shop;
//import com.google.common.annotations.Beta;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.List;
//import java.util.UUID;
//
///**
// * Perform a metric query from database with quick way.
// * *BETA*
// */
//@Beta
//public interface MetricQuery {
//    /**
//     * Query player purchase events from database
//     *
//     * @param player     player's uuid
//     * @param timeStart  start time
//     * @param timeEnd    end time
//     * @param descending true if descending order
//     * @param filter     filter shop operation
//     * @return purchases
//     */
//    @NotNull List<MetricRecord> queryPlayerPurchase(@NotNull UUID player, long timeStart, long timeEnd, boolean descending, @NotNull ShopOperationEnum... filter);
//
//    /**
//     * Query shop purchases count from database
//     *
//     * @param player     player's uuid
//     * @param timeStart  start time
//     * @param timeEnd    end time
//     * @param descending true if descending order
//     * @param filter     filter shop operation
//     * @return purchases counting
//     */
//    long queryPlayerPurchaseCount(@NotNull UUID player, long timeStart, long timeEnd, boolean descending, @NotNull ShopOperationEnum... filter);
//
//    /**
//     * Query shop translation events from database
//     *
//     * @param shop       shop
//     * @param timeStart  start time
//     * @param timeEnd    end time
//     * @param limit      limit
//     * @param descending true if descending order
//     * @param filter     filter shop operation
//     * @return translations
//     */
//    @NotNull List<MetricRecord> queryShopTransaction(@NotNull Shop shop, long timeStart, long timeEnd, int limit, boolean descending, @NotNull ShopOperationEnum... filter);
//
//    /**
//     * Query shop translation count from database
//     *
//     * @param shop       shop
//     * @param timeStart  start time
//     * @param timeEnd    end time
//     * @param limit      limit
//     * @param descending true if descending order
//     * @param filter     filter shop operation
//     * @return translations counting
//     */
//    long queryShopTranslationCount(@NotNull Shop shop, long timeStart, long timeEnd, int limit, boolean descending, @NotNull ShopOperationEnum... filter);
//}
