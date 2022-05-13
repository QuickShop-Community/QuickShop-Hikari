/*
 *  This file is a part of project QuickShop, the name is ShopBreakTriedEvent.java
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

import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ShopDeleteOverrideEvent extends AbstractQSEvent {
    private final Shop shop;
    private boolean overrideForAllowed;
    private final UUID requester;
    private final Player requesterPlayer;

    /**
     * Call the event when shop checking for deleting. The ShopDeleteEvent will call after ShopDeleteOverrideEvent if some plugin allowed it.
     * If this event passed, delete button will be shown or shop can be break.
     *
     * @param shop      Target shop
     * @param requester The requester that checking for
     */
    public ShopDeleteOverrideEvent(@NotNull Shop shop, @NotNull UUID requester) {
        this.shop = shop;
        this.requester = requester;
        this.requesterPlayer = Bukkit.getPlayer(requester);
    }

    /**
     * Getting the shop that checking for.
     *
     * @return The shop
     */
    @NotNull
    public Shop getShop() {
        return shop;
    }

    /**
     * Getting the requester that checking for.
     *
     * @return The requester
     */
    @NotNull
    public UUID getRequester() {
        return requester;
    }

    /**
     * Getting the requester's player.
     *
     * @return The requester's player, null if offline or not exists
     */
    @Nullable
    @Deprecated
    public Player getRequesterPlayer() {
        return requesterPlayer;
    }

    /**
     * Does target player can be overridden for deleting a shop.
     *
     * @return True if override is allowed
     */
    public boolean isOverrideForAllowed() {
        return overrideForAllowed;
    }

    /**
     * Set override for deleting a shop.
     *
     * @param overrideForAllowed True if override is allowed
     */
    public void setOverrideForAllowed(boolean overrideForAllowed) {
        this.overrideForAllowed = overrideForAllowed;
    }
}
