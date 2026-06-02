package com.ghostchu.quickshop.api.operation;

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

public interface OperationResult<T> {

  /**
   * Determines whether the operation completed successfully.
   *
   * @return true if the operation was successful, false otherwise
   */
  boolean success();

  /**
   * Retrieves the result of the operation.
   *
   * @return the result of the operation as an instance of type T
   */
  T result();
}