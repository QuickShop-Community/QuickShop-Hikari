package com.ghostchu.quickshop.shop.sign;


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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.SignParserProvider;
import com.ghostchu.quickshop.api.shop.parser.Node;
import com.ghostchu.quickshop.api.shop.parser.NodeCondition;
import com.ghostchu.quickshop.api.shop.parser.ParserContext;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * RevampedSignParserProvider
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class RevampedSignParserProvider implements SignParserProvider {

  private final Map<String, BiFunction<Shop, ParserContext, Component>> variables = new ConcurrentHashMap<>();


  public RevampedSignParserProvider(final QuickShop plugin) {

    variables.put("owner", (shop, context) -> shop.ownerName());
    variables.put("itemName", (shop, context) -> shop.customItemName());
    variables.put("price", (shop, context) -> Component.text(plugin.getShopManager().format(shop.getPrice(), shop)));
    variables.put("itemAmount", ((shop, parserContext) -> Component.text(shop.getItem().getAmount())));
    variables.put("remaining", ((shop, parserContext) -> Component.text(shop.shopType().remainingStock(shop))));
    variables.put("tradingKey", ((shop, parserContext) -> plugin.text().of(shop.shopType().tradingTranslationKey()).forLocale()));
    variables.put("stackTradingKey", ((shop, parserContext) -> plugin.text().of(shop.shopType().stackTradingTranslationKey()).forLocale()));
    variables.put("noRemainingKey", ((shop, parserContext) -> plugin.text().of(shop.shopType().outOfStockTranslationKey()).forLocale()));
    variables.put("available", ((shop, parserContext) -> Component.text(shop.inventoryAvailable())));
    variables.put("stacking", ((shop, parserContext) -> Component.text(shop.isStackingShop())));
    variables.put("hasCustomItemName", ((shop, parserContext) -> Component.text(shop.useCustomItemName())));

  }
  /**
   * Provides a map of variable names to functions responsible for generating components based on a
   * shop instance and parsing context. Each variable is associated with a BiFunction that takes a
   * Shop object and a ParserContext as inputs and produces a corresponding Component.
   *
   * @return a map where the keys are variable names (Strings) and the values are BiFunction objects
   * that transform a Shop and a ParserContext into a Component
   */
  @Override
  public Map<String, BiFunction<Shop, ParserContext, Component>> variables() {

    return variables;
  }

  /**
   * Parses the given template string into a list of nodes for further processing.
   *
   * @param template the template string to be parsed into nodes
   *
   * @return a List of parsed Node objects derived from the template
   */
  @Override
  public List<Node> parseTemplate(final String template) {
    System.out.println("parseTemplate: " + template);

    if(template == null) {
      return Collections.emptyList();
    }

    if(template.isBlank()) {
      return Collections.emptyList();
    }

    final List<Node> nodes = new ArrayList<>();
    for(int i = 0; i < template.length(); i++) {

      final boolean lastChar = i == template.length() - 1;
      final char c = template.charAt(i);

      switch(c) {

        case 't' -> {
          if(!lastChar && template.charAt(i + 1) == ':') {
            nodes.add(praseTranslationNode(template.substring(i + 1)));
          }
        }
        case 'i' -> {
          if(!lastChar && template.charAt(i + 1) == 'f' && template.charAt(i + 2) == ':') {
            nodes.add(parseIfNode(template.substring(i + 2)));
          }
        }
        case 'v' -> {
          if(!lastChar && template.charAt(i + 1) == 'a'&& template.charAt(i + 2) == 'r' && template.charAt(i + 3) == ':') {
            nodes.add(parseVarNode(template.substring(i + 3)));
          }
        }
      }
    }
    return nodes;
  }

  /**
   * Parses the provided token string into a Node object for further processing.
   *
   * @param body the token string to be parsed
   *
   * @return a Node object derived from parsing the token string
   */
  @Override
  public Node parseToken(final String body) {

    return null;
  }

  /**
   * Parses a given translation token into a Node object for further processing.
   *
   * @param token the translation token to be parsed into a Node
   *
   * @return a Node object resulting from parsing the provided token
   */
  @Override
  public Node praseTranslationNode(final String token) {

    return null;
  }

  /**
   * Parses the provided token into a Node object representing an "if" conditional structure for
   * further processing in templates or parsing logic.
   *
   * @param token the token string to be parsed into an "if" node
   *
   * @return a Node object resulting from parsing the given token into an "if" conditional structure
   */
  @Override
  public Node parseIfNode(final String token) {

    return null;
  }

  /**
   * Parses the provided token into a Node object representing an "if" conditional structure for
   * further processing in templates or parsing logic.
   *
   * @param token the token string to be parsed into an "if" node
   *
   * @return a Node object resulting from parsing the given token into an "if" conditional structure
   */
  public Node parseVarNode(final String token) {

    return null;
  }

  /**
   * Parses the provided token string into a nested Node structure for further processing.
   *
   * @param token the token string to be parsed into a nested Node
   *
   * @return a Node object representing the nested structure derived from the token
   */
  @Override
  public Node parseNestedNode(final String token) {

    return null;
  }

  /**
   * Parses the provided token string into a NodeCondition object for further evaluation or
   * processing.
   *
   * @param token the token string to be parsed into a NodeCondition
   *
   * @return a NodeCondition object resulting from parsing the given token
   */
  @Override
  public NodeCondition parseNodeCondition(final String token) {

    return null;
  }

  /**
   * Parses the provided template string into a ConditionNode object representing operator nodes.
   * This method is used to evaluate or process operators in the parsed structure.
   *
   * @param template the template string containing operator-related content to be parsed
   *
   * @return a ConditionNode object resulting from parsing the provided template
   */
  @Override
  public NodeCondition parseOperatorNodes(final String template) {

    return null;
  }
}
