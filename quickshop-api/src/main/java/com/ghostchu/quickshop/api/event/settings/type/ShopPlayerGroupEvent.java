package com.ghostchu.quickshop.api.event.settings.type;
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
import com.ghostchu.quickshop.api.event.settings.ShopSettingEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * ShopPlayerGroupEvent represents an event that is tied to actions/retrieval of the Shop player group
 * setting for a shop.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class ShopPlayerGroupEvent extends ShopSettingEvent<String> {

  protected final UUID player;

  public ShopPlayerGroupEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                              final @NotNull UUID player, final @NotNull String old) {

    super(phase, shop, old);

    this.player = player;
  }

  public ShopPlayerGroupEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                              final @NotNull UUID player, final @NotNull String old,
                              final @NotNull String updated) {

    super(phase, shop, old, updated);

    this.player = player;
  }

  public UUID player() {

    return player;
  }

  /**
   * Creates a clone of the ShopSettingEvent with the provided newPhase, old value, and updated
   * value.
   *
   * @param newPhase The new phase for the cloned ShopSettingEvent
   * @param old      The old value for the cloned ShopSettingEvent
   * @param updated  The updated value for the cloned ShopSettingEvent
   *
   * @return A new instance of ShopSettingEvent with the specified newPhase, old, and updated values
   */
  @Override
  public ShopPlayerGroupEvent clone(final Phase newPhase, final String old, final String updated) {

    return new ShopPlayerGroupEvent(newPhase, this.shop, this.player, old, updated);
  }

  /**
   * Creates a new instance of PhasedEvent with the specified newPhase.
   *
   * @param newPhase The new Phase for the cloned PhasedEvent
   *
   * @return A new instance of PhasedEvent with the specified newPhase
   */
  @Override
  public ShopPlayerGroupEvent clone(final Phase newPhase) {

    return new ShopPlayerGroupEvent(newPhase, this.shop, this.player, this.old, this.updated);
  }
}