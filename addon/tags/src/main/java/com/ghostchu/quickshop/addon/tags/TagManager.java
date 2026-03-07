package com.ghostchu.quickshop.addon.tags;

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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.tags.tag.ShopTags;
import com.ghostchu.quickshop.addon.tags.tag.TaggingResult;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TagManager
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class TagManager {

  private final ConcurrentHashMap<Long, ShopTags> tags = new ConcurrentHashMap<>();

  private final TagService service;

  public TagManager(final Main main, final QuickShop plugin) {
    this.service = new TagService(main, plugin);
  }

  public TaggingResult addTag(final long shopId, final UUID player, final String tag) {

    final ShopTags shopTags = tags.computeIfAbsent(shopId, id -> new ShopTags());
    if(shopTags.hasTag(player, tag)) {
      return TaggingResult.ALREADY_EXISTS;
    }

    shopTags.addTag(player, tag);
    service.addShopTag(player, shopId, tag);
    return TaggingResult.SUCCESS;
  }

  public boolean removeAllTags() {

    final Iterator<Long> iterator = tags.keySet().iterator();
    while(iterator.hasNext()) {

      final Long shopId = iterator.next();
      removeAllShopTags(shopId);
    }
    return true;
  }

  public boolean removeAllShopTags(final long shopId) {
    final ShopTags shopTags = tags.computeIfAbsent(shopId, id -> new ShopTags());
    if(shopTags.isEmpty()) {
      return false;
    }
    shopTags.clear();
    service.removeAllShopTags(shopId);
    tags.remove(shopId);
    return true;
  }

  public boolean removeAllShopTagsBy(final long shopId, final UUID player) {
    final ShopTags shopTags = tags.computeIfAbsent(shopId, id -> new ShopTags());
    if(shopTags.isEmpty()) {
      return false;
    }
    shopTags.removeAllTags(player);
    service.removeAllShopTagsBy(shopId, player);
    return true;
  }

  public boolean removeAllPlayerTags(final UUID player) {

    final Iterator<Long> iterator = tags.keySet().iterator();
    while(iterator.hasNext()) {

      final Long shopId = iterator.next();
      removeAllShopTagsBy(shopId, player);
    }
    return true;
  }

  public boolean hasTag(final long shopId, final UUID player, final String tag) {
    final ShopTags shopTags = tags.computeIfAbsent(shopId, id -> new ShopTags());
    return shopTags.hasTag(player, tag);
  }

  public TaggingResult removeTag(final long shopId, final UUID player, final String tag) {

    final ShopTags shopTags = tags.computeIfAbsent(shopId, id -> new ShopTags());
    if(!shopTags.hasTag(player, tag)) {
      return TaggingResult.NOT_FOUND;
    }

    shopTags.removeTag(player, tag);
    service.removeShopTag(player, shopId, tag);

    //remove our shop object if empty to save memory
    if(shopTags.isEmpty()) {
      tags.remove(shopId);
    }

    return TaggingResult.SUCCESS;
  }
}