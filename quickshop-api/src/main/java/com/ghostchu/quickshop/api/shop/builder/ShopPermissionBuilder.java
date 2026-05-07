package com.ghostchu.quickshop.api.shop.builder;

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


import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.components.ShopPermission;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * ShopPermissionBuilder
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopPermissionBuilder {

  /**
   * Retrieves a map of permissions associated with a shop.
   * The map uses {@code UUID} as the key to uniquely identify the entity or user,
   * and {@code String} as the value to represent the associated permission level or configuration.
   *
   * @return a non-null map where keys are {@code UUID} values representing unique identifiers,
   *         and values are {@code String} values representing associated permissions.
   */
  @NotNull
  Map<UUID, String> permissions();

  /**
   * Sets the permissions for the shop being built.
   *
   * @param permissions a map where each key is a {@code UUID} representing a unique identifier,
   *                    and each value is a {@code String} representing the associated permission;
   *                    must not be null
   * @return the current {@code ShopPermissionBuilder} instance for method chaining
   */
  ShopPermissionBuilder permissions(@NotNull Map<UUID, String> permissions);

  /**
   * Associates a specific permission group with the entity identified by the provided unique identifier (UUID).
   * This method adds or updates a permission mapping within the builder's internal configuration.
   *
   * @param uuid the unique identifier (UUID) of the entity to associate the permission with; must not be null
   * @param group the permission group name to assign to the specified entity; must not be null
   * @return the current {@code ShopPermissionBuilder} instance for method chaining
   */
  ShopPermissionBuilder permission(@NotNull UUID uuid, @NotNull String group);

  /**
   * Builds and returns a {@link ShopPermission} instance for the specified shop.
   *
   * @param shop the {@link ModernShop} instance for which the permissions will be built;
   *             must not be null and defines the context for the resulting {@link ShopPermission}.
   * @return a {@link ShopPermission} instance that represents the permissions associated
   *         with the specified {@link ModernShop}.
   */
  ShopPermission build(ModernShop<?, ?, ?, ?> shop);
}