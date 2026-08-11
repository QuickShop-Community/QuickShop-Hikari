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

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * SimpleShopQuery
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class ShopQuery {

  private final List<Predicate<Shop>> filters = new ArrayList<>();

  /**
   * Adds a filter to the current query based on the specified criteria.
   *
   * @param filter The filter defining the criteria to apply to the query.
   * @param object The object to evaluate against the filter criteria.
   *
   * @return The current query object with the applied filter, enabling method chaining.
   */
  public <T> ShopQuery filterBy(final Filter<T> filter, final T object) {

    filters.add(shop -> filter.applies(shop, object));
    return this;
  }

  /**
   * Execute the query
   *
   * @return The result of the query
   */
  public List<Shop> execute() {

    final List<Shop> shops = new ArrayList<>(15);
    for (final Shop shop : QuickShopAPI.getInstance().getShopManager().getAllShops()) {

      boolean pass = true;
      for (final Predicate<Shop> filter : filters) {
        if (!filter.test(shop)) {

          pass = false;
          break;
        }
      }

      if (pass) {
        shops.add(shop);
      }
    }
    return shops;
  }
}
