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

import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.obj.QUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Defines an adapter for interacting with the world context of a shop within the system.
 * This interface provides methods for handling shop signs, validating shops, updating display
 * locations, and managing localized sign text among other operations.
 * <strong>NOTE: Any methods related to actions that need to be performed in the world itself, any methods in here
 * need to happen on the main thread</strong>
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopWorldAdapter {

  /**
   * Checks whether the specified shop instance is valid.
   * This method determines if the given shop meets the necessary criteria
   * for being considered a valid shop within the system.
   *
   * @param shop The shop instance to validate, represented by a {@code ModernShop<?, ?, ?, ?>}.
   * @return {@code true} if the shop is valid, {@code false} otherwise.
   * @since 6.3.0.0
   */
  boolean isValidShop(@NotNull final ModernShop<?, ?, ?, ?> shop);

  /**
   * Check if shop is not valided for specific player
   *
   * @param uuid The uuid of the player
   * @param info The info of the shop
   * @param shop The shop
   *
   * @return If the shop is not valided for the player
   */
  boolean shopIsNotValid(@NotNull QUser uuid, @NotNull Info info, @NotNull ModernShop<?, ?, ?, ?> shop);

  /**
   * Determines whether the specified block is attached to the given shop instance.
   *
   * This method checks if the provided block is associated with the given shop
   * based on certain conditions, which could involve physical proximity, structural
   * linkage, or other criteria defined within the system.
   *
   * @param shop The shop instance to check against, represented by a {@code ModernShop<?, ?, ?, ?>}.
   *             Must not be null.
   * @param b    The block to verify as being attached. Must not be null.
   * @return {@code true} if the block is attached to the specified shop, {@code false} otherwise.
   * @since 6.3.0.0
   */
  boolean isAttached(@NotNull final ModernShop<?, ?, ?, ?> shop, @NotNull final Block b);

  /**
   * Ensures that the display location for the specified shop is correctly handled.
   * This method may involve verifying the shop's display location, teleporting entities,
   * or respawning display objects as required to maintain consistency.
   *
   * @param shop The shop instance whose display location is to be checked and updated,
   *             represented by a {@code ModernShop<?, ?, ?, ?>}.
   * @since 6.3.0.0
   */
  void checkDisplay(@NotNull final ModernShop<?, ?, ?, ?> shop);

  /**
   * Claim a sign as shop sign (modern method)
   *
   * @param shop The shop instance that the sign belongs to, represented by a {@code ModernShop<?, ?, ?, ?>}.
   * @param sign The shop sign
   * @since 6.3.0.0
   */
  void claimShopSign(final @NotNull ModernShop<?, ?, ?, ?> shop, @NotNull Sign sign);

  @NotNull
  BlockState makeShopSign(@NotNull Block container, @NotNull Block signBlock, @Nullable Material signMaterial);

  /**
   * Retrieves the localized text to be displayed on a shop's sign.
   * This method provides the sign text in the form of a list of {@link Component},
   * customized according to the specified shop instance and locale.
   *
   * @param shop   The shop instance for which the sign text is to be retrieved,
   *               represented by a {@code ModernShop<?, ?, ?, ?>}.
   * @param locale The locale to be used for generating the sign text,
   *               represented by a {@code ProxiedLocale}.
   * @return A list of {@link Component} objects representing the text of the shop's sign,
   *         with each list entry corresponding to a line of text.
   * @since 6.3.0.0
   */
  default List<Component> getSignText(@NotNull final ModernShop<?, ?, ?, ?> shop,
                                      @NotNull final ProxiedLocale locale) {
    //backward support
    throw new UnsupportedOperationException();
  }

  /**
   * Get shop signs, may have multi signs
   *
   * @return Signs for the shop
   * @since 6.3.0.0
   */
  @NotNull
  List<Sign> getSigns(@NotNull final ModernShop<?, ?, ?, ?> shop);

  /**
   * Checks if a Sign is a ShopSign
   *
   * @param shop The shop instance that the sign belongs to, represented by a {@code ModernShop<?, ?, ?, ?>}.
   * @param sign Target {@link Sign}
   *
   * @return Is shop info sign
   * @since 6.3.0.0
   */
  boolean isShopSign(@NotNull final ModernShop<?, ?, ?, ?> shop, @NotNull Sign sign);

  /**
   * Generate new sign texts on shop's sign.
   * @since 6.3.0.0
   */
  void setSignText(@NotNull final ModernShop<?, ?, ?, ?> shop);

  /**
   * Sets the text displayed on a shop's sign.
   * This method allows for updating the sign text of the specified shop
   * using a list of {@link Component} objects where each entry represents a line of text.
   *
   * @param shop The shop instance whose sign text is to be updated, represented by a
   *             {@code ModernShop<?, ?, ?, ?>}.
   * @param paramArrayOfString A list of {@link Component} objects representing the new sign text,
   *                           with each list entry corresponding to a line of text.
   * @since 6.3.0.0
   */
  void setSignText(@NotNull final ModernShop<?, ?, ?, ?> shop, @NotNull List<Component> paramArrayOfString);

  /**
   * Updates the text displayed on a shop's sign with content localized to the given locale.
   * This method dynamically adjusts the shop sign's text based on the specified shop instance
   * and target locale for display purposes.
   *
   * @param shop   The shop instance whose sign text is to be updated, represented by a
   *               {@code ModernShop<?, ?, ?, ?>}.
   * @param locale The locale used to localize the text displayed on the shop's sign,
   *               represented by a {@code ProxiedLocale}.
   * @since 6.3.0.0
   */
  void setSignText(@NotNull final ModernShop<?, ?, ?, ?> shop, @NotNull ProxiedLocale locale);
}