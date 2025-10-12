package com.ghostchu.quickshop.api.shop.parser.node;


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
import com.ghostchu.quickshop.api.shop.parser.Node;
import com.ghostchu.quickshop.api.shop.parser.ParserContext;
import net.kyori.adventure.text.Component;

import java.util.List;

/**
 * JoinNode
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class JoinNode implements Node {

  private final List<Node> nodes;

  public JoinNode(final List<Node> nodes) {

    this.nodes = nodes;
  }

  /**
   * Retrieves the unique identifier associated with this node.
   *
   * @return the unique identifier of the node as a string
   */
  @Override
  public String identifier() {

    return "join-node";
  }

  /**
   * Parses the provided shop information using the specified parser provider and parsing context to
   * produce a Component.
   *
   * @param parserProvider the SignParserProvider instance providing parsing logic and variable
   *                       definitions
   * @param shop           the Shop instance containing the data to be parsed
   * @param context        the ParserContext containing additional metadata or rules for the parsing
   *                       process
   *
   * @return the resulting Component created by parsing the shop with the provided parser provider
   * and context
   */
  @Override
  public Component parse(final SignParserProvider parserProvider, final Shop shop, final ParserContext context) {

    Component out = Component.empty();
    for(final Node node : nodes) {

      out = out.append(node.parse(parserProvider, shop, context));
    }
    return out;
  }
}
