package com.ghostchu.quickshop.api.event.management;

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

import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * ShopTeleportEvent
 *
 * @author creatorfromhell
 * @since 6.3.0.4
 */
public class ShopTeleportEvent extends ShopEvent {

  protected final Player player;
  protected Location teleportLocation;

  public ShopTeleportEvent(final @NotNull Shop shop, final @NotNull Player player, final @NotNull Location teleportLocation) {

    super(shop);
    this.player = player;
    this.teleportLocation = teleportLocation;
  }

  public ShopTeleportEvent(final Phase phase, final @NotNull Shop shop, final @NotNull Player player, final @NotNull Location teleportLocation) {

    super(phase, shop);
    this.player = player;
    this.teleportLocation = teleportLocation;
  }

  /**
   * Creates a new instance of PhasedEvent with the specified newPhase.
   *
   * @param newPhase The new Phase for the cloned PhasedEvent
   *
   * @return A new instance of PhasedEvent with the specified newPhase
   */
  @Override
  public ShopTeleportEvent clone(final Phase newPhase) {

    return new ShopTeleportEvent(newPhase, this.shop, this.player, this.teleportLocation);
  }

  public Location teleportLocation() {

    return teleportLocation;
  }

  public void teleportLocation(final Location teleportLocation) {

    this.teleportLocation = teleportLocation;
  }
}