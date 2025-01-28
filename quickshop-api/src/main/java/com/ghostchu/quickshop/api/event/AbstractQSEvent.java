package com.ghostchu.quickshop.api.event;

/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
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

import com.ghostchu.quickshop.api.QuickShopAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Parent about all events.
 */
public abstract class AbstractQSEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  protected AbstractQSEvent() {

    super(!Bukkit.isPrimaryThread());
  }

  protected AbstractQSEvent(final boolean async) {

    super(async);
  }

  /**
   * Call event on Bukkit event bus and check if cancelled
   *
   * @return Returns true if cancelled, and false if didn't cancel
   */
  public boolean callCancellableEvent() {

    Bukkit.getPluginManager().callEvent(this);
    if(this instanceof final Cancellable cancellable) {

      return cancellable.isCancelled();
    }
    return false;
  }

  /**
   * Fire event on Bukkit event bus
   */
  public void callEvent() {

    QuickShopAPI.getPluginInstance().getServer().getPluginManager().callEvent(this);
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {

    return getHandlerList();
  }

  public static HandlerList getHandlerList() {

    return HANDLERS;
  }

}
