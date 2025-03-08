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

import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.settings.ShopSettingEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

/**
 * ShopBenefitRemoveEvent represents an event triggered when a benefit is removed from a shop.
 * Handlers can use this to react to benefit removal.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class ShopBenefitRemoveEvent extends ShopSettingEvent<Double> {

  private final QUser user;

  public ShopBenefitRemoveEvent(@NotNull final Phase phase, @NotNull final Shop shop, @NotNull final QUser user, @NotNull final Double benefit) {
    super(phase, shop, benefit);

    this.user = user;
  }

  public QUser getUser() {
    return user;
  }

  /**
   * Creates a new instance of PhasedEvent with the specified newPhase.
   *
   * @param newPhase The new Phase for the cloned PhasedEvent
   *
   * @return A new instance of PhasedEvent with the specified newPhase
   */
  @Override
  public ShopBenefitRemoveEvent clone(final Phase newPhase) {

    return new ShopBenefitRemoveEvent(newPhase, this.shop, this.user, this.updated);
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
  public ShopBenefitRemoveEvent clone(final Phase newPhase, final Double old, final Double updated) {

    return new ShopBenefitRemoveEvent(newPhase, this.shop, this.user, updated);
  }

  public static ShopBenefitRemoveEvent PRE(final @NotNull Shop shop, final @NotNull QUser user,
                                        final Double old) {

    return new ShopBenefitRemoveEvent(Phase.PRE, shop, user, old);
  }

  public static ShopBenefitRemoveEvent PRE(final @NotNull Shop shop, final @NotNull QUser user,
                                        final Double old, final Double updated) {

    return new ShopBenefitRemoveEvent(Phase.PRE, shop, user, updated);
  }

  public static ShopBenefitRemoveEvent MAIN(final @NotNull Shop shop, final @NotNull QUser user,
                                         final Double old) {

    return new ShopBenefitRemoveEvent(Phase.MAIN, shop, user, old);
  }

  public static ShopBenefitRemoveEvent MAIN(final @NotNull Shop shop, final @NotNull QUser user,
                                         final Double old, final Double updated) {

    return new ShopBenefitRemoveEvent(Phase.MAIN, shop, user, updated);
  }

  public static ShopBenefitRemoveEvent POST(final @NotNull Shop shop, final @NotNull QUser user,
                                         final Double old) {

    return new ShopBenefitRemoveEvent(Phase.POST, shop, user, old);
  }

  public static ShopBenefitRemoveEvent POST(final @NotNull Shop shop, final @NotNull QUser user,
                                         final Double old, final Double updated) {

    return new ShopBenefitRemoveEvent(Phase.POST, shop, user, updated);
  }

  public static ShopBenefitRemoveEvent RETRIEVE(final @NotNull Shop shop, final @NotNull QUser user,
                                             final Double old) {

    return new ShopBenefitRemoveEvent(Phase.RETRIEVE, shop, user, old);
  }

  public static ShopBenefitRemoveEvent RETRIEVE(final @NotNull Shop shop, final @NotNull QUser user,
                                             final Double old, final Double updated) {

    return new ShopBenefitRemoveEvent(Phase.RETRIEVE, shop, user, updated);
  }
}