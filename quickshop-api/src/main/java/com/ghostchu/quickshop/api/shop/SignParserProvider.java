package com.ghostchu.quickshop.api.shop;


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

import com.ghostchu.quickshop.api.shop.parser.NodeCondition;
import com.ghostchu.quickshop.api.shop.parser.Node;
import com.ghostchu.quickshop.api.shop.parser.ParserContext;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * SignParser
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface SignParserProvider {

  Pattern TOKEN = Pattern.compile("\\$\\{([^}]+)}");

  /**
   * Provides a map of variable names to functions responsible for generating components based on a shop instance
   * and parsing context. Each variable is associated with a BiFunction that takes a Shop object and a ParserContext
   * as inputs and produces a corresponding Component.
   *
   * @return a map where the keys are variable names (Strings) and the values are BiFunction objects
   *         that transform a Shop and a ParserContext into a Component
   */
  Map<String, BiFunction<Shop, ParserContext, Component>> variables();

  /**
   * Parses the given template string into a list of nodes for further processing.
   *
   * @param template the template string to be parsed into nodes
   * @return a List of parsed Node objects derived from the template
   */
  List<Node> parseTemplate(final String template);

  /**
   * Parses the provided token string into a Node object for further processing.
   *
   * @param body the token string to be parsed
   * @return a Node object derived from parsing the token string
   */
  Node parseToken(final String body);

  /**
   * Parses a given translation token into a Node object for further processing.
   *
   * @param token the translation token to be parsed into a Node
   * @return a Node object resulting from parsing the provided token
   */
  Node praseTranslationNode(final String token);

  /**
   * Parses the provided token into a Node object representing an "if" conditional structure
   * for further processing in templates or parsing logic.
   *
   * @param token the token string to be parsed into an "if" node
   * @return a Node object resulting from parsing the given token into an "if" conditional structure
   */
  Node parseIfNode(final String token);

  /**
   * Parses the provided token string into a nested Node structure for further processing.
   *
   * @param token the token string to be parsed into a nested Node
   * @return a Node object representing the nested structure derived from the token
   */
  Node parseNestedNode(final String token);

  /**
   * Parses the provided token string into a NodeCondition object for further evaluation or processing.
   *
   * @param token the token string to be parsed into a NodeCondition
   * @return a NodeCondition object resulting from parsing the given token
   */
  NodeCondition parseNodeCondition(final String token);

  /**
   * Parses the provided template string into a ConditionNode object representing operator nodes.
   * This method is used to evaluate or process operators in the parsed structure.
   *
   * @param template the template string containing operator-related content to be parsed
   * @return a ConditionNode object resulting from parsing the provided template
   */
  NodeCondition parseOperatorNodes(final String template);

  /**
   * Parses the provided template into a Component using the given shop and parser context.
   *
   * @param template the template string to be parsed
   * @param shop the shop instance providing context for parsing
   * @param context the parser context containing additional parsing details
   * @return a Component representation of the parsed template
   */
  Component parse(final String template, final Shop shop, final ParserContext context);
}