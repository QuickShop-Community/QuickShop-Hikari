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
import com.ghostchu.quickshop.api.shop.IShopType;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.state.ShopState;
import org.jetbrains.annotations.NotNull;

/**
 * ShopStateEvent
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class ShopStateEvent extends ShopSettingEvent<ShopState> {

  public ShopStateEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                        final @NotNull ShopState old) {

    super(phase, shop, old);
  }

  public ShopStateEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                        final @NotNull ShopState old, final @NotNull ShopState updated) {

    super(phase, shop, old, updated);
  }

  public static ShopStateEvent PRE(final @NotNull Shop shop,
                                   final ShopState old) {

    return new ShopStateEvent(Phase.PRE, shop, old);
  }

  public static ShopStateEvent PRE(final @NotNull Shop shop,
                                   final ShopState old, final ShopState updated) {

    return new ShopStateEvent(Phase.PRE, shop, old, updated);
  }

  public static ShopStateEvent MAIN(final @NotNull Shop shop,
                                    final ShopState old) {

    return new ShopStateEvent(Phase.MAIN, shop, old);
  }

  public static ShopStateEvent MAIN(final @NotNull Shop shop,
                                    final ShopState old, final ShopState updated) {

    return new ShopStateEvent(Phase.MAIN, shop, old, updated);
  }

  public static ShopStateEvent POST(final @NotNull Shop shop,
                                    final ShopState old) {

    return new ShopStateEvent(Phase.POST, shop, old);
  }

  public static ShopStateEvent POST(final @NotNull Shop shop,
                                    final ShopState old, final ShopState updated) {

    return new ShopStateEvent(Phase.POST, shop, old, updated);
  }

  public static ShopStateEvent RETRIEVE(final @NotNull Shop shop,
                                        final ShopState old) {

    return new ShopStateEvent(Phase.RETRIEVE, shop, old);
  }

  public static ShopStateEvent RETRIEVE(final @NotNull Shop shop,
                                        final ShopState old, final ShopState updated) {

    return new ShopStateEvent(Phase.RETRIEVE, shop, old, updated);
  }

  /**
   * Creates a new instance of PhasedEvent with the specified newPhase.
   *
   * @param newPhase The new Phase for the cloned PhasedEvent
   *
   * @return A new instance of PhasedEvent with the specified newPhase
   */
  @Override
  public ShopStateEvent clone(final Phase newPhase) {

    if(this.updated != null) {

      return new ShopStateEvent(newPhase, this.shop, this.old, this.updated);
    }
    return new ShopStateEvent(newPhase, this.shop, this.old);
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
  public ShopStateEvent clone(final Phase newPhase, final ShopState old, final ShopState updated) {

    return new ShopStateEvent(newPhase, this.shop, old, updated);
  }
}