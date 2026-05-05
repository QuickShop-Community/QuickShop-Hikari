package com.ghostchu.quickshop.shop;

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
import com.ghostchu.quickshop.api.shop.ShopService;
import com.ghostchu.quickshop.api.shop.service.ShopActionResult;
import com.ghostchu.quickshop.api.shop.service.request.ShopCreateRequest;
import com.ghostchu.quickshop.api.shop.service.request.ShopDeleteRequest;
import com.ghostchu.quickshop.api.shop.service.request.ShopUpdateRequest;
import com.ghostchu.quickshop.api.shop.service.result.ShopChangeType;
import com.ghostchu.quickshop.api.shop.service.result.ShopCreateResult;
import com.ghostchu.quickshop.api.shop.service.result.ShopDeleteResult;
import com.ghostchu.quickshop.api.shop.service.result.ShopUpdateResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

/**
 * SimpleShopService
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class SimpleShopService implements ShopService {

  /**
   * Creates a new shop based on the provided creation request.
   *
   * @param request The request object containing all necessary information for shop creation,
   *                including the actor initiating the operation and the shop details.
   *
   * @return A result object encapsulating details about the shop creation operation, including the
   * success status, the created shop instance (if successful), and potential failure reasons if the
   * operation failed.
   */
  @Override
  public @NotNull ShopActionResult<ShopCreateResult> createShop(final @NotNull ShopCreateRequest request) {

    return null;
  }

  /**
   * Updates an existing shop based on the provided update request.
   *
   * @param request The request object containing the necessary details to update a shop, including
   *                the actor performing the operation and the shop to be updated.
   *
   * @return A result object containing details about the update operation, including the success
   * status, any changes made, and potential failure reasons if the operation was unsuccessful.
   */
  @Override
  public @NotNull ShopActionResult<ShopUpdateResult> updateShop(final @NotNull ShopUpdateRequest request) {

    //TODO: shopManager.findShop(request.shop().meta().shopId())
    final ModernShop<?, ?, ?, ?> originalShop = null;

    final EnumSet<ShopChangeType> changes = EnumSet.noneOf(ShopChangeType.class);
    changes.addAll(request.shop().item().diff((originalShop == null)? null : originalShop.item()));
    changes.addAll(request.shop().meta().diff((originalShop == null)? null : originalShop.meta()));
    changes.addAll(request.shop().permission().diff((originalShop == null)? null : originalShop.permission()));
    changes.addAll(request.shop().price().diff((originalShop == null)? null : originalShop.price()));

    final ShopUpdateResult result = new ShopUpdateResult(changes, originalShop, request.shop());

    if(!request.options().checkPermissions()) {
      return new ShopActionResult<>(result, true, null);
    }

    final CommandSender actor = request.actor();
    if(actor == null) {
      return new ShopActionResult<>(result, true, null);
    }
    // return result

    return null;
  }

  /**
   * Deletes a shop based on the provided delete request.
   *
   * @param request The request object containing the necessary details to delete a shop, such as
   *                the actor performing the operation and the target shop ID.
   *
   * @return A result object containing information about the outcome of the delete operation,
   * including success status and possible failure reasons.
   */
  @Override
  public @NotNull ShopActionResult<ShopDeleteResult> deleteShop(final @NotNull ShopDeleteRequest request) {

    return null;
  }

  @Override
  public ShopActionResult handleLoading() {

    return null;
  }

  @Override
  public ShopActionResult handleUnloading(final boolean dontTouchWorld) {

    return null;
  }
}