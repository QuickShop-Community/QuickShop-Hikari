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

/**
 * ShopSettingEvent - Represents an event that is tied to a shop setting.
 * <p>
 * This is a phased event, which has a lifecycle. The Pre, Main, and Post phases of the lifecycle
 * relate to a change happening in the setting. The retrieve phase indicates that it's simple a call
 * to retrieve the setting.
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
                          final T old) {

    super(phase);
    this.shop = shop;
    this.old = old;
    this.updated = old;
  }

  public ShopSettingEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                          final T old, final T updated) {

    super(phase);
    this.shop = shop;
    this.old = old;
    this.updated = updated;
  }

  /**
   * Creates a new instance of PhasedEvent with the specified newPhase.
   *
   * @param newPhase The new Phase for the cloned PhasedEvent
   *
   * @return A new instance of PhasedEvent with the specified newPhase
   */
  @Override
  public PhasedEvent clone(final Phase newPhase) {

    return new ShopSettingEvent<>(newPhase, shop, old, updated);
  }

  /**
   * Creates a clone of the ShopSettingEvent with the provided newPhase, old value, and updated value.
   *
   * @param newPhase The new phase for the cloned ShopSettingEvent
   * @param old The old value for the cloned ShopSettingEvent
   * @param updated The updated value for the cloned ShopSettingEvent
   * @return A new instance of ShopSettingEvent with the specified newPhase, old, and updated values
   */
  public ShopSettingEvent<T> clone(final Phase newPhase, final T old, final T updated) {

    return new ShopSettingEvent<>(newPhase, shop, old, updated);
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

    if(!phase.allowUpdate()) {

      return old;
    }

    return updated;
  }

  /**
   * Sets the updated value for the shop setting event.
   *
   * @param updated the new updated value to be set
   */
  public void updated(final T updated) {

    if(!phase.allowUpdate()) {

      return;
    }

    this.updated = updated;
  }



  public static <T> ShopSettingEvent<T> PRE(final @NotNull Shop shop,
                                            final T old) {

    return new ShopSettingEvent<T>(Phase.PRE, shop, old);
  }

  public static <T> ShopSettingEvent<T> PRE(final @NotNull Shop shop,
                                            final T old, final T updated) {

    return new ShopSettingEvent<T>(Phase.PRE, shop, old, updated);
  }

  public static <T> ShopSettingEvent<T> MAIN(final @NotNull Shop shop,
                                             final T old) {

    return new ShopSettingEvent<T>(Phase.MAIN, shop, old);
  }

  public static <T> ShopSettingEvent<T> MAIN(final @NotNull Shop shop,
                                             final T old, final T updated) {

    return new ShopSettingEvent<T>(Phase.MAIN, shop, old, updated);
  }

  public static <T> ShopSettingEvent<T> POST(final @NotNull Shop shop,
                                             final T old) {

    return new ShopSettingEvent<T>(Phase.POST, shop, old);
  }

  public static <T> ShopSettingEvent<T> POST(final @NotNull Shop shop,
                                             final T old, final T updated) {

    return new ShopSettingEvent<T>(Phase.POST, shop, old, updated);
  }

  public static <T> ShopSettingEvent<T> RETRIEVE(final @NotNull Shop shop,
                                                 final T old) {

    return new ShopSettingEvent<T>(Phase.RETRIEVE, shop, old);
  }

  public static <T> ShopSettingEvent<T> RETRIEVE(final @NotNull Shop shop,
                                                 final T old, final T updated) {

    return new ShopSettingEvent<T>(Phase.RETRIEVE, shop, old, updated);
  }
}