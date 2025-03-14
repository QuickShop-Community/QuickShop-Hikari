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
import com.ghostchu.quickshop.api.event.PhasedEvent;
import com.ghostchu.quickshop.api.event.settings.ShopSettingEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * ShopItemEvent represents an event that is tied to actions/retrieval of the Item setting for a
 * shop.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 * @see ItemStack
 */
public class ShopItemEvent extends ShopSettingEvent<ItemStack> {

  public ShopItemEvent(final @NotNull Phase phase, final @NotNull Shop shop, final @NotNull ItemStack old) {

    super(phase, shop, old);
  }

  public ShopItemEvent(final @NotNull Phase phase, final @NotNull Shop shop, final @NotNull ItemStack old, final @NotNull ItemStack updated) {

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
  public ShopItemEvent clone(final Phase newPhase) {
    if(this.updated != null) {

      return new ShopItemEvent(newPhase, this.shop, this.old, this.updated);
    }
    return new ShopItemEvent(newPhase, this.shop, this.old);
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
  public ShopItemEvent clone(final Phase newPhase, final ItemStack old, final ItemStack updated) {

    return new ShopItemEvent(newPhase, this.shop, old, updated);
  }

  public static ShopItemEvent PRE(final @NotNull Shop shop,
                                     final ItemStack old) {

    return new ShopItemEvent(Phase.PRE, shop, old);
  }

  public static ShopItemEvent PRE(final @NotNull Shop shop,
                                     final ItemStack old, final ItemStack updated) {

    return new ShopItemEvent(Phase.PRE, shop, old, updated);
  }

  public static ShopItemEvent MAIN(final @NotNull Shop shop,
                                      final ItemStack old) {

    return new ShopItemEvent(Phase.MAIN, shop, old);
  }

  public static ShopItemEvent MAIN(final @NotNull Shop shop,
                                      final ItemStack old, final ItemStack updated) {

    return new ShopItemEvent(Phase.MAIN, shop, old, updated);
  }

  public static ShopItemEvent POST(final @NotNull Shop shop,
                                      final ItemStack old) {

    return new ShopItemEvent(Phase.POST, shop, old);
  }

  public static ShopItemEvent POST(final @NotNull Shop shop,
                                      final ItemStack old, final ItemStack updated) {

    return new ShopItemEvent(Phase.POST, shop, old, updated);
  }

  public static ShopItemEvent RETRIEVE(final @NotNull Shop shop,
                                          final ItemStack old) {

    return new ShopItemEvent(Phase.RETRIEVE, shop, old);
  }

  public static ShopItemEvent RETRIEVE(final @NotNull Shop shop,
                                          final ItemStack old, final ItemStack updated) {

    return new ShopItemEvent(Phase.RETRIEVE, shop, old, updated);
  }
}