/*
 *  This file is a part of project QuickShop, the name is ShopCreateEvent.java
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

package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Calling when new shop creating
 */
public class ShopCreateEvent extends AbstractQSEvent implements QSCancellable {

    @NotNull
    private final UUID creator;

    @Nullable
    @Deprecated
    private final Player player;

    @NotNull
    private final Shop shop;

    private boolean cancelled;
    private @Nullable Component cancelReason;

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
    public void setCancelled(boolean cancel, @Nullable Component reason) {
        this.cancelled = cancel;
        this.cancelReason = reason;
    }

    @Override
    public @Nullable Component getCancelReason() {
        return this.cancelReason;
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
