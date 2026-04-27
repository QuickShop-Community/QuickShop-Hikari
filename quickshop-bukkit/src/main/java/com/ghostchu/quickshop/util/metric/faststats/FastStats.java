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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.metric.MetricPlatform;
import com.ghostchu.quickshop.util.metric.StatCollector;
import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import dev.faststats.core.data.Metric;

/**
 * FastStats
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class FastStats implements MetricPlatform<Metric> {

  private BukkitMetrics.Factory metrics = BukkitMetrics.factory().token(projectID());
  private final StatCollector collector;

  public FastStats(final StatCollector collector) {

    this.collector = collector;
  }
  /**
   * Retrieves the unique identifier for the project associated with this MetricPlatform.
   *
   * @return a {@code String} representing the unique project identifier.
   */
  @Override
  public String projectID() {

    return "7883edc919d2a4fa1bec3d78e190d9a5";
  }

  @Override
  public void register() {

    registerMetricCollector(new FastStatsCollector(collector));
    metrics.create(QuickShop.getInstance().getJavaPlugin());
  }

  /**
   * Retrieves the validated class associated with this MetricPlatform. The validated class
   * represents the type used for compliance or correctness validation within the system's context.
   *
   * @return an instance of type {@code T} representing the validated class.
   */
  @Override
  public Class<Metric> validatedClass() {

    return Metric.class;
  }

  /**
   * Adds a metric to the system for tracking or processing. The metric must be of the validated
   * type associated with this MetricPlatform.
   *
   * @param metric the metric of type {@code T} to be added; must not be null and should conform to
   *               the expectations of the underlying system or validation logic.
   */
  @Override
  public void addMetric(final Metric metric) {
    metrics = metrics.addMetric(metric);
  }
}