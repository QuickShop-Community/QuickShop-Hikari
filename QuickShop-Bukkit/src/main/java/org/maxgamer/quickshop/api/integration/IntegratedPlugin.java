/*
 * This file is a part of project QuickShop, the name is IntegratedPlugin.java
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

import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public interface IntegratedPlugin extends Reloadable {
    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    @NotNull String getName();

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    boolean canCreateShopHere(@NotNull Player player, @NotNull Location location);

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    boolean canTradeShopHere(@NotNull Player player, @NotNull Location location);

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default boolean canDeleteShopHere(@NotNull Player player, @NotNull Location location) {
        return false;
    }
    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    void load();

    /**
     * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    void unload();
}
