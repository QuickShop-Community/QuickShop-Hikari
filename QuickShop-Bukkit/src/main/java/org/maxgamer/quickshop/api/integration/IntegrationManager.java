/*
 * This file is a part of project QuickShop, the name is IntegrationManager.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.api.integration;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
/**
 * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public interface IntegrationManager {
    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean isIntegrationClass(@NotNull Class<?> clazz) {
        return clazz.getDeclaredAnnotation(IntegrationStage.class) != null;
    }

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    Map<String, IntegratedPlugin> getIntegrationMap();

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    List<IntegratedPlugin> getIntegrations();

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    void searchAndRegisterPlugins();

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    void register(@NotNull IntegratedPlugin integratedPlugin);

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    void register(@NotNull Class<? extends IntegratedPlugin> integratedPluginClass);

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    void register(@NotNull String integratedPluginName);

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    void unregister(@NotNull String integratedPluginName);

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    void unregisterAll();

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    void unregister(@NotNull IntegratedPlugin integratedPlugin);

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    boolean isRegistered(@NotNull String integrationName);
}
