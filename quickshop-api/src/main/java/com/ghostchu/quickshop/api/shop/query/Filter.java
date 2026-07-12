package com.ghostchu.quickshop.api.shop.query;

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

import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

/**
 * Filter
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface Filter<T> {

  /**
   * Determines whether the specified object satisfies the criteria defined by
   * the implementation in the context of the given shop.
   *
   * @param shop  The shop to consider when evaluating the criteria.
   * @param object The object to evaluate against the criteria.
   * @return {@code true} if the object satisfies the criteria for the given shop,
   *         {@code false} otherwise.
   */
  boolean applies(final @NotNull Shop shop, final @NotNull T object);
}