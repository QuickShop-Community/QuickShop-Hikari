package com.ghostchu.quickshop.util.metric.faststats;

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

import com.ghostchu.quickshop.util.metric.MetricCollectEntry;
import com.ghostchu.quickshop.util.metric.MetricDataType;
import com.ghostchu.quickshop.util.metric.StatCollector;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.CustomChart;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

/**
 * FastStatsCollector
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class FastStatsCollector {

  private final StatCollector collector;

  //TODO: Fully implement this class for research statistics. Will be implemented in future release.
  public FastStatsCollector(final StatCollector collector) {
    this.collector = collector;
  }
}