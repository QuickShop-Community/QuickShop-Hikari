/*
 *  This file is a part of project QuickShop, the name is ShopControlPanelOpenEvent.java
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
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when control panel opened for
 */
public class ShopControlPanelOpenEvent extends AbstractQSEvent implements QSCancellable {
    private final Shop shop;
    private final CommandSender sender;
    private boolean cancelled = false;
    private @Nullable Component cancelReason;

    /**
     * Called before shop control panel message.
     *
     * @param shop   The shop bought from
     * @param sender The player which receiving shop control panel message
     */
    public ShopControlPanelOpenEvent(@NotNull Shop shop, @NotNull CommandSender sender) {
        this.shop = shop;
        this.sender = sender;
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
     * Gets the shop of control panel opened
     *
     * @return The shop
     */
    public Shop getShop() {
        return this.shop;
    }

    /**
     * Get the sender that opened control panel
     *
     * @return The sender
     */
    public CommandSender getSender() {
        return this.sender;
    }
}
