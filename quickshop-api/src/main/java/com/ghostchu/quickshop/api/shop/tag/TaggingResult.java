package com.ghostchu.quickshop.api.shop.tag;

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
 * TaggingResult
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public enum TaggingResult {

  SUCCESS,
  NOT_FOUND,
  ALREADY_EXISTS,
  DATABASE_ERROR,
  INVALID_TAG,
  PLAYER_MAX_TAG_LIMIT_REACHED
}