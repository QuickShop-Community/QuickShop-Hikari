/*
 *  This file is a part of project QuickShop, the name is SimpleMetricRecord.java
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

package com.ghostchu.quickshop.database;

import com.ghostchu.quickshop.api.database.MetricRecord;
import com.ghostchu.quickshop.metric.ShopOperationEnum;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class SimpleMetricRecord implements MetricRecord {
    private final long timestamp;
    private final int x;
    private final int y;
    private final int z;
    private final String world;
    private final ShopOperationEnum type;
    private final double total;
    private final double tax;
    private final int amount;
    private final UUID player;


    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public ShopOperationEnum getType() {
        return type;
    }

    @Override
    public double getTotal() {
        return total;
    }

    @Override
    public double getTax() {
        return tax;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public UUID getPlayer() {
        return player;
    }



}
