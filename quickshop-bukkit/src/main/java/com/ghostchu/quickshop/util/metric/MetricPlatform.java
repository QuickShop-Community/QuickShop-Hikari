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
import com.ghostchu.quickshop.util.logger.Log;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * MetricPlatform
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface MetricPlatform<T> {

  /**
   * Retrieves the unique identifier for the project associated with this MetricPlatform.
   *
   * @return a {@code String} representing the unique project identifier.
   */
  String projectID();

  /**
   * Registers this MetricPlatform implementation for tracking or activation within the system.
   * This method should be invoked to enable the platform's functionality and integrate it
   * with the underlying metrics or analytics framework.
   */
  void register();

  /**
   * Retrieves the validated class associated with this MetricPlatform.
   * The validated class represents the type used for compliance or correctness
   * validation within the system's context.
   *
   * @return an instance of type {@code T} representing the validated class.
   */
  Class<T> validatedClass();

  /**
   * Adds a metric to the system for tracking or processing. The metric must be of the validated
   * type associated with this MetricPlatform.
   *
   * @param metric the metric of type {@code T} to be added; must not be null and should conform
   *               to the expectations of the underlying system or validation logic.
   */
  void addMetric(final T metric);

  /**
   * Registers a metric to be tracked or processed by the system, subject to a privacy review
   * based on the provided data type, module name, and reason.
   *
   * @param dataType   the data type of the metric, specifying its classification (e.g., STATISTIC, RESEARCH, DIAGNOSTIC)
   * @param moduleName the name of the module associated with the metric, which will be reformatted internally
   *                   for compatibility and privacy review purposes
   * @param reason     the reason or justification for collecting this metric, used for transparency and approval
   * @param metric     the metric of type {@code T} to be registered; if null, registration will be ignored
   */
  default void registerMetric(final MetricDataType dataType, final String moduleName, final String reason, final T metric) {

    //Don't register if the metric is null
    if(metric == null) {
      return;
    }

    QuickShop.getInstance().getPrivacyController().privacyReview(dataType, moduleName.replace(" ", "_").replace("-", "_").toUpperCase(Locale.ROOT), reason, ()->addMetric(metric), ()->Log.debug("Blocked metric registration: failed privacy reviewing."));
  }

  /**
   * Registers a metric collector by analyzing the provided object's methods for annotated
   * metric collection entries. For each method annotated with {@link MetricCollectEntry}, the
   * method's return value is used as the metric to register, provided it matches the expected
   * validated class. Logs warnings for invalid configurations or failed registrations.
   *
   * @param object the target object to scan for methods annotated with {@link MetricCollectEntry}.
   *               Must not be {@code null}.
   */
  default void registerMetricCollector(@NotNull final Object object) {

    for(final Method method : object.getClass().getDeclaredMethods()) {
      final MetricCollectEntry collectEntry = method.getAnnotation(MetricCollectEntry.class);
      if(collectEntry == null) {
        continue;
      }
      if(!method.getReturnType().isAssignableFrom(validatedClass())) {
        QuickShop.getInstance().logger().warn("Failed loading MetricCollectEntry [{}]: Illegal test returns", method.getName());
        continue;
      }
      try {
        final Object result = method.invoke(object, (Object[])null);
        if(result != null) {
          registerMetric(collectEntry.dataType(), collectEntry.moduleName(), collectEntry.description(), validatedClass().cast(result));
          Log.debug("Registered metrics collector: " + collectEntry.moduleName());
        }
      } catch(final Throwable th) {
        QuickShop.getInstance().logger().warn("Failed to register metrics chart", th);
      }
    }
  }
}