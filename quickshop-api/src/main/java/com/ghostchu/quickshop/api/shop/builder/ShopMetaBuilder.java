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


import com.ghostchu.quickshop.api.economy.benefit.BenefitOverflowException;
import com.ghostchu.quickshop.api.economy.benefit.BenefitProvider;
import com.ghostchu.quickshop.api.economy.benefit.BenefitsAlreadyException;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.IShopType;
import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.components.ShopMeta;
import com.ghostchu.quickshop.api.shop.state.ShopState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * ShopMetaBuilder
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopMetaBuilder {

  /**
   * Retrieves the unique identifier of the shop.
   * This identifier is used to distinguish the shop within the system.
   *
   * @return the unique identifier of the shop as a long value.
   */
  long shopId();

  /**
   * Sets the unique identifier for the shop.
   * This identifier is used to distinguish the shop within the system.
   *
   * @param shopId the unique identifier for the shop.
   *               It should be a valid long value.
   * @return the current instance of {@code ShopMetaBuilder} for method chaining.
   */
  ShopMetaBuilder shopId(final long shopId);

  /**
   * Retrieves the name of the shop.
   * The shop name represents the identity or title of the shop and can be null if no name is set.
   *
   * @return the name of the shop, or null if no name is set.
   */
  @Nullable
  String shopName();

  /**
   * Sets the name of the shop.
   * The shop name represents the identity or title of the shop.
   *
   * @param shopName the name of the shop to set. This can be {@code null} if no name is provided.
   * @return the current instance of {@code ShopMetaBuilder} for method chaining.
   */
  ShopMetaBuilder shopName(@Nullable final String shopName);

  /**
   * Retrieves the current {@code ShopState} associated with the shop.
   * The {@code ShopState} defines the behavior, accessibility, and
   * overall status of the shop (e.g. active, frozen, or disabled).
   *
   * @return the {@code ShopState} instance representing the shop's current state.
   */
  ShopState shopState();

  /**
   * Sets the state of the shop using the specified {@code ShopState} instance.
   * The shop state determines the behavior, accessibility, and status of the shop.
   *
   * @param shopState the {@code ShopState} instance representing the state of the shop.
   *                  Must not be null.
   * @return the current instance of {@code ShopMetaBuilder} for method chaining.
   */
  ShopMetaBuilder shopState(@NotNull final ShopState shopState);

  /**
   * Retrieves the type of shop being built or represented.
   *
   * @return an {@code IShopType} instance that defines the specific type of shop,
   *         such as a buying shop, selling shop, or other defined shop type.
   */
  IShopType shopType();

  /**
   * Sets the shop type for the shop being built. The shop type determines the nature
   * of the shop, such as whether it is a buying shop, selling shop, or other specific
   * type defined by an {@code IShopType} instance.
   *
   * @param shopType the {@code IShopType} instance representing the type of shop.
   *                 Must not be null.
   * @return the current instance of {@code ShopMetaBuilder} for method chaining.
   */
  ShopMetaBuilder shopType(@NotNull final IShopType shopType);

  /**
   * Retrieves the owner of the shop. The owner is typically the user
   * responsible for managing or associated with the shop.
   *
   * @return the {@code QUser} instance representing the owner of the shop.
   */
  QUser owner();

  /**
   * Sets the owner of the shop. The owner is typically the user associated with
   * creating or managing the shop.
   *
   * @param owner the {@code QUser} instance representing the owner of the shop.
   *              Must not be null.
   * @return the current instance of {@code ShopMetaBuilder} for method chaining.
   */
  ShopMetaBuilder owner(@NotNull final QUser owner);

  /**
   * Retrieves the tax account associated with the shop. The tax account is typically
   * a user or entity responsible for handling tax-related operations for the shop.
   *
   * @return the {@code QUser} instance representing the tax account, or {@code null}
   *         if no tax account is set.
   */
  @Nullable
  QUser taxAccount();

  /**
   * Sets the tax account for the shop. The tax account is typically associated with
   * a user or entity used for managing shop-related tax operations.
   *
   * @param taxAccount the {@code QUser} instance to assign as the tax account for the shop.
   *                   This parameter can be {@code null} if no tax account is to be set.
   * @return the current instance of {@code ShopMetaBuilder} for method chaining.
   */
  ShopMetaBuilder taxAccount(@Nullable final QUser taxAccount);

  /**
   * Determines if the shop is unlimited. An unlimited shop typically has no stock constraints
   * and can be used as an indicator for special shop configurations.
   *
   * @return true if the shop has no stock limitations (unlimited), false otherwise.
   */
  boolean isUnlimited();

  /**
   * Sets whether the shop is unlimited. An "unlimited" shop typically has no stock constraints.
   *
   * @param unlimited a boolean where true indicates the shop is unlimited,
   *                  and false indicates the shop has normal stock limits.
   * @return the current instance of ShopMetaBuilder for method chaining.
   */
  ShopMetaBuilder isUnlimited(final boolean unlimited);

  /**
   * Retrieves the BenefitProvider associated with the shop.
   *
   * @return The BenefitProvider instance that manages benefits for the shop.
   */
  BenefitProvider benefit();

  /**
   * Configures a benefit for the given user with a specified percentage and a result consumer.
   *
   * @param user    the {@code QUser} instance representing the user to whom the benefit will be applied.
   *                Must not be null.
   * @param percent the percentage of the benefit to be applied. Can be null, but should be a valid
   *                {@code BigDecimal} value if provided.
   * @param result  a {@code Consumer<Boolean>} to process the outcome of the operation.
   *                Must not be null.
   * @return the {@code ShopMetaBuilder} instance after applying the benefit.
   */
  ShopMetaBuilder withBenefit(final @NotNull QUser user, final BigDecimal percent, @NotNull final Consumer<Boolean> result);

  /**
   * Reduces the benefit associated with the specified user.
   *
   * @param user The {@code QUser} instance representing the user whose benefit should be reduced.
   *             Must not be null.
   * @return A {@code ShopMetaBuilder} instance after the benefit has been reduced for the specified user.
   */
  ShopMetaBuilder lessBenefit(final @NotNull QUser user);

  /**
   * Sets the benefit provider for the shop.
   *
   * @param benefit The BenefitProvider to associate with the shop. Must not be null.
   * @return The current instance of ShopMetaBuilder for method chaining.
   */
  ShopMetaBuilder benefit(@NotNull final BenefitProvider benefit);

  /**
   * Sets additional configuration for the shop using the provided {@code YamlConfiguration} instance.
   * This method allows for extending the shop's metadata with extra custom-defined settings.
   *
   * @param extra the {@code YamlConfiguration} instance containing additional shop configuration.
   *              Must not be null.
   * @return the current instance of {@code ShopMetaBuilder} for method chaining.
   */
  ShopMetaBuilder extra(final @NotNull YamlConfiguration extra);

  /**
   * Configures an inventory wrapper provider for the shop.
   * The inventory wrapper provider manages inventory-related operations
   * within the shop system and must not be null.
   *
   * @param inventoryWrapperProvider the name or identifier of the
   *                                 inventory wrapper provider to associate with the shop.
   *                                 Must be a non-null string.
   * @return the current instance of {@code ShopMetaBuilder} for method chaining.
   */
  ShopMetaBuilder inventoryWrapperProvider(@NotNull final String inventoryWrapperProvider);

  /**
   * Builds and returns a {@code ShopMeta} instance using the specified {@code ModernShop}.
   *
   * @param shop the {@code ModernShop} instance used to build a {@code ShopMeta}.
   *             Must be a valid {@code ModernShop} object.
   * @return a {@code ShopMeta} instance representing the metadata of the provided {@code ModernShop}.
   */
  ShopMeta build(ModernShop<?, ?, ?, ?> shop);
}