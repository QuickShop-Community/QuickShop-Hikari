package com.ghostchu.quickshop.api.shop.parser.condition;


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
import com.ghostchu.quickshop.api.shop.parser.NodeCondition;
import com.ghostchu.quickshop.api.shop.parser.ParserContext;

/**
 * EquationCondition
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class NumericEquationCondition implements NodeCondition {

  private final ConditionOperations operation;
  private final int rightHandSide;

  public NumericEquationCondition(final ConditionOperations operation, final int rightHandSide) {

    this.operation = operation;
    this.rightHandSide = rightHandSide;
  }

  @Override
  public String identifier() {

    return "equation-condition";
  }

  /**
   * Evaluates whether the associated condition is met based on the provided SignParserProvider,
   * Shop, and ParserContext.
   *
   * @param parserProvider the SignParserProvider instance used to access parsing utilities and
   *                       variables
   * @param shop           the Shop instance providing contextual data for the evaluation
   * @param context        the ParserContext containing additional information for parsing or
   *                       evaluation
   *
   * @return a boolean value indicating whether the condition is satisfied
   */
  @Override
  public boolean test(final SignParserProvider parserProvider, final Shop shop, final ParserContext context) {

    final int leftHandSide = shop.shopType().remainingStock(shop);
    return switch(operation) {

      case NOT_EQUAL -> leftHandSide != rightHandSide;
      case LESS_THAN -> leftHandSide < rightHandSide;
      case LESS_THAN_OR_EQUAL -> leftHandSide <= rightHandSide;
      case GREATER_THAN -> leftHandSide > rightHandSide;
      case GREATER_THAN_OR_EQUAL -> leftHandSide >= rightHandSide;

      default -> leftHandSide == rightHandSide;
    };
  }
}