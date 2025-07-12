package com.ghostchu.quickshop.api.event.management;
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

import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an event when a new shop is created. This event is triggered when a new shop is
 * created with associated user information.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class ShopCreateEvent extends ShopEvent {


  protected final QUser user;
  protected final Location location;

  public ShopCreateEvent(final @Nullable Shop shop, final @NotNull QUser user, final @NotNull Location location) {

    super(shop);

    this.user = user;
    this.location = location;
  }

  public ShopCreateEvent(final Phase phase, final @Nullable Shop shop, final @NotNull QUser user, final @NotNull Location location) {

    super(phase, shop);

    this.user = user;
    this.location = location;
  }

  public QUser user() {

    return user;
  }

  public Location location() {

    return location;
  }

  /**
   * Creates a new instance of PhasedEvent with the specified newPhase.
   *
   * @param newPhase The new Phase for the cloned PhasedEvent
   *
   * @return A new instance of PhasedEvent with the specified newPhase
   */
  @Override
  public ShopCreateEvent clone(final Phase newPhase) {

    return new ShopCreateEvent(newPhase, this.shop, this.user, this.location);
  }
}