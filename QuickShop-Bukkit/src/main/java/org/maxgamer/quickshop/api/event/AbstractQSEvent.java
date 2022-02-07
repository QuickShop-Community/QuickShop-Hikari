/*
 * This file is a part of project QuickShop, the name is AbstractQSEvent.java
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

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;

public abstract class AbstractQSEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public AbstractQSEvent() {
        super(!Bukkit.isPrimaryThread());
    }

    public AbstractQSEvent(boolean async) {
        super(async);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Fire event on Bukkit event bus
     */
    public void callEvent() {
        QuickShop.getInstance().getServer().getPluginManager().callEvent(this);
    }

    /**
     * Call event on Bukkit event bus and check if cancelled
     *
     * @return Returns true if cancelled, and false if didn't cancel
     */
    public boolean callCancellableEvent() {
        QuickShop.getInstance().getServer().getPluginManager().callEvent(this);
        if (this instanceof Cancellable) {
            return ((Cancellable) this).isCancelled();
        }
        return false;
    }

}
