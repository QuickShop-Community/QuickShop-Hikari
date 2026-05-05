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

import com.ghostchu.quickshop.api.shop.service.ShopActionResult;
import com.ghostchu.quickshop.api.shop.service.request.ShopCreateRequest;
import com.ghostchu.quickshop.api.shop.service.request.ShopDeleteRequest;
import com.ghostchu.quickshop.api.shop.service.request.ShopUpdateRequest;
import com.ghostchu.quickshop.api.shop.service.result.ShopUpdateResult;
import com.ghostchu.quickshop.api.shop.service.result.ShopCreateResult;
import com.ghostchu.quickshop.api.shop.service.result.ShopDeleteResult;
import org.jetbrains.annotations.NotNull;

/**
 * ShopService
 *
 * T = Player object
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopService {

  /**
   * Creates a new shop based on the provided creation request.
   *
   * @param request The request object containing all necessary information for shop creation,
   *                including the actor initiating the operation and the shop details.
   * @return A result object encapsulating details about the shop creation operation,
   *         including the success status, the created shop instance (if successful),
   *         and potential failure reasons if the operation failed.
   */
  @NotNull
  ShopActionResult<ShopCreateResult> createShop(@NotNull final ShopCreateRequest request);

  /**
   * Updates an existing shop based on the provided update request.
   *
   * @param request The request object containing the necessary details to update a shop,
   *                including the actor performing the operation and the shop to be updated.
   * @return A result object containing details about the update operation, including
   *         the success status, any changes made, and potential failure reasons if the
   *         operation was unsuccessful.
   */
  @NotNull
  ShopActionResult<ShopUpdateResult> updateShop(@NotNull final ShopUpdateRequest request);

  /**
   * Deletes a shop based on the provided delete request.
   *
   * @param request The request object containing the necessary details
   *                to delete a shop, such as the actor performing the operation
   *                and the target shop ID.
   * @return A result object containing information about the outcome of
   *         the delete operation, including success status and possible
   *         failure reasons.
   */
  @NotNull
  ShopActionResult<ShopDeleteResult> deleteShop(@NotNull final ShopDeleteRequest request);

  @Deprecated()
  ShopActionResult handleLoading();

  @Deprecated()
  ShopActionResult handleUnloading(boolean dontTouchWorld);
}