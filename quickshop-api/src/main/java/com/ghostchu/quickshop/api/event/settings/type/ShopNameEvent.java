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
 * ShopNameEvent represents an event that is tied to actions/retrieval of the Shop name setting for
 * a shop.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class ShopNameEvent extends ShopSettingEvent<String> {

  public ShopNameEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                           final @NotNull String old) {

    super(phase, shop, old);
  }

  public ShopNameEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                           final @NotNull String old, final @NotNull String updated) {

    super(phase, shop, old, updated);
  }
}