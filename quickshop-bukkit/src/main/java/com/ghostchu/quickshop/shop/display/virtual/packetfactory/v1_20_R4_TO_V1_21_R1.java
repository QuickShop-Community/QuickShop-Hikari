package com.ghostchu.quickshop.shop.display.virtual.packetfactory;

/*
 * QuickShop - Hikari
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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.shop.SimpleShopChunk;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItem;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItemManager;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class v1_20_R4_TO_V1_21_R1 implements VirtualDisplayPacketFactory {

  private final QuickShop plugin;
  private final VirtualDisplayItemManager manager;

  public v1_20_R4_TO_V1_21_R1(final QuickShop plugin, final VirtualDisplayItemManager manager) {

    this.plugin = plugin;
    this.manager = manager;
  }

  @Override
  public @Nullable Throwable testFakeItem() {

    try {
      createFakeItemSpawnPacket(0, new Location(Bukkit.getServer().getWorlds().get(0), 0, 0, 0));
      createFakeItemMetaPacket(0, new ItemStack(Material.values()[0]));
      createFakeItemVelocityPacket(0);
      createFakeItemDestroyPacket(0);
      return null;
    } catch(Exception throwable) {
      return throwable;
    }
  }

  @Override
  public @NotNull PacketContainer createFakeItemSpawnPacket(final int entityID, @NotNull final Location displayLocation) {


    //First, create a new packet to spawn item
    final PacketContainer fakeItemPacket = manager.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);
    //and add data based on packet class in NMS  (global scope variable)
    //Reference: https://wiki.vg/Protocol#Spawn_Object
    fakeItemPacket.getIntegers()
            //Entity ID
            .write(0, entityID);
    //Velocity x
    fakeItemPacket.getEntityTypeModifier().write(0, EntityType.ITEM);
    //UUID
    fakeItemPacket.getUUIDs().write(0, UUID.randomUUID());
    //Location
    fakeItemPacket.getDoubles()
            //X
            .write(0, displayLocation.getX())
            //Y
            .write(1, displayLocation.getY())
            //Z
            .write(2, displayLocation.getZ());
    return fakeItemPacket;
  }

  @Override
  public @NotNull PacketContainer createFakeItemMetaPacket(final int entityID, @NotNull final ItemStack itemStack) {
    //Next, create a new packet to update item data (default is empty)
    final PacketContainer fakeItemMetaPacket = manager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
    //Entity ID
    fakeItemMetaPacket.getIntegers().write(0, entityID);

    //List<DataWatcher$Item> Type are more complex
    //Create a DataWatcher
    final WrappedDataWatcher wpw = new WrappedDataWatcher();
    //https://wiki.vg/index.php?title=Entity_metadata#Entity
    if(plugin.getConfig().getBoolean("shop.display-item-use-name")) {
      final String itemName = GsonComponentSerializer.gson().serialize(Util.getItemStackName(itemStack));
      wpw.setObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromJson(itemName).getHandle()));
      wpw.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true);
    }

    //Must in the certain slot:https://wiki.vg/Entity_metadata#Item
    wpw.setObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false), itemStack);
    //Add it
    //For 1.19.2+, we need to use DataValue instead of WatchableObject
    //Check for new version protocolLib
    try {
      Class.forName("com.comphenix.protocol.wrappers.WrappedDataValue");
    } catch(ClassNotFoundException e) {
      throw new RuntimeException("Unable to initialize packet, ProtocolLib update needed", e);
    }
    //Convert List<WrappedWatchableObject> to List<WrappedDataValue>
    final List<WrappedWatchableObject> wrappedWatchableObjects = wpw.getWatchableObjects();
    final List<WrappedDataValue> wrappedDataValues = new java.util.LinkedList<>();
    for(final WrappedWatchableObject wrappedWatchableObject : wrappedWatchableObjects) {
      final WrappedDataWatcher.WrappedDataWatcherObject watchableObject = wrappedWatchableObject.getWatcherObject();
      wrappedDataValues.add(new WrappedDataValue(watchableObject.getIndex(), watchableObject.getSerializer(), wrappedWatchableObject.getRawValue()));
    }
    fakeItemMetaPacket.getDataValueCollectionModifier().write(0, wrappedDataValues);
    return fakeItemMetaPacket;
  }

  @Override
  public @NotNull PacketContainer createFakeItemVelocityPacket(final int entityID) {
    //And, create a entity velocity packet to make it at a proper location (otherwise it will fly randomly)
    final PacketContainer fakeItemVelocityPacket = manager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
    fakeItemVelocityPacket.getIntegers()
            //Entity ID
            .write(0, entityID)
            //Velocity x
            .write(1, 0)
            //Velocity y
            .write(2, 0)
            //Velocity z
            .write(3, 0);
    return fakeItemVelocityPacket;
  }

  @Override
  public @NotNull PacketContainer createFakeItemDestroyPacket(final int entityID) {
    //Also make a DestroyPacket to remove it
    final PacketContainer fakeItemDestroyPacket = manager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
    final MinecraftVersion minecraftVersion = manager.getProtocolManager().getMinecraftVersion();
    //On 1.17.1 (may be 1.17.1+? it's enough, Mojang, stop the changes), we need add the int list
    //Entity to remove
    try {
      fakeItemDestroyPacket.getIntLists().write(0, Collections.singletonList(entityID));
    } catch(NoSuchMethodError e) {
      throw new IllegalStateException("Unable to initialize packet, ProtocolLib update needed", e);
    }
    // }
    return fakeItemDestroyPacket;
  }

  @Override
  public @NotNull PacketAdapter getChunkSendPacketAdapter() {

    return new PacketAdapter(plugin.getJavaPlugin(), ListenerPriority.HIGH, PacketType.Play.Server.MAP_CHUNK) {
      @Override
      public void onPacketSending(@NotNull final PacketEvent event) {

        final Player player = event.getPlayer();
        if(player == null || !player.isOnline()) {
          return;
        }
        if(player.getClass().getName().contains("TemporaryPlayer")) {
          return;
        }
        final StructureModifier<Integer> integerStructureModifier = event.getPacket().getIntegers();
        //chunk x
        final int x = integerStructureModifier.read(0);
        //chunk z
        final int z = integerStructureModifier.read(1);

        manager.getChunksMapping().computeIfPresent(new SimpleShopChunk(player.getWorld().getName(), x, z), (chunkLoc, targetList)->{
          for(final VirtualDisplayItem target : targetList) {
            if(!target.isSpawned()) {
              continue;
            }
            if(target.isApplicableForPlayer(player)) { // TODO: Refactor with better way
              target.getPacketSenders().add(player.getUniqueId());
              target.sendDestroyItem(player);
              target.sendFakeItem(player);
            }
          }
          return targetList;
        });
      }
    };
  }

  @Override
  public @NotNull PacketAdapter getChunkUnloadPacketAdapter() {

    return new PacketAdapter(plugin.getJavaPlugin(), ListenerPriority.HIGH, PacketType.Play.Server.UNLOAD_CHUNK) {
      @Override
      public void onPacketSending(@NotNull final PacketEvent event) {

        final Player player = event.getPlayer();
        if(player == null || !player.isOnline()) {
          return;
        }
        if(player.getClass().getName().contains("TemporaryPlayer")) {
          return;
        }
        final StructureModifier<ChunkCoordIntPair> intPairStructureModifier = event.getPacket().getChunkCoordIntPairs();
        final ChunkCoordIntPair pair = intPairStructureModifier.read(0);
        //chunk x
        final int x = pair.getChunkX();
        //chunk z
        final int z = pair.getChunkZ();
        manager.getChunksMapping().computeIfPresent(new SimpleShopChunk(player.getWorld().getName(), x, z), (chunkLoc, targetList)->{
          for(final VirtualDisplayItem target : targetList) {
            if(!target.isSpawned()) {
              continue;
            }
            target.sendDestroyItem(player);
            target.getPacketSenders().remove(player.getUniqueId());
          }
          return targetList;
        });
      }
    };
  }
}
