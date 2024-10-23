package com.ghostchu.quickshop.shop.display.virtual;

import com.comphenix.protocol.events.PacketContainer;
import com.ghostchu.quickshop.api.event.display.DisplayApplicableCheckEvent;
import com.ghostchu.quickshop.api.event.display.ShopDisplayItemSpawnEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.shop.SimpleShopChunk;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.shop.display.virtual.packetfactory.VirtualDisplayPacketFactory;
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

public class VirtualDisplayItem extends AbstractDisplayItem implements Reloadable {

  private final int entityID;
  //The List which store packet sender
  private final Set<UUID> packetSenders = new ConcurrentSkipListSet<>();
  private final VirtualDisplayPacketFactory virtualDisplayPacketFactory;
  private final VirtualDisplayItemManager manager;
  private final PacketContainer fakeItemSpawnPacket;
  private final PacketContainer fakeItemMetaPacket;
  private final PacketContainer fakeItemVelocityPacket;
  private final PacketContainer fakeItemDestroyPacket;
  //cache chunk x and z
  private ShopChunk chunkLocation;
  //If packet initialized
  private boolean isSpawned = false;
  //packets

  VirtualDisplayItem(final VirtualDisplayItemManager manager, final VirtualDisplayPacketFactory packetFactory, final Shop shop) {

    super(shop);

    this.entityID = manager.generateEntityId();
    this.manager = manager;
    this.manager.shopEntities.put(shop.getShopId(), entityID);
    this.virtualDisplayPacketFactory = packetFactory;
    this.fakeItemSpawnPacket = virtualDisplayPacketFactory.createFakeItemSpawnPacket(entityID, getDisplayLocation());
    this.fakeItemMetaPacket = virtualDisplayPacketFactory.createFakeItemMetaPacket(entityID, getOriginalItemStack().clone());
    this.fakeItemVelocityPacket = virtualDisplayPacketFactory.createFakeItemVelocityPacket(entityID);
    this.fakeItemDestroyPacket = virtualDisplayPacketFactory.createFakeItemDestroyPacket(entityID);
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

    sendPacketToAll(fakeItemDestroyPacket);
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

  public void sendFakeItem(@NotNull final Player player) {

    sendPacket(player, fakeItemDestroyPacket);
    sendPacket(player, fakeItemSpawnPacket);
    sendPacket(player, fakeItemMetaPacket);
    if(fakeItemVelocityPacket != null) {
      sendPacket(player, fakeItemVelocityPacket);
    }
  }

  public void sendDestroyItem(@NotNull final Player player) {

    sendPacket(player, fakeItemDestroyPacket);
  }

  private void sendPacket(@NotNull final Player player, @NotNull final PacketContainer packet) {

    manager.getProtocolManager().sendServerPacket(player, packet);
  }

  public void sendFakeItemToAll() {

    sendPacketToAll(fakeItemDestroyPacket);
    sendPacketToAll(fakeItemSpawnPacket);
    sendPacketToAll(fakeItemMetaPacket);
    if(fakeItemVelocityPacket != null) {
      sendPacketToAll(fakeItemVelocityPacket);
    }
  }

  private void sendPacketToAll(@NotNull final PacketContainer packet) {

    final Iterator<UUID> iterator = packetSenders.iterator();
    while(iterator.hasNext()) {
      final Player nextPlayer = Bukkit.getPlayer(iterator.next());
      if(nextPlayer == null) {
        iterator.remove();
      } else {
        sendPacket(nextPlayer, packet);
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
