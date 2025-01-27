package com.ghostchu.quickshop.shop.display.virtual.packet;
/*
 * QuickShop-Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
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

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.ghostchu.quickshop.api.shop.display.PacketFactory;
import com.ghostchu.quickshop.api.shop.display.PacketHandler;
import com.ghostchu.quickshop.shop.display.virtual.packet.protocollib.PacketFactoryv1_20;
import com.ghostchu.quickshop.shop.display.virtual.packet.protocollib.PacketFactoryv1_21;

import java.util.HashMap;
import java.util.Map;

/**
 * ProtocolLibHandler
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class ProtocolLibHandler implements PacketHandler<ProtocolManager> {

  protected final Map<String, PacketFactory<?>> factories = new HashMap<>();

  protected static ProtocolLibHandler instance;

  protected ProtocolManager protocolManager;

  public ProtocolLibHandler() {

    instance = this;
  }

  /**
   * Retrieves a map of PacketFactory instances keyed by the game version supported.
   *
   * @return Map of PacketFactory instances where keys are the game version supported.
   */
  @Override
  public Map<String, PacketFactory<?>> factories() {

    return factories;
  }

  /**
   * Retrieves the identifier for the PacketHandler.
   *
   * @return The identifier for the PacketHandler.
   */
  @Override
  public String identifier() {

    return "protocollib";
  }

  /**
   * Retrieves the name of the plugin.
   *
   * @return The name of the plugin as a String.
   */
  @Override
  public String pluginName() {

    return "ProtocolLib";
  }

  /**
   * Initializes the object or resource.
   */
  @Override
  public void initialize() {

    this.protocolManager = ProtocolLibrary.getProtocolManager();

    final PacketFactoryv1_20 oneTwenty = new PacketFactoryv1_20();
    factories.put("1.20.1", oneTwenty);
    factories.put("1.20.2", oneTwenty);
    factories.put("1.20.3", oneTwenty);
    factories.put("1.20.4", oneTwenty);
    factories.put("1.20.5", oneTwenty);

    final PacketFactoryv1_21 oneTwentyOne = new PacketFactoryv1_21();
    factories.put("1.20.6", oneTwentyOne);
    factories.put("1.21", oneTwentyOne);
    factories.put("1.21.1", oneTwentyOne);
    factories.put("1.21.2", oneTwentyOne);
    factories.put("1.21.3", oneTwentyOne);
    factories.put("1.21.4", oneTwentyOne);
  }

  public static ProtocolLibHandler instance() {

    return instance;
  }

  @Override
  public ProtocolManager internal() {

    return protocolManager;
  }
}