package com.ghostchu.quickshop.api.operation.result;

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

import com.ghostchu.quickshop.api.inventory.ItemRemoveResult;
import com.ghostchu.quickshop.api.operation.OperationResult;

public class ItemRemoveOperationResult implements OperationResult<ItemRemoveResult> {

  private final boolean success;
  private final ItemRemoveResult result;

  public ItemRemoveOperationResult(final boolean success, final ItemRemoveResult result) {

    this.success = success;
    this.result = result;
  }

  @Override
  public boolean success() {

    return this.success;
  }

  @Override
  public ItemRemoveResult result() {

    return this.result;
  }
}