package com.ghostchu.quickshop.compatibility.craftengine;

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

import com.ghostchu.quickshop.api.shop.display.PacketFactory;
import com.ghostchu.quickshop.api.shop.display.PacketHandler;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * CraftEnginePacketHandler
 *
 * <p>A {@link PacketHandler} which is registered into QuickShop's virtual display manager and wraps
 * the packet handler QuickShop already selected (PacketEvents or ProtocolLib). Everything is
 * delegated to that handler, only the display item metadata packet is deferred so that the
 * CraftEngine client bound item can be built for each receiving player.</p>
 *
 * @author YuanYuanOwO
 * @since 6.3.0.3
 */
final class CraftEnginePacketHandler implements PacketHandler<Object> {

  private final Logger logger;
  private final CraftEngineItemConverter converter;
  private final PacketHandler<?> delegate;
  private final Map<String, PacketFactory<?>> wrappedFactories = new HashMap<>();
  private boolean initialized;

  CraftEnginePacketHandler(final Logger logger,
                           final CraftEngineItemConverter converter,
                           final PacketHandler<?> delegate) {

    this.logger = logger;
    this.converter = converter;
    this.delegate = delegate;
  }

  /**
   * Builds the wrapped packet factories of the underlying packet library.
   *
   * <p>May be called multiple times, only the first call does the work.</p>
   */
  @Override
  public void initialize() {

    if(this.initialized) return;
    if(this.delegate.factories().isEmpty()) {
      //The wrapped handler was not initialized yet (happens while QuickShop rebuilds its manager)
      this.delegate.initialize();
    }
    this.delegate.factories().forEach((version, factory)->this.wrappedFactories.put(
            version,
            new CraftEnginePacketFactory(factory, this.converter)));
    this.initialized = true;
    this.logger.info("Hooked the " + this.delegate.identifier() + " packet factory ("
                             + this.wrappedFactories.size() + " supported version(s)) to convert CraftEngine display items.");
  }

  @Override
  public Map<String, PacketFactory<?>> factories() {

    return this.wrappedFactories;
  }

  /**
   * Reuses the identifier of the wrapped handler on purpose.
   *
   * <p>QuickShop selects the virtual display protocol by identifier, so registering this adapter
   * under the very same identifier as the handler QuickShop currently uses makes it take over the
   * display packets without any configuration change.</p>
   */
  @Override
  public String identifier() {

    return this.delegate.identifier();
  }

  /**
   * @return the packet handler of the real packet library which is wrapped by this adapter
   */
  PacketHandler<?> delegate() {

    return this.delegate;
  }

  @Override
  public String pluginName() {

    return this.delegate.pluginName();
  }

  @Override
  public Object internal() {

    return this.delegate.internal();
  }

  @Override
  public ItemStack filterEnchantments(final ItemStack itemStack) {

    return this.delegate.filterEnchantments(itemStack);
  }
}
