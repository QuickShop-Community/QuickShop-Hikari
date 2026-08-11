package com.ghostchu.quickshop.shop.tag;

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

import com.ghostchu.quickshop.api.shop.tag.PlayerTagIndex;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default QuickShop implementation of {@link PlayerTagIndex}.
 *
 * <p>This implementation maintains two concurrent indexes:</p>
 * <ul>
 *   <li>shopId → tags</li>
 *   <li>tag → shopIds</li>
 * </ul>
 *
 * <p>This allows fast lookups in both directions.</p>
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class QuickShopPlayerTagIndex implements PlayerTagIndex {

  private final Map<Long, Set<String>> shopToTags = new ConcurrentHashMap<>();
  private final Map<String, Set<Long>> tagToShops = new ConcurrentHashMap<>();

  @Override
  public void addTag(final long shopId, final String tag) {

    shopToTags
            .computeIfAbsent(shopId, id->ConcurrentHashMap.newKeySet())
            .add(tag);

    tagToShops
            .computeIfAbsent(tag, t->ConcurrentHashMap.newKeySet())
            .add(shopId);
  }

  @Override
  public void removeTag(final long shopId, final String tag) {

    final Set<String> tags = shopToTags.get(shopId);
    if(tags != null) {
      tags.remove(tag);
      if(tags.isEmpty()) {
        shopToTags.remove(shopId);
      }
    }

    final Set<Long> shops = tagToShops.get(tag);
    if(shops != null) {
      shops.remove(shopId);
      if(shops.isEmpty()) {
        tagToShops.remove(tag);
      }
    }
  }

  @Override
  public Set<String> getTags(final long shopId) {

    return shopToTags.getOrDefault(shopId, Collections.emptySet());
  }

  @Override
  public Set<Long> getShops(final String tag) {

    return tagToShops.getOrDefault(tag, Collections.emptySet());
  }

  @Override
  public Set<Long> shops() {

    return Collections.unmodifiableSet(shopToTags.keySet());
  }

  @Override
  public int totalTags() {

    int total = 0;
    for(final Set<String> tags : shopToTags.values()) {
      total += tags.size();
    }
    return total;
  }

  @Override
  public boolean hasTag(final long shopId, final String tag) {

    final Set<String> tags = shopToTags.get(shopId);
    return tags != null && tags.contains(tag);
  }

  @Override
  public void clear() {

    shopToTags.clear();
    tagToShops.clear();
  }
}