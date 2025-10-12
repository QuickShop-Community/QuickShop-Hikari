package com.ghostchu.quickshop.api.shop.parser;


/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
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

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.SignParserProvider;

/**
 * ConditionNode
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface NodeCondition {

  String identifier();

  /**
   * Evaluates whether the associated condition is met based on the provided SignParserProvider,
   * Shop, and ParserContext.
   *
   * @param parserProvider the SignParserProvider instance used to access parsing utilities and variables
   * @param shop the Shop instance providing contextual data for the evaluation
   * @param context the ParserContext containing additional information for parsing or evaluation
   * @return a boolean value indicating whether the condition is satisfied
   */
  boolean test(final SignParserProvider parserProvider, final Shop shop, final ParserContext context);
}