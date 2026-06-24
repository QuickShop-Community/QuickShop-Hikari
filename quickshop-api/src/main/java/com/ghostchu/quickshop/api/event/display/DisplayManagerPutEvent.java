package com.ghostchu.quickshop.api.event.display;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
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

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.shop.ShopChunk;

/**
 * DisplayEntityItemManager
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class DisplayManagerPutEvent<T> extends AbstractQSEvent {

  private final ShopChunk shopChunk;
  private final T display;

  public DisplayManagerPutEvent(final ShopChunk shopChunk, final T display) {

    this.shopChunk = shopChunk;
    this.display = display;
  }

  public ShopChunk getShopChunk() {

    return shopChunk;
  }

  public T getDisplay() {

    return display;
  }
}