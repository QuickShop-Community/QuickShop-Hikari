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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.tag.PlayerTagIndex;
import com.ghostchu.quickshop.api.shop.tag.TagManager;
import com.ghostchu.quickshop.api.shop.tag.TagService;
import com.ghostchu.quickshop.api.shop.tag.TaggingResult;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.ghostchu.quickshop.api.shop.tag.TagService.TOTAL_INDEX;
import static com.ghostchu.quickshop.api.shop.tag.TaggingResult.NOT_FOUND;

/**
 * The TagManager class handles the management of tags associated with shops and players. It allows
 * adding, removing, and querying tags for shops, as well as filtering shops by tags. The data is
 * stored in memory with support for persistent operations via the TagService.
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class QuickShopTagManager implements TagManager {

  private final ConcurrentHashMap<Long, ShopTags> tags = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<UUID, PlayerTagIndex> playerIndexes = new ConcurrentHashMap<>();

  private final QuickShop plugin;
  private final TagService service;

  public QuickShopTagManager(final QuickShop plugin) {

    this.plugin = plugin;
    this.service = new QuickShopTagService(plugin);
  }

  private PlayerTagIndex playerIndex(final UUID player) {

    return playerIndexes.computeIfAbsent(player, id->new QuickShopPlayerTagIndex());
  }

  private void cleanupPlayerIndex(final UUID player) {

    final PlayerTagIndex index = playerIndexes.get(player);
    if(index == null) {
      return;
    }

    if(index.totalTags() <= 0) {
      playerIndexes.remove(player);
    }
  }

  @Override
  public TaggingResult addTag(final long shopId, final UUID player, final String tag) {

    final ShopTags shopTags = tags.computeIfAbsent(shopId, id->new ShopTags());
    if(shopTags.hasTag(player, tag)) {
      return TaggingResult.ALREADY_EXISTS;
    }

    shopTags.addTag(player, tag);
    //update our index service
    playerIndex(player).addTag(shopId, tag);
    //update our database
    service.addShopTag(player, shopId, tag);
    return TaggingResult.SUCCESS;
  }

  @Override
  public TaggingResult toggleTag(final long shopId, final UUID player, final String tag) {

    if(hasTag(shopId, player, tag)) {
      return removeTag(shopId, player, tag);
    }
    return addTag(shopId, player, tag);
  }

  @Override
  public boolean removeAllTags() {

    final Iterator<Long> iterator = tags.keySet().iterator();
    while(iterator.hasNext()) {

      final Long shopId = iterator.next();
      removeAllShopTags(shopId);
    }
    return true;
  }

  @Override
  public boolean removeAllShopTags(final long shopId) {

    final ShopTags shopTags = tags.get(shopId);
    if(shopTags == null) {
      return false;
    }

    for(final PlayerTags playerTags : shopTags.getTags()) {
      final UUID player = playerTags.player();

      final PlayerTagIndex index = playerIndexes.get(player);
      if(index != null) {
        for(final String tag : playerTags.getTags()) {
          index.removeTag(shopId, tag);
        }
        cleanupPlayerIndex(player);
      }
    }

    shopTags.clear();
    service.removeAllShopTags(shopId);
    tags.remove(shopId);
    return true;
  }

  @Override
  public boolean removeAllShopTagsBy(final long shopId, final UUID player) {

    final ShopTags shopTags = tags.get(shopId);
    if(shopTags == null) {
      return false;
    }

    final PlayerTags playerTags = shopTags.getTags(player);
    if(playerTags == null || playerTags.getTags().isEmpty()) {
      return false;
    }

    //update our index service
    final PlayerTagIndex index = playerIndexes.get(player);
    if(index != null) {
      for(final String tag : playerTags.getTags()) {
        index.removeTag(shopId, tag);
      }
      cleanupPlayerIndex(player);
    }

    shopTags.removeAllTags(player);
    //remove our shop object if empty to save memory
    if(shopTags.isEmpty()) {
      tags.remove(shopId);
    }

    service.removeAllShopTagsBy(shopId, player);
    return true;
  }

  @Override
  public boolean removeAllPlayerTags(final UUID player) {

    //update our index service
    final PlayerTagIndex index = playerIndexes.get(player);
    if(index == null || index.totalTags() <= 0) {
      return false;
    }

    final Iterator<Long> iterator = tags.keySet().iterator();
    while(iterator.hasNext()) {

      final Long shopId = iterator.next();
      removeAllShopTagsBy(shopId, player);
    }

    //update our index service
    playerIndexes.remove(player);
    return true;
  }

  @Override
  public int totalTags() {

    int total = 0;
    for(final PlayerTagIndex index : playerIndexes.values()) {
      total += index.totalTags();
    }
    return total;
  }

  @Override
  public int totalTagsByPlayer(final UUID player) {

    final PlayerTagIndex index = playerIndexes.get(player);
    if(index == null) {
      return 0;
    }
    return index.totalTags();
  }

  @Override
  public TreeMap<Long, Integer> tagsCount(final UUID player) {

    final TreeMap<Long, Integer> count = new TreeMap<>();
    final PlayerTagIndex index = playerIndexes.get(player);
    //check out index service
    if(index == null) {
      count.put(TOTAL_INDEX, 0);
      return count;
    }

    int total = 0;
    for(final Long shopId : index.shops()) {
      final int size = index.getTags(shopId).size();
      if(size <= 0) {
        continue;
      }

      count.put(shopId, size);
      total += size;
    }

    //our total count
    count.put(TOTAL_INDEX, total);
    return count;
  }

  @Override
  public Set<String> tagsFilteredByShop(final UUID player, final long shopId) {

    final PlayerTagIndex index = playerIndexes.get(player);
    if(index != null) {
      return index.getTags(shopId);
    }

    final ShopTags shopTags = tags.get(shopId);
    if(shopTags == null) {
      return Collections.emptySet();
    }

    final PlayerTags playerTags = shopTags.getTags(player);
    if(playerTags == null) {
      return Collections.emptySet();
    }

    return playerTags.getTags();
  }

  @Override
  public List<Long> shopsFilteredByTag(final UUID player, final String tag) {

    final PlayerTagIndex index = playerIndexes.get(player);
    if(index == null) {
      return Collections.emptyList();
    }

    return new ArrayList<>(index.getShops(tag));
  }

  @Override
  public List<Long> shopsFilteredByTags(final UUID player, final List<String> filterTags) {

    final PlayerTagIndex index = playerIndexes.get(player);
    if(index == null || filterTags.isEmpty()) {
      return Collections.emptyList();
    }

    Set<Long> result = null;

    for(final String tag : filterTags) {
      final Set<Long> shops = index.getShops(tag);
      if(shops.isEmpty()) {
        return Collections.emptyList();
      }

      if(result == null) {
        result = ConcurrentHashMap.newKeySet();
        result.addAll(shops);
        continue;
      }

      result.retainAll(shops);
      if(result.isEmpty()) {
        return Collections.emptyList();
      }
    }

    return result == null? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(result));
  }

  @Override
  public void listShopsByFilter(final Player player, final List<String> filterTags, final String titleNode,
                                final String noEntries) {

    final List<Long> shopIds = shopsFilteredByTags(player.getUniqueId(), filterTags);
    if(shopIds.isEmpty()) {
      plugin.text().of(player, noEntries).send();
      return;
    }

    plugin.text().of(player, titleNode).send();
    for(final Long shopId : shopIds) {
      plugin.text().of(player, "tags.general.list-entry", shopId).send();
    }
  }

  @Override
  public boolean hasTag(final long shopId, final UUID player, final String tag) {

    final ShopTags shopTags = tags.get(shopId);
    if(shopTags == null) {
      return false;
    }
    return shopTags.hasTag(player, tag);
  }

  @Override
  public boolean removeTag(final UUID player, final String tag) {

    final Iterator<Long> iterator = tags.keySet().iterator();
    while(iterator.hasNext()) {
      final Long shopId = iterator.next();
      removeTag(shopId, player, tag);
    }
    return true;
  }

  @Override
  public TaggingResult removeTag(final long shopId, final UUID player, final String tag) {

    final ShopTags shopTags = tags.get(shopId);
    if(shopTags == null) {
      return NOT_FOUND;
    }

    if(!shopTags.hasTag(player, tag)) {
      return NOT_FOUND;
    }

    shopTags.removeTag(player, tag);
    //update our index service
    final PlayerTagIndex index = playerIndexes.get(player);
    if(index != null) {

      index.removeTag(shopId, tag);
    }
    //update our database
    service.removeShopTag(player, shopId, tag);

    //remove our shop object if empty to save memory
    if(shopTags.isEmpty()) {
      tags.remove(shopId);
    }

    return TaggingResult.SUCCESS;
  }

  @Override
  public TagService service() {

    return service;
  }
}