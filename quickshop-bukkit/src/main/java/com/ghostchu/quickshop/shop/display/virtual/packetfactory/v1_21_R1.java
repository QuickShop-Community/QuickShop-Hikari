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
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * v1_21_R1
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class v1_21_R1 implements VirtualDisplayPacketFactory {

  private static final WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getItemStackSerializer(false);
  private final QuickShop plugin;
  private final VirtualDisplayItemManager manager;

  public v1_21_R1(final QuickShop plugin, final VirtualDisplayItemManager manager) {

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
  public @NotNull PacketContainer createFakeItemSpawnPacket(final int entityID, @NotNull final Location location) {

    final UUID identifier = UUID.nameUUIDFromBytes(("SHOP:" + entityID).getBytes(StandardCharsets.UTF_8));

    //First, create a new packet to spawn item
    final PacketContainer fakeItemPacket = manager.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);

    //id and velocity
    fakeItemPacket.getIntegers()
            .write(0, entityID)
            .write(1, 0)
            .write(2, 0)
            .write(3, 0);

    //Entity Type
    fakeItemPacket.getEntityTypeModifier().write(0, EntityType.ITEM);

    //UUID
    fakeItemPacket.getUUIDs().write(0, identifier);

    //Location
    fakeItemPacket.getDoubles()
            //X
            .write(0, location.getX())
            //Y
            .write(1, location.getY())
            //Z
            .write(2, location.getZ());
    return fakeItemPacket;
  }

  @Override
  public @NotNull PacketContainer createFakeItemMetaPacket(final int entityID, @NotNull final ItemStack itemStack) {

    final List<WrappedDataValue> values = new ArrayList<>();
    values.add(new WrappedDataValue(8, serializer, MinecraftReflection.getMinecraftItemStack(itemStack)));

    if(plugin.getConfig().getBoolean("shop.display-item-use-name")) {

      final String itemName = GsonComponentSerializer.gson().serialize(Util.getItemStackName(itemStack));

      values.add(new WrappedDataValue(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromJson(itemName).getHandle())));
      values.add(new WrappedDataValue(3, WrappedDataWatcher.Registry.get(Boolean.class), true));
    }

    //Next, create a new packet to update item data (default is empty)
    final PacketContainer fakeItemMetaPacket = manager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
    //Entity ID
    fakeItemMetaPacket.getIntegers().write(0, entityID);
    fakeItemMetaPacket.getDataValueCollectionModifier().write(0, values);

    //Add it
    //For 1.19.2+, we need to use DataValue instead of WatchableObject
    //Check for new version protocolLib
    try {
      Class.forName("com.comphenix.protocol.wrappers.WrappedDataValue");
    } catch(ClassNotFoundException e) {
      throw new RuntimeException("Unable to initialize packet, ProtocolLib update needed", e);
    }
    return fakeItemMetaPacket;
  }

  @Override
  public PacketContainer createFakeItemVelocityPacket(final int entityID) {

    return null;
  }

  @Override
  public @NotNull PacketContainer createFakeItemDestroyPacket(final int entityID) {
    //Also make a DestroyPacket to remove it
    final PacketContainer fakeItemDestroyPacket = manager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);

    try {
      fakeItemDestroyPacket.getIntLists().write(0, Collections.singletonList(entityID));
    } catch(NoSuchMethodError e) {
      throw new IllegalStateException("Unable to initialize packet, ProtocolLib update needed", e);
    }
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
