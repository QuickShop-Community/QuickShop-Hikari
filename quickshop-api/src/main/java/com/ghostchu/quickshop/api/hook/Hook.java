package com.ghostchu.quickshop.api.hook;

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

/**
 * Hook
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface Hook {

  /**
   * Retrieves the identifier for the hook.
   *
   * @return a non-null, unique string representing the identifier of the hook
   */
  String identifier();

  /**
   * Checks whether the hook is currently enabled.
   *
   * @return true if the hook is enabled, false otherwise
   */
  boolean canEnable();

  /**
   * Enables the hook and updates its state to active.
   *
   * @return true if the hook was successfully enabled, false if the operation failed
   */
  boolean enable();

  /**
   * Disables the hook and updates its state to inactive.
   *
   * @return true if the hook was successfully disabled, false if the operation failed
   */
  boolean disable();
}