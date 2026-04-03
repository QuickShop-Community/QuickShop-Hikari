package com.ghostchu.quickshop.api.shop.meta;

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

import com.ghostchu.quickshop.api.economy.benefit.BenefitProvider;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.IShopType;
import com.ghostchu.quickshop.api.shop.state.ShopState;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * ShopIdentity
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopMeta extends ShopPrice<Double> {

  /**
   * WARNING: This UUID will changed after plugin reload, shop reload or server restart DO NOT USE
   * IT TO STORE DATA!
   *
   * @return Random UUID
   */
  @NotNull
  UUID getRuntimeRandomUniqueId();

  /**
   * Gets the Shop ID to identify the shop.
   *
   * @return Shop ID -1 if shop in creating state.
   */
  long getShopId();

  /**
   * Internal Only: Give shop that under id_waiting state an ShopId.
   *
   * @param newId The new shop id, once set will cannot change anymore.
   */
  @ApiStatus.Internal
  void setShopId(long newId);

  /**
   * Gets this shop name that set by player
   *
   * @return Shop name, or null if not set
   */
  @Nullable
  String getShopName();

  /**
   * Sets shop name
   *
   * @param shopName shop name, null to remove currently name
   */
  void setShopName(@Nullable String shopName);

  /**
   * Get shop item's ItemStack
   *
   * @return The shop's ItemStack
   */
  @NotNull
  ItemStack getItem();

  /**
   * Set shop item's ItemStack
   *
   * @param item ItemStack to set
   */
  void setItem(@NotNull ItemStack item);

  /**
   * Gets the currency that shop use
   *
   * @return The currency name
   */
  @Nullable
  String getCurrency();

  /**
   * Sets the currency that shop use
   *
   * @param currency The currency name; null to use default currency
   */
  void setCurrency(@Nullable String currency);

  /**
   * Get shop's price
   *
   * @return Price
   * @deprecated Use {@link ShopPrice#price()} instead
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  double getPrice();

  /**
   * Set shop's new price
   *
   * @param paramDouble New price
   * @deprecated Use {@link ShopPrice#price(Object)} instead.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  void setPrice(double paramDouble);

  /**
   * Retrieves the current state of the shop.
   *
   * @return the current state of the shop as a ShopState object
   */
  ShopState shopState();

  /**
   * Updates the current state of the shop based on the provided {@code ShopState}.
   *
   * @param state the new state to set for the shop; must not be null
   */
  void shopState(@NotNull ShopState state);

  /**
   * Updates or processes the state of a shop based on the provided identifier.
   *
   * @param shopStateIdentifier a non-null string representing the unique identifier
   *                             for the shop state to be updated or processed.
   */
  void shopState(@NotNull String shopStateIdentifier);

  /**
   * Retrieves the type of shop associated with this entity.
   *
   * @return an instance of IShopType representing the shop type
   */
  IShopType shopType();

  /**
   * Sets the type of shop using the provided shop type parameter.
   *
   * @param newShopType the shop type to set, must not be null
   */
  void shopType(@NotNull IShopType newShopType);

  /**
   * Specifies the type of shop based on the given identifier.
   *
   * @param shopTypeIdentifier the identifier representing the type of shop. Must not be null.
   */
  void shopType(@NotNull String shopTypeIdentifier);

  /**
   * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
   *
   * @param forceUsername Force returns username of shop
   * @param locale        The locale to parse the message
   *
   * @return owner name
   */
  @NotNull
  Component ownerName(boolean forceUsername, @NotNull ProxiedLocale locale);

  /**
   * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
   *
   * @param locale The locale to parse the message
   *
   * @return owner name
   */
  @NotNull
  Component ownerName(@NotNull ProxiedLocale locale);

  /**
   * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
   *
   * @return owner name
   */
  @NotNull
  Component ownerName();


  /**
   * Get shop's owner QUser
   *
   * @return Shop's owner QUser object, can use Bukkit.getOfflinePlayer to convert to the
   * OfflinePlayer.
   */
  @NotNull
  QUser getOwner();

  /**
   * Set new owner to the shop's owner
   *
   * @param qUser New owner user
   */
  void setOwner(@NotNull QUser qUser);

  /**
   * Getting the shop tax account for using, it can be specific uuid or general tax account
   *
   * @return Shop Tax Account or fallback to general tax account
   */
  @Nullable
  QUser getTaxAccount();

  /**
   * Sets shop taxAccount
   *
   * @param taxAccount tax account, null to use general tax account
   */
  void setTaxAccount(@Nullable QUser taxAccount);

  /**
   * Getting the shop tax account, it can be specific uuid or general tax account
   *
   * @return Shop Tax Account, null if use general tax account
   */

  @Nullable
  QUser getTaxAccountActual();

  /**
   * Gets shop status is stacking shop
   *
   * @return The shop stacking status
   */
  boolean isStackingShop();

  /**
   * Get shop is or not in Unlimited Mode (Admin Shop)
   *
   * @return yes or not
   */
  boolean isUnlimited();

  /**
   * Set shop is or not Unlimited Mode (Admin Shop)
   *
   * @param paramBoolean status
   */
  void setUnlimited(boolean paramBoolean);

  /**
   * Gets the benefit in this shop
   */
  @NotNull
  BenefitProvider getShopBenefit();

  /**
   * Sets the benefit in this shop
   */
  void setShopBenefit(@NotNull BenefitProvider benefit);
}