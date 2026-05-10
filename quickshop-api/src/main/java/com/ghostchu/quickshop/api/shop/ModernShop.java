package com.ghostchu.quickshop.api.shop;

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

import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.shop.builder.ShopBuilder;
import com.ghostchu.quickshop.api.shop.components.ShopInteraction;
import com.ghostchu.quickshop.api.shop.components.ShopItem;
import com.ghostchu.quickshop.api.shop.components.ShopLifecycle;
import com.ghostchu.quickshop.api.shop.components.ShopMeta;
import com.ghostchu.quickshop.api.shop.components.ShopPermission;
import com.ghostchu.quickshop.api.shop.components.ShopPrice;
import com.ghostchu.quickshop.api.shop.components.ShopTrading;
import com.ghostchu.quickshop.api.shop.service.result.ShopChangeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * ShopModern
 * T = price object, such as BigDecimal or Double
 * S = Location object such as Bukkit's Location object
 * U = Preview object such as Bukkit's Inventory object
 * V = Inventory object such as InventoryWrapper
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ModernShop<T, S, U, V> extends Locatable<S> {

  /**
   * WARNING: This UUID will changed after plugin reload, shop reload or server restart DO NOT USE
   * IT TO STORE DATA!
   *
   * @return Random UUID
   */
  @NotNull
  UUID getRuntimeRandomUniqueId();

  ShopItem item();

  ShopInteraction<U, V> interaction();

  ShopLifecycle lifecycle();

  ShopMeta meta();

  ShopPermission permission();

  ShopPrice<T> price();

  ShopTrading trading();

  /**
   * Converts the current shop instance into a {@link ShopSignStorage}.
   *
   * This method provides a {@link ShopSignStorage} object that acts as a
   * container for storing the shop's sign location data, such as world, x, y, and z coordinates.
   *
   * @return a {@link ShopSignStorage} instance that contains the location data of the shop's sign.
   */
  ShopSignStorage asShopSignStorage();

  /**
   * Converts the current shop instance into a {@link DataRecord}.
   *
   * This method provides a {@link DataRecord} representation of the shop instance,
   * encapsulating all relevant shop-related data such as item details, pricing,
   * permissions, owner information, and additional metadata.
   *
   * @return a {@link DataRecord} instance representing the shop's data.
   */
  @NotNull
  DataRecord asDataRecord();

  /**
   * Getting ShopInfoStorage that you can use for storage the shop data
   *
   * @return ShopInfoStorage
   */
  ShopInfoStorage asInfoStorage();

  /**
   * Gets the symbol link that created by InventoryWrapperManager
   *
   * @return InventoryWrapper
   */
  @NotNull
  String asSymbolLink();

  /**
   * Compares the current {@code ModernShop} instance with another provided instance and determines the set of
   * differences between them. These differences are represented as a set of {@code ShopChangeType} values,
   * where each type corresponds to a category of change (e.g., item, price, owner, etc.).
   *
   * @param compare The {@code ModernShop} instance to compare against. If {@code null}, the method assumes
   *                comparison with a non-existent or empty shop.
   * @return An {@code EnumSet} of {@code ShopChangeType} values that represent the changes detected between
   *         the current shop instance and the provided shop. If no changes are detected, an empty set is returned.
   */
  EnumSet<ShopChangeType> diff(final @Nullable ModernShop<T, S, U, V> compare);

  /**
   * Applies a series of changes to the current shop instance using a {@link ShopBuilder}.
   * The specified consumer allows modification of the {@code ShopBuilder},
   * after which the shop is rebuilt with the applied changes.
   *
   * <strong>NOTE: This does not apply the changes to the Shop cache or database, you'll need to utilize the
   * {@link ShopService} to do that.</strong>
   *
   * @param changes a {@code Consumer} that defines the modifications to the {@link ShopBuilder}.
   * @return a {@link ModernShop} instance with the applied modifications.
   */
  default ModernShop<T, S, U, V> withChanges(final Consumer<ShopBuilder<T, S, U, V>> changes) {

    final ShopBuilder<T, S, U, V> builder = builder();
    changes.accept(builder);
    return builder.build();
  }

  /**
   * Converts the current shop instance into a {@link ShopBuilder} for applying modifications or rebuilding.
   *
   * @return a {@link ShopBuilder} instance initialized with the current shop's state, enabling further modifications.
   */
  ShopBuilder<T, S, U, V> builder();
}