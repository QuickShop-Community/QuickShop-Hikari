package com.ghostchu.quickshop.util.metric;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.metric.bstats.BStats;
import com.ghostchu.quickshop.util.metric.faststats.FastStats;

/**
 * MetricManager
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class MetricManager {

  private final BStats bStats;
  private final FastStats fastStats;
  private final StatCollector collector;

  public MetricManager() {

    this.collector = new StatCollector(QuickShop.getInstance());

    QuickShop.getInstance().logger().info("Initializing bStats....");
    this.bStats = new BStats(collector);

    QuickShop.getInstance().logger().info("Initializing FastStats....");
    this.fastStats = new FastStats(collector);
  }

  public void initPlatforms() {

    QuickShop.getInstance().logger().info("Registering bStats....");
    bStats.register();

    QuickShop.getInstance().logger().info("Registering FastStats....");
    fastStats.register();
  }
}