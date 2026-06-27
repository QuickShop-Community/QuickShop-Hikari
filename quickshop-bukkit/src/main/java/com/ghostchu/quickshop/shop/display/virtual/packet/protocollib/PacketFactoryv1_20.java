package com.ghostchu.quickshop.shop.display.virtual.packet.protocollib;
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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.display.PacketFactory;
import com.ghostchu.quickshop.shop.SimpleShopChunk;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItem;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItemManager;
import com.ghostchu.quickshop.shop.display.virtual.packet.ProtocolLibHandler;
import com.ghostchu.quickshop.util.Util;
import lombok.Getter;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PacketFactoryv1_21
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class PacketFactoryv1_20 implements PacketFactory<PacketContainer> {

  private static final WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getItemStackSerializer(false);

  @Getter
  private PacketAdapter chunkSendingPacketAdapter;

  @Getter
  private PacketAdapter chunkUnloadingPacketAdapter;

  /**
   * Creates a spawn packet for the specified ID and display location.
   *
   * @param id              the ID of the packet to be created
   * @param displayLocation the display location where the packet will be spawned
   *
   * @return the spawn packet of type T
   */
  @Override
  public PacketContainer createSpawnPacket(final int id, @NotNull final Location displayLocation) {

    final UUID identifier = UUID.nameUUIDFromBytes(("SHOP:" + id).getBytes(StandardCharsets.UTF_8));

    //First, create a new packet to spawn item
    final PacketContainer fakeItemPacket = ProtocolLibHandler.instance().internal().createPacket(PacketType.Play.Server.SPAWN_ENTITY);

    //id and velocity
    fakeItemPacket.getIntegers()
            .write(0, id)
            .write(1, 0)
            .write(2, 0)
            .write(3, 0);

    //Entity Type
    fakeItemPacket.getEntityTypeModifier().write(0, EntityType.valueOf("DROPPED_ITEM"));

    //UUID
    fakeItemPacket.getUUIDs().write(0, identifier);

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

  /**
   * Creates a metadata packet with the specified ID and ItemStack.
   *
   * @param id        the ID of the metadata packet to be created
   * @param itemStack the ItemStack to include in the metadata packet
   *
   * @return the metadata packet of type T
   */
  @Override
  public PacketContainer createMetaDataPacket(final int id, @NotNull final ItemStack itemStack) {

    final List<WrappedDataValue> values = new ArrayList<>();
    //gravity disabled
    values.add(new WrappedDataValue(5, WrappedDataWatcher.Registry.get(Boolean.class), true));
    values.add(new WrappedDataValue(8, serializer, MinecraftReflection.getMinecraftItemStack(itemStack)));

    if(QuickShop.getInstance().getDisplayManager().useItemName()) {

      final String itemName = GsonComponentSerializer.gson().serialize(Util.getItemStackName(itemStack));

      values.add(new WrappedDataValue(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromJson(itemName).getHandle())));
      values.add(new WrappedDataValue(3, WrappedDataWatcher.Registry.get(Boolean.class), true));
    }

    //Next, create a new packet to update item data (default is empty)
    final PacketContainer fakeItemMetaPacket = ProtocolLibHandler.instance().internal().createPacket(PacketType.Play.Server.ENTITY_METADATA);
    //Entity ID
    fakeItemMetaPacket.getIntegers().write(0, id);
    fakeItemMetaPacket.getDataValueCollectionModifier().write(0, values);

    //Add it
    //For 1.19.2+, we need to use DataValue instead of WatchableObject
    //Check for new version protocolLib
    try {
      Class.forName("com.comphenix.protocol.wrappers.WrappedDataValue");
    } catch(final ClassNotFoundException e) {
      throw new RuntimeException("Unable to initialize packet, ProtocolLib update needed", e);
    }
    return fakeItemMetaPacket;
  }

  /**
   * Creates a text display spawn packet for the specified entity ID and location.
   *
   * @param id       the unique identifier for the entity associated with the text display spawn
   *                 packet
   * @param location the location where the text display will be spawned, cannot be null
   *
   * @return the text display spawn packet of type T
   */
  @Override
  public PacketContainer createTextDisplaySpawnPacket(final int id, @NotNull final Location location) {

    final UUID identifier = UUID.nameUUIDFromBytes(("SHOP_TEXT:" + id).getBytes(StandardCharsets.UTF_8));

    final PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

    packet.getIntegers().write(0, id);
    packet.getUUIDs().write(0, identifier);
    packet.getEntityTypeModifier().write(0, EntityType.TEXT_DISPLAY);

    packet.getDoubles().write(0, location.getX());
    packet.getDoubles().write(1, location.getY());
    packet.getDoubles().write(2, location.getZ());

    packet.getIntegers().write(4, 0);
    packet.getIntegers().write(5, 0);
    return packet;
  }

  /**
   * Creates a name visibility packet for the given entity ID, item stack, and visibility state.
   *
   * @param id        the ID of the entity associated with the packet
   * @param itemStack the ItemStack to include in the packet, cannot be null
   *
   * @return the name visibility packet of type T
   */
  @Override
  public PacketContainer createTextDisplayVisiblePacket(final int id,
                                                        final @NotNull Shop shop,
                                                        final @NotNull ItemStack itemStack) {

    final PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
    packet.getIntegers().write(0, id);

    final int blockDistance = QuickShop.getInstance().getConfig().getInt("shop.text-display.range-blocks", 8);

    final Vector3f scaleVector = new Vector3f(QuickShop.getInstance().getConfig().getFloat("shop.text-display.scale.x", 1.0f),
                                              QuickShop.getInstance().getConfig().getFloat("shop.text-display.scale.y", 1.0f),
                                              QuickShop.getInstance().getConfig().getFloat("shop.text-display.scale.z", 1.0f));

    final WrappedChatComponent component = WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(Util.getTextDisplay(shop, itemStack)));

    final List<WrappedDataValue> data = new ArrayList<>();
    //data.add(new WrappedDataValue(12, WrappedDataWatcher.Registry.get(Vector3f.class), scaleVector));
    data.add(new WrappedDataValue(15, WrappedDataWatcher.Registry.get(Byte.class), (byte)3));
    data.add(new WrappedDataValue(17, WrappedDataWatcher.Registry.get(Float.class), blockDistance * 0.0125f));
    data.add(new WrappedDataValue(23, WrappedDataWatcher.Registry.getChatComponentSerializer(), component.getHandle()));
    data.add(new WrappedDataValue(24, WrappedDataWatcher.Registry.get(Integer.class),
                                  QuickShop.getInstance().getConfig().getInt("shop.text-display.line-width", 200)));
    data.add(new WrappedDataValue(25, WrappedDataWatcher.Registry.get(Integer.class),
                                  QuickShop.getInstance().getConfig().getInt("shop.text-display.background-color", 1073741824)));
    data.add(new WrappedDataValue(26, WrappedDataWatcher.Registry.get(Byte.class),
                                  QuickShop.getInstance().getConfig().getByte("shop.text-display.text-opacity", (byte)-1)));
    data.add(new WrappedDataValue(27, WrappedDataWatcher.Registry.get(Byte.class), Util.createTextDisplayFlags()));


    packet.getDataValueCollectionModifier().write(0, data);

    return packet;
  }

  /**
   * Creates a velocity packet with the specified ID.
   *
   * @param id the ID of the velocity packet to be created
   *
   * @return the velocity packet of type T
   */
  @Override
  public PacketContainer createVelocityPacket(final int id) {

    return null;
  }

  /**
   * Creates a destroy packet for the given ID.
   *
   * @param id the ID of the packet to be destroyed
   *
   * @return the destroy packet of type T
   */
  @Override
  public PacketContainer createDestroyPacket(final int id) {
    //Also make a DestroyPacket to remove it
    final PacketContainer fakeItemDestroyPacket = ProtocolLibHandler.instance().internal().createPacket(PacketType.Play.Server.ENTITY_DESTROY);

    try {
      fakeItemDestroyPacket.getIntLists().write(0, Collections.singletonList(id));
    } catch(final NoSuchMethodError e) {
      throw new IllegalStateException("Unable to initialize packet, ProtocolLib update needed", e);
    }
    return fakeItemDestroyPacket;
  }

  /**
   * Sends the specified packet to the given player.
   *
   * @param player the player to receive the packet, cannot be null
   * @param packet the packet of type T to be sent, cannot be null
   *
   * @return true if the packet was successfully sent, false otherwise
   */
  @Override
  public boolean sendPacket(@NotNull final Player player, @NotNull final PacketContainer packet) {

    ProtocolLibHandler.instance().internal().sendServerPacket(player, packet);
    return true;
  }

  /**
   * Registers the method to listen to the packet sending chunk data.
   */
  @Override
  public void registerSendChunk() {

    this.chunkSendingPacketAdapter = new PacketAdapter(QuickShop.getInstance().getJavaPlugin(), ListenerPriority.HIGH, PacketType.Play.Server.MAP_CHUNK) {

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

        VirtualDisplayItemManager.instance().chunksMapping().computeIfPresent(new SimpleShopChunk(player.getWorld().getName(), x, z), (chunkLoc, targetList)->{
          for(final VirtualDisplayItem<?> target : targetList.values()) {
            if(!target.isSpawned()) {
              continue;
            }
            if(target.isApplicableForPlayer(player)) { // TODO: Refactor with better way
              target.getPacketSenders().add(player.getUniqueId());
              target.sendDestroyPacket(player);
              target.sendFakeItem(player);
            }
          }
          return targetList;
        });
      }
    };

    ProtocolLibHandler.instance().internal().addPacketListener(chunkSendingPacketAdapter);
  }

  /**
   * Unregisters the method to listen to the packet sending chunk data.
   */
  @Override
  public void unregisterSendChunk() {

    if(chunkSendingPacketAdapter != null) {

      ProtocolLibHandler.instance().internal().removePacketListener(chunkSendingPacketAdapter);
    }
  }

  /**
   * Registers the method to listen to the packet sending the unloading of a chunk.
   */
  @Override
  public void registerUnloadChunk() {

    this.chunkUnloadingPacketAdapter = new PacketAdapter(QuickShop.getInstance().getJavaPlugin(), ListenerPriority.HIGH, PacketType.Play.Server.UNLOAD_CHUNK) {
      @Override
      public void onPacketSending(@NotNull final PacketEvent event) {

        final Player player = event.getPlayer();
        if(player == null || !player.isOnline()) {
          return;
        }
        if(player.getClass().getName().contains("TemporaryPlayer")) {
          return;
        }
        
          final StructureModifier<Integer> intModifier = event.getPacket().getIntegers();
          final int x = intModifier.read(0);
          final int z = intModifier.read(1);

        VirtualDisplayItemManager.instance().chunksMapping().computeIfPresent(new SimpleShopChunk(player.getWorld().getName(), x, z), (chunkLoc, targetList)->{
          for(final VirtualDisplayItem<?> target : targetList.values()) {

            if(!target.isSpawned()) {

              continue;
            }
            target.sendDestroyPacket(player);
            target.getPacketSenders().remove(player.getUniqueId());
          }
          return targetList;
        });
      }
    };

    ProtocolLibHandler.instance().internal().addPacketListener(chunkUnloadingPacketAdapter);
  }

  /**
   * Unregisters the method to listen to the packet sending the unloading of a chunk.
   */
  @Override
  public void unregisterUnloadChunk() {

    if(chunkUnloadingPacketAdapter != null) {

      ProtocolLibHandler.instance().internal().removePacketListener(chunkUnloadingPacketAdapter);
    }
  }
}