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
import com.ghostchu.quickshop.addon.tags.tag.PlayerTags;
import com.ghostchu.quickshop.addon.tags.tag.ShopTags;
import com.ghostchu.quickshop.addon.tags.tag.TaggingResult;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.ghostchu.quickshop.addon.tags.TagService.TOTAL_INDEX;
import static com.ghostchu.quickshop.addon.tags.tag.TaggingResult.NOT_FOUND;

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

    final ShopTags shopTags = tags.computeIfAbsent(shopId, id->new ShopTags());
    if(shopTags.hasTag(player, tag)) {
      return TaggingResult.ALREADY_EXISTS;
    }

    shopTags.addTag(player, tag);
    service.addShopTag(player, shopId, tag);
    return TaggingResult.SUCCESS;
  }

  public TaggingResult toggleTag(final long shopId, final UUID player, final String tag) {

    if(hasTag(shopId, player, tag)) {
      return removeTag(shopId, player, tag);
    }
    return addTag(shopId, player, tag);
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

    final ShopTags shopTags = tags.get(shopId);
    if(shopTags == null) {
      return false;
    }
    shopTags.clear();
    service.removeAllShopTags(shopId);
    tags.remove(shopId);
    return true;
  }

  public boolean removeAllShopTagsBy(final long shopId, final UUID player) {

    final ShopTags shopTags = tags.get(shopId);
    if(shopTags == null) {
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

  public int totalTags() {

    int total = 0;
    for(final Map.Entry<Long, ShopTags> entry : tags.entrySet()) {

      for(final PlayerTags tags : entry.getValue().getTags()) {
        total += tags.getTags().size();
      }
    }
    return total;
  }

  public int totalTagsByPlayer(final UUID player) {

    int total = 0;
    for(final Map.Entry<Long, ShopTags> entry : tags.entrySet()) {

      final PlayerTags tags = entry.getValue().getTags(player);
      if(tags == null || tags.getTags().isEmpty()) {
        continue;
      }
      total += tags.getTags().size();
    }
    //our total count for all tags by player.
    return total;
  }

  public TreeMap<Long, Integer> tagsCount(final UUID player) {

    int total = 0;
    final TreeMap<Long, Integer> count = new TreeMap<>();
    for(final Map.Entry<Long, ShopTags> entry : tags.entrySet()) {

      final PlayerTags tags = entry.getValue().getTags(player);
      if(tags == null || tags.getTags().isEmpty()) {
        continue;
      }

      count.put(entry.getKey(), tags.getTags().size());
      total += tags.getTags().size();
    }
    //our total count for all tags by player.
    count.put(TOTAL_INDEX, total);
    return count;
  }

  public Set<String> tagsFilteredByShop(final UUID player, final long shopId) {

    final ShopTags shopTags = tags.get(shopId);
    if(shopTags == null) {
      return Collections.emptySet();
    }
    return shopTags.getTags(player).getTags();
  }

  public List<Long> shopsFilteredByTag(final UUID player, final String tag) {

    final List<Long> shopIds = new ArrayList<>();
    final Iterator<Long> iterator = tags.keySet().iterator();
    while(iterator.hasNext()) {

      final Long shopId = iterator.next();
      final ShopTags shopTags = tags.get(shopId);
      if(shopTags == null) {
        continue;
      }

      if(shopTags.hasTag(player, tag)) {
        shopIds.add(shopId);
      }
    }
    return shopIds;
  }

  public List<Long> shopsFilteredByTags(final UUID player, final List<String> filterTags) {

    final List<Long> shopIds = new ArrayList<>();
    final Iterator<Long> iterator = tags.keySet().iterator();
    while(iterator.hasNext()) {

      final Long shopId = iterator.next();
      final ShopTags shopTags = tags.get(shopId);
      if(shopTags == null) {
        continue;
      }

      if(shopTags.hasTags(player, filterTags)) {
        shopIds.add(shopId);
      }
    }
    return shopIds;
  }

  public void listShopsByFilter(final Player player, final List<String> filterTags, final String titleNode,
                                final String noEntries) {

    final List<Long> shopIds = shopsFilteredByTags(player.getUniqueId(), filterTags);
    if(shopIds.isEmpty()) {
      Main.instance().quickShop().text().of(player, noEntries).send();
      return;
    }

    Main.instance().quickShop().text().of(player, titleNode).send();
    for(final Long shopId : shopIds) {
      Main.instance().quickShop().text().of(player, "addon.tags.general.list-entry", shopId).send();
    }
  }

  public boolean hasTag(final long shopId, final UUID player, final String tag) {

    final ShopTags shopTags = tags.get(shopId);
    if(shopTags == null) {
      return false;
    }
    return shopTags.hasTag(player, tag);
  }

  public boolean removeTag(final UUID player, final String tag) {

    final Iterator<Long> iterator = tags.keySet().iterator();
    while(iterator.hasNext()) {
      final Long shopId = iterator.next();
      removeTag(shopId, player, tag);
    }
    return true;
  }

  public TaggingResult removeTag(final long shopId, final UUID player, final String tag) {

    final ShopTags shopTags = tags.get(shopId);
    if(shopTags == null) {
      return NOT_FOUND;
    }

    if(!shopTags.hasTag(player, tag)) {
      return NOT_FOUND;
    }

    shopTags.removeTag(player, tag);
    service.removeShopTag(player, shopId, tag);

    //remove our shop object if empty to save memory
    if(shopTags.isEmpty()) {
      tags.remove(shopId);
    }

    return TaggingResult.SUCCESS;
  }

  public TagService service() {

    return service;
  }
}