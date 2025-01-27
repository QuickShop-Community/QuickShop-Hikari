package com.ghostchu.quickshop.shop.display.virtual;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.packet.handler.PacketHandlerAddedEvent;
import com.ghostchu.quickshop.api.event.packet.handler.PacketHandlerInitEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.display.PacketFactory;
import com.ghostchu.quickshop.api.shop.display.PacketHandler;
import com.ghostchu.quickshop.shop.display.virtual.packet.PacketEventsHandler;
import com.ghostchu.quickshop.shop.display.virtual.packet.ProtocolLibHandler;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualDisplayItemManager {

  protected final Map<String, PacketHandler<?>> packetHandlers = new LinkedHashMap<>();

  public final Map<Long, Integer> shopEntities = new ConcurrentHashMap<>();

  @Getter
  private final Map<ShopChunk, List<VirtualDisplayItem<?>>> chunksMapping = new ConcurrentHashMap<>();

  private final QuickShop plugin;
  private final AtomicInteger entityIdCounter;

  private PacketHandler<?> packetHandler;

  private PacketFactory<?> packetFactory;

  private static VirtualDisplayItemManager instance;

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

  public void setHandler() {

    for(final PacketHandler<?> packetHandler : packetHandlers.values()) {

      if(Bukkit.getPluginManager().getPlugin(packetHandler.pluginName()) != null) {

        this.packetHandler = packetHandler;
      }
    }
  }

  public void load() {
    Log.debug("Attempting to load packet factory...");

    final Optional<PacketFactory<?>> factoryOptional  = packetHandler.factory(plugin.getPlatform().getMinecraftVersion());
    if(factoryOptional.isEmpty()) {

      throw new IllegalStateException("No PacketFactory found for platform version " + plugin.getPlatform().getMinecraftVersion());
    }

    this.packetFactory = factoryOptional.get();
    Log.debug("Attempting to register chunk packet listeners...");

    if(packetFactory != null) {

      packetFactory.registerSendChunk();
      packetFactory.registerUnloadChunk();
    }
  }

  public void put(@NotNull final ShopChunk key, @NotNull final VirtualDisplayItem<?> value) {

    //Thread-safe was ensured by ONLY USE Map method to do something
    final List<VirtualDisplayItem<?>> virtualDisplayItems = new ArrayList<>(Collections.singletonList(value));
    chunksMapping.merge(key, virtualDisplayItems, (mapOldVal, mapNewVal)->{

      mapOldVal.addAll(mapNewVal);
      return mapOldVal;
    });
  }

  public void remove(@NotNull final ShopChunk key, @NotNull final VirtualDisplayItem value) {

    chunksMapping.computeIfPresent(key, (mapOldKey, mapOldVal)->{
      mapOldVal.remove(value);
      return mapOldVal;
    });
  }

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

  @NotNull
  public VirtualDisplayItem<?> createVirtualDisplayItem(@NotNull final Shop shop) {

    return new VirtualDisplayItem<>(this, packetFactory, shop);
  }

  public void setTestPassed(final boolean testPassed) {

    this.testPassed = testPassed;
  }

  public boolean isTestPassed() {

    return testPassed;
  }

  public void addHandler(final PacketHandler<?> packetHandler) {

    final PacketHandlerAddedEvent addedEvent = new PacketHandlerAddedEvent(packetHandler);
    if(addedEvent.callCancellableEvent()) {

      Log.debug("Canceled the addition of PacketHandler: " + packetHandler.identifier());

    } else {

      packetHandlers.put(packetHandler.identifier(), packetHandler);
    }
  }

  public Map<String, PacketHandler<?>> packetHandlers() {

    return packetHandlers;
  }

  public PacketHandler<?> packetHandler() {

    return packetHandler;
  }

  public static VirtualDisplayItemManager instance() {
    return instance;
  }
}
