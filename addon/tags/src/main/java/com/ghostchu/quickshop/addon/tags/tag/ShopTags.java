package com.ghostchu.quickshop.addon.tags.tag;

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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShopTags
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class ShopTags {

  private final ConcurrentHashMap<UUID, PlayerTags> tags = new ConcurrentHashMap<>();

  public void addTag(final UUID player, final String tag) {

    if(!tags.containsKey(player)) {
      tags.put(player, new PlayerTags());
    }
    tags.get(player).addTag(tag);
  }

  public void removeTag(final UUID player, final String tag) {

    final PlayerTags playerTags = tags.get(player);
    if(playerTags == null) {
      return;
    }

    playerTags.removeTag(tag);
    //remove our player object if empty to save memory
    if(playerTags.getTags().isEmpty()) {
      tags.remove(player);
    }
  }

  public boolean removeAllTags(final UUID player) {

    if(!tags.containsKey(player)) {
      return false;
    }
    tags.remove(player);
    return true;

  }

  public boolean hasTag(final UUID player, final String tag) {

    final PlayerTags playerTags = tags.get(player);
    return playerTags != null && playerTags.hasTag(tag);
  }

  public boolean hasTags(final UUID player, final List<String> filterTags) {

    final PlayerTags playerTags = tags.get(player);
    return playerTags != null && playerTags.hasTags(filterTags);
  }

  public PlayerTags getTags(final UUID player) {

    return tags.get(player);
  }

  public void setTags(final UUID player, final PlayerTags tags) {

    this.tags.put(player, tags);
  }

  public boolean isEmpty() {

    return tags.isEmpty();
  }

  public void clear() {

    tags.clear();
  }

  public Collection<PlayerTags> getTags() {

    return Collections.unmodifiableCollection(tags.values());
  }
}