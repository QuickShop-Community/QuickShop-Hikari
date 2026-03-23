package com.ghostchu.quickshop.api.shop.permission;

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

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ShopPermission
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopPermission {

  /**
   * Gets all player and their group on this shop
   *
   * @return Map of UUID and group
   */
  @NotNull
  Map<UUID, String> getPermissionAudiences();

  /**
   * Gets specific player group on specific shop
   *
   * @param player player
   *
   * @return namespaced group
   */
  @NotNull
  String getPlayerGroup(@NotNull UUID player);

  /**
   * Check if player have authorized for specific permission on specific shop
   *
   * @param player     player
   * @param namespace  permission namespace
   * @param permission permission
   *
   * @return true if player have authorized
   */
  boolean playerAuthorize(@NotNull UUID player, @NotNull Plugin namespace, @NotNull String permission);

  /**
   * Check if player have authorized for specific permission on specific shop
   *
   * @param player     player
   * @param permission namespaced permission
   *
   * @return true if player have authorized
   */
  boolean playerAuthorize(@NotNull UUID player, @NotNull BuiltInShopPermission permission);

  /**
   * Gets the player list of who can authorize specific permission on this shop
   *
   * @param permission permission
   *
   * @return Collection of UUID
   */
  List<UUID> playersCanAuthorize(@NotNull BuiltInShopPermission permission);

  /**
   * Gets the player list of who can authorize specific group on this shop
   *
   * @param permissionGroup group
   *
   * @return Collection of UUID
   */
  List<UUID> playersCanAuthorize(@NotNull BuiltInShopPermissionGroup permissionGroup);

  /**
   * Gets the player list of who can authorize specific permission on this shop
   *
   * @param permission raw permission
   * @param plugin     namespace of permission
   *
   * @return Collection of UUID
   */
  List<UUID> playersCanAuthorize(@NotNull Plugin plugin, @NotNull String permission);

  /**
   * Sets specific player permission on specfic shop
   *
   * @param player player
   * @param group  namespaced group name
   */
  void setPlayerGroup(@NotNull UUID player, @Nullable String group);

  /**
   * Sets specific player permission on specfic shop
   *
   * @param player player
   * @param group  group
   */
  void setPlayerGroup(@NotNull UUID player, @Nullable BuiltInShopPermissionGroup group);
}