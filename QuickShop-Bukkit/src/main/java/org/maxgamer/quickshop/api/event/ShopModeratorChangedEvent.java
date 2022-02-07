/*
 * This file is a part of project QuickShop, the name is ShopModeratorChangedEvent.java
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

package org.maxgamer.quickshop.api.event;

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.api.shop.ShopModerator;

/**
 * Calling when moderator was changed, Can't cancel
 */
public class ShopModeratorChangedEvent extends AbstractQSEvent {

    @NotNull
    private final ShopModerator moderator;

    @NotNull
    private final Shop shop;

    /**
     * Will call when shop moderator was changed.
     * Shop moderator included owner itself.
     *
     * @param shop          Target shop
     * @param shopModerator The shop moderator
     */
    public ShopModeratorChangedEvent(@NotNull Shop shop, @NotNull ShopModerator shopModerator) {
        this.shop = shop;
        this.moderator = shopModerator;
    }

    /**
     * Gets the shop moderators
     *
     * @return the shop
     */
    public @NotNull ShopModerator getModerator() {
        return this.moderator;
    }

    /**
     * Gets the shop
     *
     * @return the shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }
}
