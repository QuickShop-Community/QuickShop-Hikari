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
import com.ghostchu.quickshop.api.shop.components.ShopInteraction;
import com.ghostchu.quickshop.api.shop.components.ShopItem;
import com.ghostchu.quickshop.api.shop.components.ShopLifecycle;
import com.ghostchu.quickshop.api.shop.components.ShopMeta;
import com.ghostchu.quickshop.api.shop.components.ShopPermission;
import com.ghostchu.quickshop.api.shop.components.ShopPrice;

import java.util.UUID;

/**
 * ShopBuilder
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopBuilder<T, S, U, V> {

  /**
   * Retrieves the location associated with the shop being built.
   *
   * @return the location of the shop, represented as an instance of type {@code T}.
   */
  T location();

  /**
   * Sets the location for the shop being built.
   *
   * @param location the location to assign to the shop, represented by an instance of type {@code T};
   *                 must not be null
   * @return the current {@code ShopBuilder} instance for method chaining
   */
  ShopBuilder<T, S, U, V> location(T location);

  /**
   * Generates and retrieves a runtime-unique {@code UUID}. This identifier is intended
   * to uniquely distinguish a specific shop instance during its lifecycle.
   *
   * @return a {@code UUID} representing a randomly generated runtime-unique identifier.
   */
  UUID getRuntimeRandomUniqueId();

  /**
   * Sets the runtime UUID for the shop being built. The runtime UUID is used
   * to uniquely identify the shop during its lifecycle.
   *
   * @param runTimeUUID the {@code UUID} to associate with the shop runtime;
   *                    must not be null
   * @return the current {@code ShopBuilder} instance for method chaining
   */
  ShopBuilder<T, S, U, V> runTimeUUID(UUID runTimeUUID);

  /**
   * Retrieves the {@code ShopItem} instance currently associated with the {@code ShopBuilder}.
   *
   * @return the current {@code ShopItem} instance, representing the item configuration for the shop.
   */
  ShopItem item();

  /**
   * Adds a {@code ShopItem} to the shop being built.
   *
   * @param item the {@code ShopItem} instance to add; must not be null
   * @return the current {@code ShopBuilder} instance for method chaining
   */
  ShopBuilder<T, S, U, V> item(ShopItem item);

  /**
   * Retrieves the {@code ShopInteraction} instance associated with the shop being built.
   *
   * @return the {@code ShopInteraction} instance defining the interaction logic
   *         for the shop, including player interactions and preview handling.
   */
  ShopInteraction<U, V> interaction();

  /**
   * Sets the interaction configuration for the shop being built.
   *
   * @param interaction the {@code ShopInteraction} instance representing the interaction logic
   *                    to be applied to the shop; must not be null
   * @return the current {@code ShopBuilder} instance for method chaining
   */
  ShopBuilder<T, S, U, V> interaction(ShopInteraction<U, V> interaction);

  /**
   * Retrieves the {@code ShopLifecycle} instance associated with the shop being built.
   *
   * @return the {@code ShopLifecycle} representing the lifecycle management of the shop.
   */
  ShopLifecycle lifecycle();

  /**
   * Retrieves the metadata associated with the shop being built.
   *
   * @return the {@code ShopMeta} instance that contains metadata information for the shop.
   */
  ShopMeta meta();

  /**
   * Sets the metadata for the shop being built.
   *
   * @param meta the {@code ShopMeta} instance that contains metadata
   *             information for the shop; must not be null
   * @return the current {@code ShopBuilder} instance for method chaining
   */
  ShopBuilder<T, S, U, V> meta(ShopMeta meta);

  /**
   * Retrieves the {@code ShopPermission} associated with the shop being built.
   *
   * @return the {@code ShopPermission} instance representing the permissions configuration of the shop.
   */
  ShopPermission permission();

  /**
   * Sets the permission configuration for the shop being built.
   *
   * @param permission the {@code ShopPermission} instance representing the permissions
   *                   to apply to the shop; must not be null
   * @return the current {@code ShopBuilder} instance for method chaining
   */
  ShopBuilder<T, S, U, V> permission(ShopPermission permission);

  /**
   * Retrieves the {@code ShopPrice} instance associated with the shop being built.
   *
   * @return the {@code ShopPrice} of type {@code T}, representing the price configuration of the shop.
   */
  ShopPrice<T> price();

  /**
   * Sets the price for the shop using a {@link ShopPrice} instance.
   *
   * @param price the {@code ShopPrice} instance representing the price to assign to the shop; must not be null
   * @return the current {@code ShopBuilder} instance for method chaining
   */
  ShopBuilder<T, S, U, V> price(ShopPrice<T> price);

  /**
   * Builds and returns a completed instance of {@link ModernShop}, using the
   * properties and configurations set in this {@code ShopBuilder}.
   *
   * @return a fully constructed {@link ModernShop} instance with the applied configurations.
   */
  ModernShop<T, S, U, V> build();
}