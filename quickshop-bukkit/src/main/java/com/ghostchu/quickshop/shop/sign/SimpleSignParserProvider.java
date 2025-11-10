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
import com.ghostchu.quickshop.api.shop.parser.condition.ConditionFlags;
import com.ghostchu.quickshop.api.shop.parser.condition.ConditionOperations;
import com.ghostchu.quickshop.api.shop.parser.condition.FlagCondition;
import com.ghostchu.quickshop.api.shop.parser.condition.NumericEquationCondition;
import com.ghostchu.quickshop.api.shop.parser.node.IfNode;
import com.ghostchu.quickshop.api.shop.parser.node.JoinNode;
import com.ghostchu.quickshop.api.shop.parser.node.TextNode;
import com.ghostchu.quickshop.api.shop.parser.node.TranslationNode;
import com.ghostchu.quickshop.api.shop.parser.node.VarNode;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;

/**
 * SimpleSignParserProvider
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class SimpleSignParserProvider implements SignParserProvider {

  private final Map<String, BiFunction<Shop, ParserContext, Component>> variables = new ConcurrentHashMap<>();


  public SimpleSignParserProvider(final QuickShop plugin) {

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

    final Matcher matcher = TOKEN.matcher(template);
    int lastIndex = 0;
    while(matcher.find()) {

      System.out.println("Group: " + matcher.group(1));
      System.out.println("Start: " + matcher.start());
      System.out.println("Last: " + lastIndex);
      /*if(matcher.start() > lastIndex) {
        System.out.println("TextNode");

        nodes.add(new TextNode(template.substring(lastIndex, matcher.start())));
      }*/

      nodes.add(parseToken(matcher.group(1)));
      lastIndex = matcher.end();
    }

    if(lastIndex < template.length()) {
      System.out.println("TextNode2");

      nodes.add(new TextNode(template.substring(lastIndex)));
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
    System.out.println("parsetoken: " + body);

    if(body.startsWith("t:")) {
      return praseTranslationNode(body.substring(2));
    }

    if(body.startsWith("if:")) {
      return parseIfNode(body.substring(3));
    }

    if(body.startsWith("var:")) {
      return new VarNode(body.substring(4).trim());
    }

    return new TextNode(body);
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

    final String[] parts = token.split("\\|", -1);
    final String key = parts[0];
    System.out.println("parseTranslation: " + token);
    System.out.println("key: " + key);

    final List<Node> arguments = new ArrayList<>(parts.length - 1);
    for(int i = 1; i < parts.length; i++) {

      arguments.add(parseNestedNode(parts[i].trim()));
    }

    return new TranslationNode(key, arguments);
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

    final int ternaryIndex = token.indexOf("?");
    final int colonIndex = token.indexOf("~");
    System.out.println("parseIf: " + token);

    final String condition = token.substring(0, ternaryIndex).trim();
    final String trueValue = token.substring(ternaryIndex + 1, colonIndex).trim();
    final String falseValue = token.substring(colonIndex + 1).trim();

    System.out.println("condition: " + condition);
    System.out.println("true: " + trueValue);
    System.out.println("false: " + falseValue);
    final NodeCondition conditionNode = parseNodeCondition(condition);

    return new IfNode(conditionNode, parseNestedNode(trueValue), parseNestedNode(falseValue));
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

    System.out.println("parseNested: " + token);
    final List<Node> nodes = parseTemplate(token);

    if(nodes.size() == 1) {

      System.out.println("first node");
      return nodes.getFirst();
    }
    System.out.println("join node");
    return new JoinNode(nodes);
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
    final String flagString = token.replaceAll("\\s+", "");
    System.out.println("parseCondition: " + flagString);

    if(flagString.startsWith("available=")) {

      return new FlagCondition(ConditionFlags.AVAILABLE, flagString.substring("available=".length()).equals("true"));
    }

    if(flagString.startsWith("stacking=")) {

      return new FlagCondition(ConditionFlags.STACKING, flagString.substring("stacking=".length()).equals("true"));
    }

    if(flagString.startsWith("hasCustomItemName=")) {

      return new FlagCondition(ConditionFlags.CUSTOM_NAME, flagString.substring("hasCustomItemName=".length()).equals("true"));
    }

    if(flagString.startsWith("remaining")) {

      final String operator = flagString.substring("remaining".length());
      return parseOperatorNodes(operator);
    }
    return new FlagCondition(ConditionFlags.DEFAULT, false);
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
    System.out.println("parseOperation: " + template);

    if(template.startsWith("=")) {

      return new NumericEquationCondition(ConditionOperations.EQUAL, Integer.parseInt(template.substring(1)));
    }

    if(template.startsWith("!=")) {

      return new NumericEquationCondition(ConditionOperations.NOT_EQUAL, Integer.parseInt(template.substring(2)));
    }

    if(template.startsWith("<")) {

      return new NumericEquationCondition(ConditionOperations.LESS_THAN, Integer.parseInt(template.substring(1)));
    }

    if(template.startsWith("<=")) {

      return new NumericEquationCondition(ConditionOperations.LESS_THAN_OR_EQUAL, Integer.parseInt(template.substring(2)));
    }

    if(template.startsWith(">")) {

      return new NumericEquationCondition(ConditionOperations.GREATER_THAN, Integer.parseInt(template.substring(1)));
    }

    if(template.startsWith(">=")) {

      return new NumericEquationCondition(ConditionOperations.GREATER_THAN_OR_EQUAL, Integer.parseInt(template.substring(2)));
    }

    throw new IllegalArgumentException("Unsupported operator applied" + template);
  }
}