package com.ghostchu.quickshop.shop.display.virtual.packet.protocollib;
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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.display.PacketFactory;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItemManager;
import com.ghostchu.quickshop.shop.display.virtual.packet.ProtocolLibHandler;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
public class PacketFactoryv1_21 implements PacketFactory<PacketContainer> {

  private static final WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getItemStackSerializer(false);
  private final QuickShop plugin;
  private final VirtualDisplayItemManager manager;

  public PacketFactoryv1_21(final QuickShop plugin, final VirtualDisplayItemManager manager) {

    this.plugin = plugin;
    this.manager = manager;
  }

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
    fakeItemPacket.getEntityTypeModifier().write(0, EntityType.ITEM);

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
    values.add(new WrappedDataValue(8, serializer, MinecraftReflection.getMinecraftItemStack(itemStack)));

    if(plugin.getConfig().getBoolean("shop.display-item-use-name")) {

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
}
