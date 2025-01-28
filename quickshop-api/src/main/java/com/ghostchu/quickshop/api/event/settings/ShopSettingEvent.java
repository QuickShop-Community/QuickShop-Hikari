package com.ghostchu.quickshop.api.event.settings;
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
import com.ghostchu.quickshop.api.event.PhasedEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ShopSettingEvent - Represents an event that is tied to a shop setting.
 *
 * @param <T> Represents the type of the setting that this event is for.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class ShopSettingEvent<T> extends PhasedEvent {

  protected final Shop shop;

  protected T old;
  protected T updated;

  public ShopSettingEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                          final @NotNull T old) {

    super(phase);
    this.shop = shop;
    this.old = old;
    this.updated = old;
  }

  public ShopSettingEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                          final @NotNull T old, final @NotNull T updated) {

    super(phase);
    this.shop = shop;
    this.old = old;
    this.updated = updated;
  }

  public Shop shop() {

    return shop;
  }

  /**
   * Retrieves the old value of the shop setting event.
   *
   * @return the old value of the shop setting event
   */
  public T old() {

    return old;
  }

  /**
   * Retrieves the updated value based on the phase of the event.
   *
   * @return the updated value if the phase is not RETRIEVE, otherwise returns the old value
   */
  public T updated() {

    if(phase == Phase.RETRIEVE) {

      return old;
    }

    return updated;
  }

  /**
   * Sets the updated value for the shop setting event.
   *
   * @param updated the new updated value to be set
   */
  public void updated(final @NotNull T updated) {

    this.updated = updated;
  }
}