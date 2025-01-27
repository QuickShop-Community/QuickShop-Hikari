package com.ghostchu.quickshop.shop.display.virtual;

import com.ghostchu.quickshop.api.event.display.DisplayApplicableCheckEvent;
import com.ghostchu.quickshop.api.event.display.ShopDisplayItemSpawnEvent;
import com.ghostchu.quickshop.api.event.packet.handler.PacketHandlerInitEvent;
import com.ghostchu.quickshop.api.event.packet.send.PacketHandlerSendDestroyEvent;
import com.ghostchu.quickshop.api.event.packet.send.PacketHandlerSendMetaEvent;
import com.ghostchu.quickshop.api.event.packet.send.PacketHandlerSendSpawnEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.api.shop.display.PacketFactory;
import com.ghostchu.quickshop.shop.SimpleShopChunk;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class VirtualDisplayItem<T> extends AbstractDisplayItem implements Reloadable {

  private final int entityID;
  //The List which store packet sender
  private final Set<UUID> packetSenders = new ConcurrentSkipListSet<>();
  private final PacketFactory<T> packetFactory;

  private final VirtualDisplayItemManager manager;

  private final T spawnPacket;
  private final T metaPacket;
  private final T velocityPacket;
  private final T destroyPacket;

  //cache chunk x and z
  private ShopChunk chunkLocation;

  //If packet initialized
  private boolean isSpawned = false;
  //packets

  VirtualDisplayItem(final VirtualDisplayItemManager manager, final PacketFactory<T> packetFactory, final Shop shop) {

    super(shop);

    this.entityID = manager.generateEntityId();
    this.manager = manager;
    this.manager.shopEntities.put(shop.getShopId(), entityID);
    this.packetFactory = packetFactory;

    if(getDisplayLocation() != null) {

      this.spawnPacket = packetFactory.createSpawnPacket(entityID, getDisplayLocation());
      this.metaPacket = packetFactory.createMetaDataPacket(entityID, getOriginalItemStack().clone());
      this.velocityPacket = packetFactory.createVelocityPacket(entityID);
      this.destroyPacket = packetFactory.createDestroyPacket(entityID);

    } else {
      this.spawnPacket = null;
      this.metaPacket = null;
      this.velocityPacket = null;
      this.destroyPacket = null;
    }

    load();
  }

  @Override
  public boolean checkDisplayIsMoved() {

    return false;
  }

  @Override
  public boolean checkDisplayNeedRegen() {

    return false;
  }

  @Override
  public boolean checkIsShopEntity(@NotNull final Entity entity) {

    return false;
  }

  @Override
  public void fixDisplayMoved() {

  }

  @Override
  public void fixDisplayNeedRegen() {

  }

  @Override
  public @Nullable Entity getDisplay() {

    return null;
  }

  @Override
  public boolean isSpawned() {

    return isSpawned;
  }

  @Override
  public boolean isApplicableForPlayer(final Player player) {

    final DisplayApplicableCheckEvent event = new DisplayApplicableCheckEvent(shop, player.getUniqueId());

    event.setApplicable(true);
    event.callEvent();

    return event.isApplicable();
  }

  @Override
  public void remove(final boolean dontTouchWorld) {

    final Iterator<UUID> iterator = packetSenders.iterator();
    while(iterator.hasNext()) {

      final Player nextPlayer = Bukkit.getPlayer(iterator.next());
      if(nextPlayer == null) {

        iterator.remove();
      } else {

        sendDestroyPacket(nextPlayer);
      }
    }

    if(isSpawned()) {

      unload();
      isSpawned = false;
    }
  }

  @Override
  public boolean removeDupe() {

    return false;
  }

  @Override
  public void respawn() {

    Util.ensureThread(false);
    remove(false);
    spawn();
  }

  @Override
  public void safeGuard(@Nullable final Entity entity) {

  }

  @Override
  public void spawn() {

    Util.ensureThread(false);
    if(isSpawned || !shop.isLoaded()) {
      return;
    }
    if(new ShopDisplayItemSpawnEvent(shop, originalItemStack, DisplayType.VIRTUALITEM).callCancellableEvent()) {
      Log.debug(
              "Canceled the displayItem spawning because a plugin setCancelled the spawning event, usually this is a QuickShop Add on");
      return;
    }
    load();

    // Can't rely on the attachedShop cache to be accurate
    // So just try it and if it fails, no biggie
        /*try {
            shop.getAttachedShop().updateAttachedShop();
        } catch (NullPointerException ignored) {
        }*/

    sendFakeItemToAll();
    isSpawned = true;
  }


  //Due to the delay task in ChunkListener
  //We must move load task to first spawn to prevent some bug and make the check lesser
  private void load() {

    Util.ensureThread(false);
    //some time shop can be loaded when world isn't loaded
    chunkLocation = SimpleShopChunk.fromLocation(shop.getLocation());
    manager.put(chunkLocation, this);
    //Let nearby player can saw fake item
    final List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
    onlinePlayers.removeIf(p->!p.getWorld().equals(shop.getLocation().getWorld()));

    for(final Player onlinePlayer : onlinePlayers) {

      final double distance = onlinePlayer.getLocation().distance(shop.getLocation());
      if(Math.abs(distance) > Bukkit.getViewDistance() * 16) {

        continue;
      }
      if(isApplicableForPlayer(onlinePlayer)) { // TODO: Refactor with better way

        packetSenders.add(onlinePlayer.getUniqueId());
      }
    }
  }

  public void sendSpawnPacket(@NotNull final Player player) {

    final PacketHandlerSendSpawnEvent<T> event = new PacketHandlerSendSpawnEvent<>(manager.packetHandler(),
                                                                                       this.packetFactory,
                                                                                       spawnPacket);
    if(event.callCancellableEvent()) {

      Log.debug("Canceled the sending of the spawn packet: " + shop.getShopId());
    } else {

      this.packetFactory.sendPacket(player, event.spawnPacket());
    }
  }

  public void sendMetaPacket(@NotNull final Player player) {

    final PacketHandlerSendMetaEvent<T> event = new PacketHandlerSendMetaEvent<>(manager.packetHandler(),
                                                                                    this.packetFactory,
                                                                                    metaPacket);
    if(event.callCancellableEvent()) {

      Log.debug("Canceled the sending of the meta packet: " + shop.getShopId());
    } else {

      this.packetFactory.sendPacket(player, event.metaPacket());
    }
  }

  public void sendDestroyPacket(@NotNull final Player player) {

    final PacketHandlerSendDestroyEvent<T> event = new PacketHandlerSendDestroyEvent<>(manager.packetHandler(),
                                                                                     this.packetFactory,
                                                                                     destroyPacket);
    if(event.callCancellableEvent()) {

      Log.debug("Canceled the sending of the destroy packet: " + shop.getShopId());
    } else {

      this.packetFactory.sendPacket(player, event.destroyPacket());
    }
  }

  public void sendFakeItem(@NotNull final Player player) {

    this.sendDestroyPacket(player);
    this.sendSpawnPacket(player);
    this.sendMetaPacket(player);
    if(velocityPacket != null) {

      this.packetFactory.sendPacket(player, velocityPacket);
    }
  }

  public void sendFakeItemToAll() {

    final Iterator<UUID> iterator = packetSenders.iterator();
    while(iterator.hasNext()) {

      final Player nextPlayer = Bukkit.getPlayer(iterator.next());
      if(nextPlayer == null) {

        iterator.remove();
      } else {

        sendFakeItem(nextPlayer);
      }
    }
  }

  private void unload() {

    packetSenders.clear();
    manager.remove(chunkLocation, this);
  }

  @NotNull
  public Set<UUID> getPacketSenders() {

    return packetSenders;
  }
}
