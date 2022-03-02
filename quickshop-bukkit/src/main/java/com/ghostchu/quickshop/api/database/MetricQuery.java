/*
 *  This file is a part of project QuickShop, the name is MetricQuery.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.api.database;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.metric.ShopOperationEnum;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface MetricQuery {
    @NotNull List<MetricRecord> queryPlayerPurchase(@NotNull UUID player, long timeStart, long timeEnd, boolean descending, @NotNull ShopOperationEnum... filter);

    long queryPlayerPurchaseCount(@NotNull UUID player, long timeStart, long timeEnd, boolean descending, @NotNull ShopOperationEnum... filter);

    @NotNull List<MetricRecord> queryShopTransaction(@NotNull Shop shop, long timeStart, long timeEnd, int limit, boolean descending, @NotNull ShopOperationEnum... filter);

    long queryShopTranslationCount(@NotNull Shop shop, long timeStart, long timeEnd, int limit, boolean descending, @NotNull ShopOperationEnum... filter);
}
