package com.ghostchu.quickshop.api.event.settings.type.benefit;
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

import com.ghostchu.quickshop.api.economy.Benefit;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.settings.ShopSettingEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

/**
 * ShopBenefitEvent represents an event that is tied to retrieval of the Shop benefit setting for
 * a shop.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 * @see Benefit
 */
public class ShopBenefitEvent extends ShopSettingEvent<Benefit> {

  public ShopBenefitEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                          final @NotNull Benefit old) {

    super(phase, shop, old);
  }

  public ShopBenefitEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                          final @NotNull Benefit old, final @NotNull Benefit updated) {

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
  public ShopBenefitEvent clone(final Phase newPhase) {

    return new ShopBenefitEvent(newPhase, this.shop, this.old, this.updated);
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
  public ShopBenefitEvent clone(final Phase newPhase, final Benefit old, final Benefit updated) {

    return new ShopBenefitEvent(newPhase, this.shop, old, updated);
  }

  public static ShopBenefitEvent PRE(final @NotNull Shop shop,
                                  final Benefit old) {

    return new ShopBenefitEvent(Phase.PRE, shop, old);
  }

  public static ShopBenefitEvent PRE(final @NotNull Shop shop,
                                  final Benefit old, final Benefit updated) {

    return new ShopBenefitEvent(Phase.PRE, shop, old, updated);
  }

  public static ShopBenefitEvent MAIN(final @NotNull Shop shop,
                                   final Benefit old) {

    return new ShopBenefitEvent(Phase.MAIN, shop, old);
  }

  public static ShopBenefitEvent MAIN(final @NotNull Shop shop,
                                   final Benefit old, final Benefit updated) {

    return new ShopBenefitEvent(Phase.MAIN, shop, old, updated);
  }

  public static ShopBenefitEvent POST(final @NotNull Shop shop,
                                   final Benefit old) {

    return new ShopBenefitEvent(Phase.POST, shop, old);
  }

  public static ShopBenefitEvent POST(final @NotNull Shop shop,
                                   final Benefit old, final Benefit updated) {

    return new ShopBenefitEvent(Phase.POST, shop, old, updated);
  }

  public static ShopBenefitEvent RETRIEVE(final @NotNull Shop shop,
                                       final Benefit old) {

    return new ShopBenefitEvent(Phase.RETRIEVE, shop, old);
  }

  public static ShopBenefitEvent RETRIEVE(final @NotNull Shop shop,
                                       final Benefit old, final Benefit updated) {

    return new ShopBenefitEvent(Phase.RETRIEVE, shop, old, updated);
  }
}