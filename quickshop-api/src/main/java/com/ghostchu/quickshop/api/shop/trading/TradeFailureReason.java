package com.ghostchu.quickshop.api.shop.trading;

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
 * TradeFailReason
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public enum TradeFailureReason {

  NO_PERMISSION,
  SELF_TRADE_DENIED,
  SHOP_INVALID,
  SHOP_FROZEN,
  SHOP_DISABLED,
  INVALID_AMOUNT,
  STOCK_TOO_LOW,
  INVENTORY_FULL,
  SHOP_NO_SPACE,
  LIMIT_REACHED,
  INSUFFICIENT_FUNDS,
  ITEM_NOT_ENOUGH,
  ECONOMY_TRANSACTION_NOT_COMPLETABLE,
  ECONOMY_TRANSACTION_FAILED,
  INVENTORY_TRANSACTION_FAILED,
  SHOP_TRANSACTION_FAILED,
  EVENT_CANCELLED,
  INTERNAL_ERROR
}