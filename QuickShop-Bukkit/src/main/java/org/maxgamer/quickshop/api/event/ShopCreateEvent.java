/*
 * This file is a part of project QuickShop, the name is ShopCreateEvent.java
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

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.UUID;

/**
 * Calling when new shop creating
 */
public class ShopCreateEvent extends AbstractQSEvent implements Cancellable {

    @NotNull
    private final UUID creator;

    @Nullable
    @Deprecated
    private final Player player;

    @NotNull
    private final Shop shop;

    private boolean cancelled;

    /**
     * Call when have a new shop was creating.
     *
     * @param shop    Target shop
     * @param creator The player creating the shop, the player might offline/not exist if creating by a plugin.
     */
    public ShopCreateEvent(@NotNull Shop shop, @NotNull UUID creator) {
        this.shop = shop;
        this.creator = creator;
        this.player = QuickShop.getInstance().getServer().getPlayer(creator);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Gets the creator of this shop
     *
     * @return The creator, may be a online/offline/virtual player
     */
    public @NotNull UUID getCreator() {
        return this.creator;
    }

    /**
     * Gets the creator that is the shop
     *
     * @return Player or null when player not exists or offline
     * @deprecated Now creator not only players but also virtual or offline
     */
    @Deprecated
    public @Nullable Player getPlayer() {
        return this.player;
    }

    /**
     * Gets the shop created
     *
     * @return The shop that created
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }
}
