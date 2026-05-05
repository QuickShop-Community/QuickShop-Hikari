package com.ghostchu.quickshop.shop.components;

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
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.management.ShopPermissionCheckEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopPlayerGroupEvent;
import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.components.ShopItem;
import com.ghostchu.quickshop.api.shop.components.ShopPermission;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.api.shop.service.result.ShopChangeType;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.shop.InventoryPreview;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * SimpleShopPermission
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class SimpleShopPermission implements ShopPermission {

  private final ModernShop<?, ?, Player, InventoryPreview> shop;

  //TODO: modification methods for this in order to add entries/set all entries
  @NotNull
  private final Map<UUID, String> playerGroup = new HashMap<>();

  public SimpleShopPermission(final ModernShop<?, ?, Player, InventoryPreview> shop) {
    this.shop = shop;
  }

  /**
   * Gets all player and their group on this shop
   *
   * @return Map of UUID and group
   */
  @Override
  public @NotNull Map<UUID, String> getPermissionAudiences() {

    final Map<UUID, String> clonedPlayerGroup = new HashMap<>(playerGroup);
    final Optional<UUID> uuid = shop.meta().getOwner().getUniqueIdOptional();
    if(uuid.isPresent()) {
      clonedPlayerGroup.put(shop.meta().getOwner().getUniqueId(), BuiltInShopPermissionGroup.ADMINISTRATOR.getNamespacedNode());
    }
    return clonedPlayerGroup;
  }

  /**
   * Gets specific player group on specific shop
   *
   * @param player player
   *
   * @return namespaced group
   */
  @Override
  public @NotNull String getPlayerGroup(@NotNull final UUID player) {

    if(player.equals(shop.meta().getOwner().getUniqueId())) {
      return BuiltInShopPermissionGroup.ADMINISTRATOR.getNamespacedNode();
    }

    final String group = getPermissionAudiences().getOrDefault(player, BuiltInShopPermissionGroup.EVERYONE.getNamespacedNode());
    if(QuickShop.getInstance().getShopPermissionManager().hasGroup(group)) {

      final ShopPlayerGroupEvent event = new ShopPlayerGroupEvent(Phase.RETRIEVE, shop, player, group);
      event.callEvent();

      return event.updated();
    }
    return BuiltInShopPermissionGroup.EVERYONE.getNamespacedNode();
  }

  /**
   * Check if player have authorized for specific permission on specific shop
   *
   * @param player     player
   * @param namespace  permission namespace
   * @param permission permission
   *
   * @return true if player have authorized
   */
  @Override
  public boolean playerAuthorize(@NotNull final UUID player, @NotNull final Plugin namespace, @NotNull final String permission) {

    if(player.equals(shop.meta().getOwner().getUniqueId())) {
      Log.permission("Check permission " + namespace.getName().toLowerCase(Locale.ROOT) + "." + permission + " for " + player + " -> " + "true");
      return true;
    }

    final String group = getPlayerGroup(player);
    final boolean hasPermission = QuickShop.getInstance().getShopPermissionManager().hasPermission(group, namespace, permission);

    final ShopPermissionCheckEvent event = new ShopPermissionCheckEvent(Phase.MAIN, shop, player, namespace.getName(), permission, hasPermission);
    event.callEvent();

    Log.permission("Check permission " + namespace.getName().toLowerCase(Locale.ROOT) + "." + permission + ": " + player + " -> " + event.hasPermission());

    return event.hasPermission();
  }

  /**
   * Check if player have authorized for specific permission on specific shop
   *
   * @param player     player
   * @param permission namespaced permission
   *
   * @return true if player have authorized
   */
  @Override
  public boolean playerAuthorize(@NotNull final UUID player, @NotNull final BuiltInShopPermission permission) {

    return playerAuthorize(player, QuickShop.getInstance().getJavaPlugin(), permission.getRawNode());
  }

  /**
   * Gets the player list of who can authorize specific permission on this shop
   *
   * @param permission permission
   *
   * @return Collection of UUID
   */
  @Override
  public List<UUID> playersCanAuthorize(@NotNull final BuiltInShopPermission permission) {

    return playersCanAuthorize(QuickShop.getInstance().getJavaPlugin(), permission.getRawNode());
  }

  /**
   * Gets the player list of who can authorize specific group on this shop
   *
   * @param permissionGroup group
   *
   * @return Collection of UUID
   */
  @Override
  public List<UUID> playersCanAuthorize(@NotNull final BuiltInShopPermissionGroup permissionGroup) {

    return getPermissionAudiences().entrySet().stream().filter(entry->entry.getValue().equals(permissionGroup.getNamespacedNode())).map(Map.Entry::getKey).toList();
  }

  /**
   * Gets the player list of who can authorize specific permission on this shop
   *
   * @param plugin     namespace of permission
   * @param permission raw permission
   *
   * @return Collection of UUID
   */
  @Override
  public List<UUID> playersCanAuthorize(@NotNull final Plugin plugin, @NotNull final String permission) {

    final List<UUID> result = new ArrayList<>();
    for(final Map.Entry<UUID, String> uuidStringEntry : this.getPermissionAudiences().entrySet()) {

      final String group = uuidStringEntry.getValue();
      final boolean hasPermission = QuickShop.getInstance().getShopPermissionManager().hasPermission(group, plugin, permission);

      final ShopPermissionCheckEvent event = new ShopPermissionCheckEvent(Phase.MAIN, shop, uuidStringEntry.getKey(), plugin.getName(), permission, hasPermission);
      event.callEvent();

      if(event.hasPermission()) {
        result.add(uuidStringEntry.getKey());
      }
    }
    Log.permission("Check permission " + plugin.getName().toLowerCase(Locale.ROOT) + "." + permission + ": " + CommonUtil.list2String(result.stream().map(UUID::toString).toList()));
    return result;
  }

  /**
   * Sets specific player permission on specfic shop
   *
   * @param player player
   * @param group  namespaced group name
   */
  @Override
  public void setPlayerGroup(@NotNull final UUID player, @Nullable String group) {

    if(group == null) {
      group = BuiltInShopPermissionGroup.EVERYONE.getNamespacedNode();
    }

    ShopPlayerGroupEvent event = new ShopPlayerGroupEvent(Phase.PRE, shop, player, getPlayerGroup(player), group);
    event.callEvent();

    if(group.equals(BuiltInShopPermissionGroup.EVERYONE.getNamespacedNode())) {

      this.playerGroup.remove(player);
    } else {

      this.playerGroup.put(player, group);
    }
    event = event.clone(Phase.POST);
    event.callEvent();

    //TODO: Determine how to mark as dirty. Maybe through shop service?
    //setDirty();
  }

  /**
   * Sets specific player permission on specfic shop
   *
   * @param player player
   * @param group  group
   */
  @Override
  public void setPlayerGroup(@NotNull final UUID player, @Nullable BuiltInShopPermissionGroup group) {

    if(group == null) {
      group = BuiltInShopPermissionGroup.EVERYONE;
    }

    ShopPlayerGroupEvent event = new ShopPlayerGroupEvent(Phase.PRE, shop, player, getPlayerGroup(player), group.getNamespacedNode());
    event.callEvent();
    if(group == BuiltInShopPermissionGroup.EVERYONE) {

      this.playerGroup.remove(player);
    } else {

      setPlayerGroup(player, group.getNamespacedNode());
    }

    event = event.clone(Phase.POST);
    event.callEvent();

    //TODO: Determine how to mark as dirty. Maybe through shop service?
    //setDirty();
  }

  @Override
  public EnumSet<ShopChangeType> diff(final @Nullable ShopPermission compare) {

    final EnumSet<ShopChangeType> changes = EnumSet.noneOf(ShopChangeType.class);

    //TODO: check maps for equality

    return changes;
  }
}
