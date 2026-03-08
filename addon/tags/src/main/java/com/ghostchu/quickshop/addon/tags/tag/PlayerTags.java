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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlayerTags
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class PlayerTags {

  private final Set<String> tags = ConcurrentHashMap.newKeySet();

  public boolean hasTag(final String tag) {

    return tags.contains(tag);
  }

  public boolean hasTags(final List<String> filterTags) {

    return tags.containsAll(filterTags);
  }

  public boolean addTag(final String tag) {

    return tags.add(tag);
  }

  public boolean removeTag(final String tag) {

    return tags.remove(tag);
  }

  public Set<String> getTags() {

    return Collections.unmodifiableSet(tags);
  }
}