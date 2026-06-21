package com.ghostchu.quickshop.shop.display.virtual;

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
import com.ghostchu.quickshop.api.event.packet.handler.PacketHandlerAddedEvent;
import com.ghostchu.quickshop.api.event.packet.handler.PacketHandlerInitEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.display.DisplayManager;
import com.ghostchu.quickshop.api.shop.display.PacketFactory;
import com.ghostchu.quickshop.api.shop.display.PacketHandler;
import com.ghostchu.quickshop.shop.display.virtual.packet.PacketEventsHandler;
import com.ghostchu.quickshop.shop.display.virtual.packet.ProtocolLibHandler;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualDisplayItemManager implements DisplayManager<VirtualDisplayItem<?>> {

  private static VirtualDisplayItemManager instance;
  public final Map<Long, Integer> shopEntities = new ConcurrentHashMap<>();
  protected final Map<String, PacketHandler<?>> packetHandlers = new LinkedHashMap<>();
  private final ConcurrentHashMap<ShopChunk, ConcurrentHashMap<Integer, VirtualDisplayItem<?>>> chunksMapping = new ConcurrentHashMap<>();
  private final QuickShop plugin;
  private final AtomicInteger entityIdCounter;
  private PacketHandler<?> packetHandler;
  private PacketFactory<?> packetFactory;
  private boolean testPassed = true;

  public VirtualDisplayItemManager(final QuickShop plugin) {

    instance = this;

    //We handle our default packet handlers
    addHandler(new PacketEventsHandler());
    addHandler(new ProtocolLibHandler());

    setHandler();

    if(this.packetHandler != null) {
      this.plugin = plugin;

      final PacketHandlerInitEvent initEvent = new PacketHandlerInitEvent(this.packetHandler);
      if(initEvent.callCancellableEvent()) {

        Log.debug("Canceled the initialization of PacketHandler: " + this.packetHandler.identifier());
        throw new IllegalStateException("No suitable packet handler found for virtual display item management. Please make sure either PacketEvents or ProtocolLib is installed.");
      } else {

        this.packetHandler.initialize();
      }
      this.entityIdCounter = new AtomicInteger(Integer.MAX_VALUE);

      load();
    } else {

      throw new IllegalStateException("No suitable packet handler found for virtual display item management. Please make sure either PacketEvents or ProtocolLib is installed.");
    }
  }

  public static VirtualDisplayItemManager instance() {

    return instance;
  }

  /**
   * Retrieves a mapping between ShopChunk objects and their associated lists of display entities.
   *
   * This method provides access to the internal data structure used by the display manager to store
   * and organize displays according to their respective ShopChunk locations. The returned mapping
   * allows efficient retrieval, addition, or removal of display entities based on chunk-specific
   * contexts.
   *
   * @return A {@code ConcurrentHashMap} where the keys represent {@code ShopChunk} objects, and the
   * values are lists of display entities of type {@code T} associated with the respective chunks.
   */
  @Override
  public ConcurrentHashMap<ShopChunk, ConcurrentHashMap<Integer, VirtualDisplayItem<?>>> chunksMapping() {

    return chunksMapping;
  }

  public void setHandler() {

    final String preferred = QuickShop.getInstance().getConfig().getString("shop.display-protocol", "protocollib").toLowerCase(Locale.ROOT);

    //attempt to use the preferred packet handler.
    final PacketHandler<?> handler = packetHandlers.get(preferred);
    if(handler != null && Bukkit.getPluginManager().getPlugin(handler.pluginName()) != null) {

      this.packetHandler = handler;
      return;
    }

    for(final PacketHandler<?> packetHandler : packetHandlers.values()) {

      if(Bukkit.getPluginManager().getPlugin(packetHandler.pluginName()) != null) {

        this.packetHandler = packetHandler;
      }
    }
  }


  @Override
  public void load() {

    Log.debug("Attempting to load packet factory...");

    final Optional<PacketFactory<?>> factoryOptional = packetHandler.factory(plugin.platform().getMinecraftVersion());
    if(factoryOptional.isEmpty()) {

      throw new IllegalStateException("No PacketFactory found for platform version " + plugin.platform().getMinecraftVersion());
    }

    this.packetFactory = factoryOptional.get();
    Log.debug("Attempting to register chunk packet listeners...");

    if(packetFactory != null) {

      packetFactory.registerSendChunk();
      packetFactory.registerUnloadChunk();
    }
  }

  @Override
  public void unload() {

    Log.debug("Unregistering the packet listener...");
    if(packetFactory != null) {

      packetFactory.unregisterSendChunk();
      packetFactory.unregisterUnloadChunk();
    }
  }

  public int generateEntityId() {

    return entityIdCounter.getAndDecrement();
  }

  @Override
  @NotNull
  public VirtualDisplayItem<?> create(@NotNull final Shop shop) {

    return new VirtualDisplayItem<>(this, packetFactory, shop);
  }

  public void addHandler(final PacketHandler<?> packetHandler) {

    final PacketHandlerAddedEvent addedEvent = new PacketHandlerAddedEvent(packetHandler);
    if(addedEvent.callCancellableEvent()) {

      Log.debug("Canceled the addition of PacketHandler: " + packetHandler.identifier());

    } else {

      packetHandlers.put(packetHandler.identifier().toLowerCase(Locale.ROOT), packetHandler);
    }
  }

  public PacketHandler<?> packetHandler() {

    return packetHandler;
  }
}
