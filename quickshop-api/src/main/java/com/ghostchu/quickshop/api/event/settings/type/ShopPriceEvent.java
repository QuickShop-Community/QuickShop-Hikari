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

/**
 * ShopPriceEvent represents an event that is tied to actions/retrieval of the Shop price setting for
 * a shop.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class ShopPriceEvent extends ShopSettingEvent<Double> {

  public ShopPriceEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                        final @NotNull Double old) {

    super(phase, shop, old);
  }

  public ShopPriceEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                        final @NotNull Double old, final @NotNull Double updated) {

    super(phase, shop, old, updated);
  }

  /**
   * Creates a new instance of PhasedEvent with the specified newPhase.
   *
   * @param newPhase The new Phase for the cloned PhasedEvent
   *
   * @return A new instance of PhasedEvent with the specified newPhase
   */
  @Override
  public ShopPriceEvent clone(final Phase newPhase) {

    return new ShopPriceEvent(newPhase, this.shop, this.old, this.updated);
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
  public ShopPriceEvent clone(final Phase newPhase, final Double old, final Double updated) {

    return new ShopPriceEvent(newPhase, this.shop, old, updated);
  }

  public static ShopPriceEvent PRE(final @NotNull Shop shop,
                                  final Double old) {

    return new ShopPriceEvent(Phase.PRE, shop, old);
  }

  public static ShopPriceEvent PRE(final @NotNull Shop shop,
                                  final Double old, final Double updated) {

    return new ShopPriceEvent(Phase.PRE, shop, old, updated);
  }

  public static ShopPriceEvent MAIN(final @NotNull Shop shop,
                                   final Double old) {

    return new ShopPriceEvent(Phase.MAIN, shop, old);
  }

  public static ShopPriceEvent MAIN(final @NotNull Shop shop,
                                   final Double old, final Double updated) {

    return new ShopPriceEvent(Phase.MAIN, shop, old, updated);
  }

  public static ShopPriceEvent POST(final @NotNull Shop shop,
                                   final Double old) {

    return new ShopPriceEvent(Phase.POST, shop, old);
  }

  public static ShopPriceEvent POST(final @NotNull Shop shop,
                                   final Double old, final Double updated) {

    return new ShopPriceEvent(Phase.POST, shop, old, updated);
  }

  public static ShopPriceEvent RETRIEVE(final @NotNull Shop shop,
                                       final Double old) {

    return new ShopPriceEvent(Phase.RETRIEVE, shop, old);
  }

  public static ShopPriceEvent RETRIEVE(final @NotNull Shop shop,
                                       final Double old, final Double updated) {

    return new ShopPriceEvent(Phase.RETRIEVE, shop, old, updated);
  }
}
